package com.brentcroft.util.xpath;

import com.brentcroft.util.TextUtils;
import com.brentcroft.util.xpath.gob.Gob;
import com.brentcroft.util.xpath.gob.Gobber;
import com.brentcroft.util.xpath.gob.Selection;
import org.junit.Test;

import static java.lang.String.format;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class XParserTest
{

    Gob testGob = TestGob.fromText( "<?xml version=\"1.0\"?>\n" +
                                    "<color time=\"place\">\n" +
                                    "\t<blue em=\"1\">\n" +
                                    "\t\t<mood hue=\"1\" decay=\"99\"/>\n" +
                                    "\t\t<sky hue=\"2\" decay=\"98\"/>\n" +
                                    "\t</blue>\n" +
                                    "\t<green em=\"2\">\n" +
                                    "\t\t<forest hue=\"3\" decay=\"97\"/>\n" +
                                    "\t\t<day hue=\"4\" decay=\"96\"/>\n" +
                                    "\t</green>\n" +
                                    "\t<red em=\"3\">\n" +
                                    "\t\t<lorry hue=\"5\" decay=\"95\"/>\n" +
                                    "\t\t<rain hue=\"6\" decay=\"94\"/>\n" +
                                    "\t</red>\n" +
                                    "\t<green em=\"4\">\n" +
                                    "\t\t<forest hue=\"7\" decay=\"93\"/>\n" +
                                    "\t\t<night hue=\"8\" decay=\"92\"/>\n" +
                                    "\t</green>\n" +
                                    "\t<red em=\"5\">\n" +
                                    "\t\t<lorry hue=\"9\" decay=\"91\"/>\n" +
                                    "\t\t<rain hue=\"10\" decay=\"90\"/>\n" +
                                    "\t</red>\n" +
                                    "</color>\n" );


    @Test
    public void processBasicPaths() throws Exception
    {
        String xpath = "color/green";

        Gobber gobber = Gobber.newGobber( xpath );

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );
        assertNotNull( "No result gobs", result.getGobs() );

        System.out.println( format( "Test: %n  xpath: %s%n  results: %s", TextUtils.indent( xpath ), TextUtils.indentList( result.getGobs() ) ) );

        assertEquals( 2, result.getGobs().size() );
        assertEquals( "green", result.getGobs().get( 0 ).getComponentTag() );
        assertEquals( "green", result.getGobs().get( 1 ).getComponentTag() );
    }

    @Test
    public void processNameFunction() throws Exception
    {
        String xpath = "name( color/green[ @em='2' ] )";

        Gobber gobber = Gobber.newGobber( xpath );

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );
        assertTrue( "No result text", result.isText() );

        System.out.println( format( "Test: %n  xpath: %s%n  results: %s", TextUtils.indent( xpath ), result.toText() ) );

        assertEquals( "green", result.toText() );
    }

    @Test
    public void processContainsFunction() throws Exception
    {
        String xpath = "color/green[ contains( forest/@decay, '9' ) and day/@hue='4' ]";

        Gobber gobber = Gobber.newGobber( xpath );

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );
        assertTrue( "No result gobs", result.isGobs() );


        System.out.println( format( "Test: %n  xpath: %s%n  results: %s", TextUtils.indent( xpath ), TextUtils.indentList( result.getGobs() ) ) );

        assertEquals( "green", result.getGobs().get( 0 ).getComponentTag() );
    }


    @Test
    public void processParenthesesAnd() throws Exception
    {
        String xpath = "color/green[ ( contains( forest/@decay, '9' ) and day/@hue='4' ) ]";

        Gobber gobber = Gobber.newGobber( xpath );

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );
        assertNotNull( "No result gobs", result.getGobs() );


        System.out.println( format( "Test: %n  xpath: %s%n  results: %s", TextUtils.indent( xpath ), TextUtils.indentList( result.getGobs() ) ) );

        assertEquals( 1, result.getGobs().size() );
        assertEquals( "green", result.getGobs().get( 0 ).getComponentTag() );
    }

    @Test
    public void processParenthesesOr() throws Exception
    {
        String xpath = "color/green[ ( contains( forest/@decay, '9' ) or day/@hue='4' ) ]";

        Gobber gobber = Gobber.newGobber( xpath );

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );
        assertNotNull( "No result gobs", result.getGobs() );


        System.out.println( format( "Test: %n  xpath: %s%n  results: %s", TextUtils.indent( xpath ), TextUtils.indentList( result.getGobs() ) ) );

        assertEquals( 2, result.getGobs().size() );
        assertEquals( "green", result.getGobs().get( 0 ).getComponentTag() );
        assertEquals( "green", result.getGobs().get( 1 ).getComponentTag() );
    }


    @Test
    public void processReverseStep() throws Exception
    {
        String xpath = "color/*/*[ ../forest/@hue = '7' or @decay='90']";

        Gobber gobber = Gobber.newGobber( xpath );

        //gobber.dump();

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );
        assertNotNull( "No result gobs", result.getGobs() );

        System.out.println( format( "Test: %n  xpath: %s%n  results: %s", TextUtils.indent( xpath ), TextUtils.indentList( result.getGobs() ) ) );

        assertEquals( 3, result.getGobs().size() );
        assertEquals( "forest", result.getGobs().get( 0 ).getComponentTag() );
        assertEquals( "night", result.getGobs().get( 1 ).getComponentTag() );
        assertEquals( "rain", result.getGobs().get( 2 ).getComponentTag() );
    }


    @Test
    public void processReverseStep2() throws Exception
    {
        String xpath = "name( color/*[ @em='2' ]/forest/../.. )";

        Gobber gobber = Gobber.newGobber( xpath );

        //gobber.dump();

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );
        assertNotNull( "No result text", result.getText() );

        System.out.println( format( "Test: %n  xpath: %s%n  results: %s", TextUtils.indent( xpath ), TextUtils.indent( result.getText() ) ) );

        assertEquals( "color", result.getText() );
    }


    @Test
    public void processReverseStep3() throws Exception
    {
        String xpath = "color/green/../*/forest/../..";

        Gobber gobber = Gobber.newGobber( xpath );

        //gobber.dump();

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );
        assertNotNull( "No result gobs", result.isGobs() );

        System.out.println( format( "Test: %n  xpath: %s%n  results: %s", TextUtils.indent( xpath ), TextUtils.indentList( result.getGobs() ) ) );

        assertEquals( "color", result.getGobs().get( 0 ).getComponentTag() );
    }


    @Test
    public void processPositional01() throws Exception
    {
        String xpath = "color/green[ 2 ]";

        Gobber gobber = Gobber.newGobber( xpath );

        //gobber.dump();

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );
        assertTrue( "No result gobs", result.isGobs() );

        System.out.println( format( "Test: %n  xpath: %s%n  results: %s", TextUtils.indent( xpath ), TextUtils.indentList( result.getGobs() ) ) );

        assertEquals( 1, result.getGobs().size() );
        assertEquals( "green", result.getGobs().get( 0 ).getComponentTag() );
    }


    @Test
    public void processPositional02() throws Exception
    {
        String xpath = "color/green[ position() = 1 ]";

        Gobber gobber = Gobber.newGobber( xpath );

        //gobber.dump();

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );
        assertNotNull( "No result gobs", result.isGobs() );

        System.out.println( format( "Test: %n  xpath: %s%n  results: %s", TextUtils.indent( xpath ), TextUtils.indentList( result.getGobs() ) ) );

        assertEquals( "green", result.getGobs().get( 0 ).getComponentTag() );
    }


    @Test
    public void processPositional03() throws Exception
    {
        String xpath = "color/*[ 1 ]";

        Gobber gobber = Gobber.newGobber( xpath );

        //gobber.dump();

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );
        assertNotNull( "No result gobs", result.isGobs() );

        System.out.println( format( "Test: %n  xpath: %s%n  results: %s", TextUtils.indent( xpath ), TextUtils.indentList( result.getGobs() ) ) );

        assertEquals( "blue", result.getGobs().get( 0 ).getComponentTag() );
    }


    @Test
    public void processPositionalLast() throws Exception
    {
        String xpath = "color/green[ position() = last() ]";

        Gobber gobber = Gobber.newGobber( xpath );

        //gobber.dump();

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );
        assertNotNull( "No result gobs", result.isGobs() );

        System.out.println( format( "Test: %n  xpath: %s%n  results: %s", TextUtils.indent( xpath ), TextUtils.indentList( result.getGobs() ) ) );

        assertEquals( "green", result.getGobs().get( 0 ).getComponentTag() );
    }

    @Test
    public void processPositionalLast02() throws Exception
    {
        String xpath = "color/green[ last() ]";

        Gobber gobber = Gobber.newGobber( xpath );

        //gobber.dump();

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );
        assertNotNull( "No result gobs", result.isGobs() );

        System.out.println( format( "Test: %n  xpath: %s%n  results: %s", TextUtils.indent( xpath ), TextUtils.indentList( result.getGobs() ) ) );

        assertEquals( "green", result.getGobs().get( 0 ).getComponentTag() );
    }


}