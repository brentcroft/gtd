package com.brentcroft.gtd.events;

import com.brentcroft.gtd.events.AWTEventMask;
import org.junit.Test;

import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;

import static org.junit.Assert.*;

/**
 * Created by Alaric on 15/11/2016.
 */
public class AWTEventMaskTest
{
    @Test
    public void combineNoMasks() throws Exception
    {
        assertEquals( 0, AWTEventMask.combine() );
    }


    @Test
    public void combineMasks01() throws Exception
    {
        long expected = AWTEventMask.ACTION_EVENT_MASK.mask;

        assertEquals( expected, AWTEventMask.combine(
                AWTEventMask.ACTION_EVENT_MASK,
                AWTEventMask.ACTION_EVENT_MASK,
                AWTEventMask.ACTION_EVENT_MASK ) );
    }


    @Test
    public void combineMasks02() throws Exception
    {
        long expected = AWTEventMask.ACTION_EVENT_MASK.mask |
                        AWTEventMask.ADJUSTMENT_EVENT_MASK.mask |
                        AWTEventMask.COMPONENT_EVENT_MASK.mask;


        assertEquals( expected, AWTEventMask.combine(
                AWTEventMask.ACTION_EVENT_MASK,
                AWTEventMask.ADJUSTMENT_EVENT_MASK,
                AWTEventMask.COMPONENT_EVENT_MASK ) );
    }


    @Test
    public void combineMasks03() throws Exception
    {
        long expected = 0;

        for ( AWTEventMask eim : AWTEventMask.values() )
        {
            expected = expected | eim.mask;
        }

        assertEquals( expected, AWTEventMask.combine( AWTEventMask.values() ) );
    }


    @Test
    public void containsMasks01() throws Exception
    {
        assertTrue(
                AWTEventMask.containsAny(
                        ItemEvent.ITEM_STATE_CHANGED,
                        AWTEventMask.ITEM_EVENT_MASK ) );
    }

    @Test
    public void containsMasks02() throws Exception
    {
        assertFalse(
                AWTEventMask.containsAny(
                        ItemEvent.ITEM_STATE_CHANGED,
                        AWTEventMask.ADJUSTMENT_EVENT_MASK ) );
    }


    @Test
    public void containsMasks03() throws Exception
    {
        assertTrue( AWTEventMask.containsAny( MouseEvent.MOUSE_CLICKED, AWTEventMask.MOUSE_EVENT_MASK ) );
    }


    @Test
    public void containsMasks04() throws Exception
    {
        AWTEventMask[] masks01 = {
                AWTEventMask.ITEM_EVENT_MASK,
                AWTEventMask.MOUSE_EVENT_MASK };

        AWTEventMask[] masks02 = {
                AWTEventMask.PAINT_EVENT_MASK,
                AWTEventMask.HIERARCHY_BOUNDS_EVENT_MASK };

        assertTrue( AWTEventMask.containsAny( ItemEvent.ITEM_STATE_CHANGED, masks01 ) );
        assertTrue( AWTEventMask.containsAny( MouseEvent.MOUSE_CLICKED, masks01 ) );

        assertFalse( AWTEventMask.containsAny( ItemEvent.ITEM_STATE_CHANGED, masks02 ) );
        assertFalse( AWTEventMask.containsAny( MouseEvent.MOUSE_CLICKED, masks02 ) );
    }



    @Test
    public void containsMasks05() throws Exception
    {
        long masks01 = AWTEventMask.combine(
                AWTEventMask.ITEM_EVENT_MASK,
                AWTEventMask.MOUSE_EVENT_MASK );

        long masks02 = AWTEventMask.combine(
                AWTEventMask.PAINT_EVENT_MASK,
                AWTEventMask.HIERARCHY_BOUNDS_EVENT_MASK );

        assertTrue( AWTEventMask.containsAny( ItemEvent.ITEM_STATE_CHANGED, masks01 ) );
        assertTrue( AWTEventMask.containsAny( MouseEvent.MOUSE_CLICKED, masks01 ) );

        assertFalse( AWTEventMask.containsAny( ItemEvent.ITEM_STATE_CHANGED, masks02 ) );
        assertFalse( AWTEventMask.containsAny( MouseEvent.MOUSE_CLICKED, masks02 ) );
    }

    @Test
    public void containsAllMasks01() throws Exception
    {
        assertTrue( AWTEventMask.containsAll( 208, 0 ) );
        assertFalse( AWTEventMask.containsAll( 208, 2 ) );
    }


    @Test
    public void containsAllMasks02() throws Exception
    {
        assertTrue( AWTEventMask.containsAll( 208, 16 ) );
    }

    @Test
    public void containsAllMasks03() throws Exception
    {
        assertTrue( AWTEventMask.containsAll( MouseEvent.MOUSE_CLICKED, AWTEventMask.MOUSE_EVENT_MASK.getMask() ) );
    }
}