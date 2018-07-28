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
public class XParserAxisTest
{
    @Parameterized.Parameters
    public static Collection< Object[] > data()
    {
        return Arrays.asList( new Object[][]{
                { "name( color/self::color/self::color )", "color" },
                { "name( color/*/*[ self::forest/@hue = '7' ] )", "forest" },
                { "name( color/green/forest/self::*[ @hue = '7' ][ 3 ] )", "forest" },
                { "name( color/*[ last() ]/preceding-sibling::*[ last() ] )", "blue" },
                { "name( color/*[ 1 ]/following-sibling::*[ last() ] )", "red" },
                { "name( color/*[ 1 ]/parent::* )", "color" },
                { "name( color/*[ 1 ]/ancestor-or-self::blue )", "blue" },
                { "name( //ancestor-or-self::blue )", "blue" },
                { "name( color/*/*/ancestor-or-self::blue )", "blue" },


                { "name( descendant::forest[ 1 ] )", "forest" },

                //{ "color[ @time ]", "sky" }


        } );
    }

    private String xpath;

    private Object expected;

    public XParserAxisTest( String xpath, String expected )
    {
        this.xpath = xpath;
        this.expected = expected;
    }

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
    public void axis() throws Exception
    {
        Gobber gobber = Gobber.newGobber( xpath );

        Selection result = gobber.execute( testGob );

        assertNotNull( "No result", result );

        System.out.println( format( "Test: %s = %s", xpath, result ) );

        assertEquals( expected, result.toText() );
    }
}