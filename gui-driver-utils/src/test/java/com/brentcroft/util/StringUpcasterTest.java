package com.brentcroft.util;

import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Alaric on 24/03/2017.
 */
public class StringUpcasterTest
{

    @Test
    public void upcastEntitisesX()
    {
        // comma is entitised in all collection downcasts
        assertTrue(
                "comma is unentitised in set",
                StringUpcaster
                        .upcastSet( "green,pur&comma;ple,red" )
                        .contains( "pur,ple" ) );
    }

    @Test
    public void upcastEntitises()
    {
        // comma is entitised in all collection downcasts
        assertTrue(
                "comma is unentitised in set",
                StringUpcaster
                        .upcastSet( "green,pur&comma;ple,red" )
                        .contains( "pur,ple" ) );
        assertEquals(
                "blo,ggs",
                StringUpcaster
                        .upcastMap( "green=blue,fred=blo&comma;ggs,red=purple" )
                        .get( "fred" ) );
        assertTrue(
                "comma is unentitised in MapSet",
                StringUpcaster
                        .upcastMapSet( "green=blue,fred=blo&comma;ggs,red=purple" )
                        .get( "fred" )
                .contains( "blo,ggs" )
        );

        // equals
        assertTrue(
                "Equals is not unentitised in Set",
                StringUpcaster
                        .upcastSet( "green,fr&equals;ed,red" )
                        .contains( "fr&equals;ed" ) );
        assertTrue(
                "Equals is not entitised in Set",
                StringUpcaster
                        .upcastSet( "green,fr=ed,red" )
                        .contains( "fr=ed" ) );

        // equals is unentitised in Map and MapSet
        assertEquals(
                "blo=ggs",
                StringUpcaster
                        .upcastMap( "green=blue,fred=blo&equals;ggs,red=purple" )
                        .get( "fred" ) );

        // and in MapSet
        assertTrue(
                "pe=ars",
                StringUpcaster
                        .upcastMapSet( "fruit = apples | pe&equals;ars, vegetables = carrots | potatoes" )
                        .get( "fruit" ).contains( "pe=ars" ) );


        // pipe is unentitised in Map
        assertEquals(
                "blo&pipe;ggs",
                StringUpcaster
                        .upcastMap( "green=blue,fred=blo&pipe;ggs,red=purple" )
                        .get( "fred" ) );

        // and MapSet
        assertTrue(
                "pe|ars",
                StringUpcaster
                        .upcastMapSet( "fruit = apples | pe&pipe;ars, vegetables = carrots | potatoes" )
                        .get( "fruit" )
                        .contains( "pe|ars" ) );
    }

    @Test( expected = RuntimeException.class )
    public void upcastMustEntitiseCommaInMap()
    {
        StringUpcaster.upcastMap( "green=blue,fred=blo&comma;ggs,red=purple" );
        StringUpcaster.upcastMap( "green=blue,fred=blo,ggs,red=purple" );
    }

    @Test( expected = RuntimeException.class )
    public void upcastMustEntitiseCommaInMapSet()
    {
        StringUpcaster.upcastMapSet( "color=green|blue,days=mon|mon&comma;tues|tues&comma;fri" );
        StringUpcaster.upcastMapSet( "color=green|blue,days=mon|mon,tues|tues,fri" );
    }

    @Test()
    public void upcastMustEntitisePipeInMapSet()
    {
        assertTrue( StringUpcaster.upcastMapSet( "fruit = apples | pe&pipe;ars, vegetables = carrots | potatoes" ).get( "fruit" ).contains( "pe|ars" ) );

        // wrong result
        assertFalse( StringUpcaster.upcastMapSet( "fruit = apples | pe|ars, vegetables = carrots | potatoes" ).get( "fruit" ).contains( "pe|ars" ) );
    }

    @Test( expected = RuntimeException.class )
    public void upcastMustEntitiseEqualsInMap()
    {
        StringUpcaster.upcastMap( "fruit = pe&equals;ars, vegetables = carrots" );
        StringUpcaster.upcastMap( "fruit = pe=ars, vegetables = carrots" );
    }


    @Test( expected = RuntimeException.class )
    public void upcastMustEntitiseEqualsInMapSet()
    {
        assertTrue( StringUpcaster.upcastMapSet( "fruit = apples | pe&equals;ars, vegetables = carrots | potatoes" ).get( "fruit" ).contains( "pe=ars" ) );
        StringUpcaster.upcastMapSet( "fruit = apples | pe=ars, vegetables = carrots | potatoes" );
    }


    @Test
    public void upcastMap()
    {
        assertEquals( Collections.EMPTY_MAP, StringUpcaster.upcastMap( null ) );
        assertEquals( Collections.EMPTY_MAP, StringUpcaster.upcastMap( "" ) );

        //
        assertEquals( "bloggs", StringUpcaster.upcastMap( "fred=bloggs" ).get( "fred" ) );
        assertEquals( "bloggs", StringUpcaster.upcastMap( "green=blue,fred=bloggs" ).get( "fred" ) );
        assertEquals( "bloggs", StringUpcaster.upcastMap( "green=blue,fred=bloggs,red=purple" ).get( "fred" ) );
    }


    @Test
    public void upcastMapSet()
    {
        assertEquals( Collections.EMPTY_MAP, StringUpcaster.upcastMapSet( null ) );
        assertEquals( Collections.EMPTY_MAP, StringUpcaster.upcastMapSet( "" ) );

        //
        assertTrue( StringUpcaster.upcastMapSet( "* = *" ).get( "*" ).contains( "*" ) );
        assertTrue( StringUpcaster.upcastMapSet( "a = b" ).get( "a" ).contains( "b" ) );
        assertTrue( StringUpcaster.upcastMapSet( "a = b | c" ).get( "a" ).contains( "b" ) );
        assertTrue( StringUpcaster.upcastMapSet( "a = b | c" ).get( "a" ).contains( "c" ) );
        assertTrue( StringUpcaster.upcastMapSet( "a | b = c" ).get( "a" ).contains( "c" ) );
        assertTrue( StringUpcaster.upcastMapSet( "a | b = c" ).get( "b" ).contains( "c" ) );

        assertTrue( StringUpcaster.upcastMapSet( "a | b = c | d" ).get( "a" ).contains( "c" ) );
        assertTrue( StringUpcaster.upcastMapSet( "a | b = c | d" ).get( "a" ).contains( "d" ) );
        assertTrue( StringUpcaster.upcastMapSet( "a | b = c | d" ).get( "b" ).contains( "c" ) );
        assertTrue( StringUpcaster.upcastMapSet( "a | b = c | d" ).get( "b" ).contains( "d" ) );
    }


    @Test( expected = RuntimeException.class )
    public void upcastBadMap_NotTwoPartsToOneItem()
    {
        assertEquals( null, StringUpcaster.upcastMap( "fred" ) );
    }

    @Test( expected = RuntimeException.class )
    public void upcastBadMap_FirstPartEmpty1()
    {
        assertEquals( null, StringUpcaster.upcastMap( " = fred" ) );
    }

    @Test( expected = RuntimeException.class )
    public void upcastBadMap_FirstPartEmpty2()
    {
        assertEquals( null, StringUpcaster.upcastMap( "blue=green, = fred,orange=loud" ) );
    }

    @Test( expected = RuntimeException.class )
    public void upcastBadMap_SecondPartEmpty1()
    {
        assertEquals( null, StringUpcaster.upcastMap( "fred = " ) );
    }

    @Test( expected = RuntimeException.class )
    public void upcastBadMap_SecondPartEmpty2()
    {
        assertEquals( null, StringUpcaster.upcastMap( "green=red, fred = , orange=junk" ) );
    }
}