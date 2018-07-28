package com.brentcroft.gtd.events;

import java.awt.AWTEvent;
import java.util.Arrays;

import static java.lang.String.format;

/**
 * Created by Alaric on 15/11/2016.
 */
public enum AWTEventMask
{
    ACTION_EVENT_MASK( AWTEvent.ACTION_EVENT_MASK ),
    ADJUSTMENT_EVENT_MASK( AWTEvent.ADJUSTMENT_EVENT_MASK ),
    COMPONENT_EVENT_MASK( AWTEvent.COMPONENT_EVENT_MASK ),
    CONTAINER_EVENT_MASK( AWTEvent.CONTAINER_EVENT_MASK ),
    FOCUS_EVENT_MASK( AWTEvent.FOCUS_EVENT_MASK ),
    HIERARCHY_EVENT_MASK( AWTEvent.HIERARCHY_EVENT_MASK ),
    INVOCATION_EVENT_MASK( AWTEvent.INVOCATION_EVENT_MASK ),
    INPUT_METHOD_EVENT_MASK( AWTEvent.INPUT_METHOD_EVENT_MASK ),
    ITEM_EVENT_MASK( AWTEvent.ITEM_EVENT_MASK ),
    KEY_EVENT_MASK( AWTEvent.KEY_EVENT_MASK ),
    MOUSE_EVENT_MASK( AWTEvent.MOUSE_EVENT_MASK ),
    PAINT_EVENT_MASK( AWTEvent.PAINT_EVENT_MASK ),
    TEXT_EVENT_MASK( AWTEvent.TEXT_EVENT_MASK ),
    WINDOW_EVENT_MASK( AWTEvent.WINDOW_EVENT_MASK ),
    HIERARCHY_BOUNDS_EVENT_MASK( AWTEvent.HIERARCHY_BOUNDS_EVENT_MASK ),
    MOUSE_MOTION_EVENT_MASK( AWTEvent.MOUSE_MOTION_EVENT_MASK ),
    MOUSE_WHEEL_EVENT_MASK( AWTEvent.MOUSE_WHEEL_EVENT_MASK ),
    WINDOW_FOCUS_EVENT_MASK( AWTEvent.WINDOW_FOCUS_EVENT_MASK ),
    WINDOW_STATE_EVENT_MASK( AWTEvent.WINDOW_STATE_EVENT_MASK ),

    // synthetic
    DEFAULT_GUI(
            AWTEvent.ACTION_EVENT_MASK |
            AWTEvent.COMPONENT_EVENT_MASK |
            AWTEvent.INPUT_METHOD_EVENT_MASK |
            AWTEvent.ITEM_EVENT_MASK |
            AWTEvent.KEY_EVENT_MASK |
            AWTEvent.MOUSE_EVENT_MASK |
            AWTEvent.TEXT_EVENT_MASK |
            AWTEvent.WINDOW_EVENT_MASK
    ),

    // synthetic
    MINIMAL_GUI(
            AWTEvent.ACTION_EVENT_MASK |
            AWTEvent.INPUT_METHOD_EVENT_MASK |
            AWTEvent.ITEM_EVENT_MASK |
            AWTEvent.KEY_EVENT_MASK |
            AWTEvent.TEXT_EVENT_MASK |
            AWTEvent.WINDOW_EVENT_MASK |
            AWTEvent.MOUSE_EVENT_MASK
    );





    final long mask;

    AWTEventMask( long mask )
    {
        this.mask = mask;
    }

    public String toString()
    {
//        return format( "%s: mask=%d [%016d]",
//                name(),
//                mask,
//                Long.toString( mask,2 ));
        return format( "%s [%d]",
                name(),
                mask );

    }

    public long getMask()
    {
        return mask;
    }

    public boolean containsAny( long eventId )
    {
        return containsAny( eventId, mask );
    }

    public boolean containsAll( long eventId )
    {
        return containsAll( eventId, mask );
    }


    public static long combine( AWTEventMask... masks )
    {
        long c = 0;

        if ( masks == null || masks.length < 1 )
        {
            return c;
        }

        for ( AWTEventMask eim : masks )
        {
            c = c | eim.mask;
        }

        return c;
    }

    public static boolean containsAny( long eventId, long mask )
    {
        // any non-zero value means there's some intersection
        return ( mask & eventId ) != 0;
    }

    public static boolean containsAll( long eventId, long mask )
    {
        long x = ( mask & eventId );
        // exact subset
        return x == mask;
    }

    public static boolean containsAny( long eventId, AWTEventMask... masks )
    {
        long mask = combine( masks );

        return containsAny( eventId, mask );
    }


    public static void main( String[] args )
    {
        Arrays
                .asList( AWTEventMask.values() )
                .stream()
                .forEach( awtEventMask -> {
                    System.out.println( format( "%s [%s] -> %s",
                            awtEventMask.name(),
                            awtEventMask.mask,
                            AWTEventMask.combine( awtEventMask ) ) );
                } );
    }
}
