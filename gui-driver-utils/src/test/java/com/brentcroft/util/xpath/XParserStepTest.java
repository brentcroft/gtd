package com.brentcroft.util.xpath;

import com.brentcroft.util.xpath.gob.Gob;
import com.brentcroft.util.xpath.gob.Gobber;
import com.brentcroft.util.xpath.gob.Selection;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith( Parameterized.class )
public class XParserStepTest
{
    @Parameterized.Parameters
    public static Collection< Object[] > data()
    {
        return Arrays.asList( new Object[][]{
                { "color//day[ @decay=96 ]/@hue", "4" },
                { "color//*[ @decay=96 ]/@hue", "4" },
                { "//red[ lorry/@decay=95 ]/rain/@hue", "6" },
                { "color/*/*[ ../@em=2 ][ 2 ]/@decay", "96" },
                { "color/*/*[ ../@em=2 ][ name( . ) = 'forest' ]/@decay", "97" },
                { "color/*/*[ ../@em=2 ][ name( . ) = 'day' ]/@decay", "96" },
        } );
    }

    Gob testGob = TestGob.fromText(
            "<color time='place'>" +
            "  <blue em='1'>" +
            "    <mood hue='1' decay='99'/>" +
            "    <sky hue='2' decay='98'/>" +
            "  </blue>" +
            "  <green em='2'>" +
            "    <forest hue='3' decay='97'/>" +
            "    <day hue='4' decay='96'/>" +
            "  </green>" +
            "  <red em='3'>" +
            "    <lorry hue='5' decay='95'/>" +
            "    <rain hue='6' decay='94'/>" +
            "  </red>" +
            "  <green em='4'>" +
            "    <forest hue='7' decay='93'/>" +
            "    <night hue='8' decay='92'/>" +
            "  </green>" +
            "  <red em='5'>" +
            "    <lorry hue='9' decay='91'/>" +
            "    <rain hue='10' decay='90'/>" +
            "  </red>" +
            "</color>" );


    private String xpath;

    private Object expected;

    public XParserStepTest( String xpath, String expected )
    {
        this.xpath = xpath;
        this.expected = expected;
    }


    @Test
    public void step() throws Exception
    {
        Gobber gobber = Gobber.newGobber( xpath );

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );

        System.out.println( format( "Test: %s = %s", xpath, result ) );

        assertEquals( expected, result.toText() );
    }
}