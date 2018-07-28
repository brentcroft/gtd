package com.brentcroft.util.xpath;

import com.brentcroft.util.xpath.gob.Gob;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestGobTest
{
    // the TestGob.toString format
    public static String text = "<?xml version=\"1.0\"?>\n" +
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
                                "</color>\n";

    Gob gobDoc = new TestGob( null )
            .withChildren( new TestGob( "color" )
                    .withAttributes( new Gob.Attr( "time", "place" ) )
                    .withChildren(
                            new TestGob( "blue" )
                                    .withAttributes( new Gob.Attr( "em", "1" ) )
                                    .withChildren(
                                            new TestGob( "mood" )
                                                    .withAttributes(
                                                            new Gob.Attr( "hue", "1" ),
                                                            new Gob.Attr( "decay", "99" ) ),
                                            new TestGob( "sky" )
                                                    .withAttributes(
                                                            new Gob.Attr( "hue", "2" ),
                                                            new Gob.Attr( "decay", "98" ) ) ),
                            new TestGob( "green" )
                                    .withAttributes( new Gob.Attr( "em", "2" ) )
                                    .withChildren(
                                            new TestGob( "forest" )
                                                    .withAttributes(
                                                            new Gob.Attr( "hue", "3" ),
                                                            new Gob.Attr( "decay", "97" ) ),
                                            new TestGob( "day" )
                                                    .withAttributes(
                                                            new Gob.Attr( "hue", "4" ),
                                                            new Gob.Attr( "decay", "96" ) ) ),
                            new TestGob( "red" )
                                    .withAttributes( new Gob.Attr( "em", "3" ) )
                                    .withChildren(
                                            new TestGob( "lorry" )
                                                    .withAttributes(
                                                            new Gob.Attr( "hue", "5" ),
                                                            new Gob.Attr( "decay", "95" ) ),
                                            new TestGob( "rain" )
                                                    .withAttributes(
                                                            new Gob.Attr( "hue", "6" ),
                                                            new Gob.Attr( "decay", "94" ) ) ),
                            new TestGob( "green" )
                                    .withAttributes( new Gob.Attr( "em", "4" ) )
                                    .withChildren(
                                            new TestGob( "forest" )
                                                    .withAttributes(
                                                            new Gob.Attr( "hue", "7" ),
                                                            new Gob.Attr( "decay", "93" ) ),
                                            new TestGob( "night" )
                                                    .withAttributes(
                                                            new Gob.Attr( "hue", "8" ),
                                                            new Gob.Attr( "decay", "92" ) ) ),
                            new TestGob( "red" )
                                    .withAttributes( new Gob.Attr( "em", "5" ) )
                                    .withChildren(
                                            new TestGob( "lorry" )
                                                    .withAttributes(
                                                            new Gob.Attr( "hue", "9" ),
                                                            new Gob.Attr( "decay", "91" ) ),
                                            new TestGob( "rain" )
                                                    .withAttributes(
                                                            new Gob.Attr( "hue", "10" ),
                                                            new Gob.Attr( "decay", "90" ) ) ) ) );


    @Test
    public void fromText() throws Exception
    {
        Gob testGob = TestGob.fromText( text );


        assertEquals( text, testGob.toString() );
        assertEquals( gobDoc.toString(), testGob.toString() );
    }


    @Test
    public void toText() throws Exception
    {
        Gob testGob = TestGob.fromText( gobDoc.toString() );

        assertEquals( text, testGob.toString() );
        assertEquals( gobDoc.toString(), testGob.toString() );
    }
}