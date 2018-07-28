package com.brentcroft.util;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by Alaric on 16/11/2016.
 */
public class XmlUtilsTest
{

    @Test
    public void escapeApostrophe() throws Exception
    {
        String text = "here's john's one.";
        String expected = format( "here&apos;s john&apos;s one." );

        String actual = XmlUtils.escapeForXmlAttribute( text );

        //System.out.println( "escape: [" + text + "] -> [" + actual + "]" );

        assertEquals( expected, actual );
    }

    @Test
    public void escapeQuotes() throws Exception
    {
        String text = "the \"blue\" \"one\".";
        String expected = format( "the &quot;blue&quot; &quot;one&quot;." );

        String actual = XmlUtils.escapeForXmlAttribute( text );

        //System.out.println( "escape: [" + text + "] -> [" + actual + "]" );

        assertEquals( expected, actual );
    }

    @Test
    public void escapeLessThan() throws Exception
    {
        String text = "the <blue< <one<.";
        String expected = format( "the &lt;blue&lt; &lt;one&lt;." );

        String actual = XmlUtils.escapeForXmlAttribute( text );

        //System.out.println( "escape: [" + text + "] -> [" + actual + "]" );

        assertEquals( expected, actual );
    }

    @Test
    public void escapeMoreThan() throws Exception
    {
        String text = "the >blue> >one>.";
        String expected = format( "the &gt;blue&gt; &gt;one&gt;." );

        String actual = XmlUtils.escapeForXmlAttribute( text );

        //System.out.println( "escape: [" + text + "] -> [" + actual + "]" );

        assertEquals( expected, actual );
    }

    @Test
    public void escapeAmpersand() throws Exception
    {
        String text = "red & blue & green";
        String expected = format( "red &amp; blue &amp; green" );

        String actual = XmlUtils.escapeForXmlAttribute( text );

        //System.out.println( "escape: [" + text + "] -> [" + actual + "]" );

        assertEquals( expected, actual );
    }


    @Test
    public void escapeForAllBelowX20() throws Exception
    {
        for ( int i = 1, n = 20; i < n; i++ )
        {
            String text = "" + ( char ) i;

            String expected = format( "&#%04d;", i );

            String actual = XmlUtils.escapeForXmlAttribute( text );

            //System.out.println( "escape: [" + text + "] -> [" + actual + "]" );

            assertEquals( expected, actual );
        }
    }

    @Test
    public void escapeForAllEscapedBelowX20() throws Exception
    {
        for ( int i = 1, n = 20; i < n; i++ )
        {
            String text = format( "&#%s;", i );

            String expected = format( "&amp;#%s;", i );

            String actual = XmlUtils.escapeForXmlAttribute( text );

            //System.out.println( "escape: [" + text + "] -> [" + actual + "]" );

            assertEquals( expected, actual );
        }
    }

    @Test
    public void escapeForAllAboveX7F() throws Exception
    {
        for ( int i = 0x80, n = 0xff; i <= n; i++ )
        {
            String text = "" + ( char ) i;

            String expected = format( "&#%04d;", i );

            String actual = XmlUtils.escapeForXmlAttribute( text );

            //System.out.println( "escape escaped: [" + text + "] -> [" + actual + "]" );

            assertEquals( expected, actual );
        }
    }

    @Test
    public void escapeForAllEscapedAboveX7F() throws Exception
    {
        for ( int i = 0x80, n = 0xff; i <= n; i++ )
        {
            String text = format( "&#%s;", i );

            String expected = format( "&amp;#%s;", i );

            String actual = XmlUtils.escapeForXmlAttribute( text );

            //System.out.println( "escape escaped: [" + text + "] -> [" + actual + "]" );

            assertEquals( expected, actual );
        }
    }

    @Test
    public void filterByXPath()
    {
        String xml = "<g><a1><b>hello</b></a1><a2><b>hello</b></a2></g>";
        String path = "//a2/b";
        String expected = "<g><a2><b>hello</b></a2></g>";

        Document d = XmlUtils.filterByPath( XmlUtils.parse( xml ), path );

        XmlUtils.removeTrimmedEmptyTextNodes( d );

        String actual = XmlUtils.serialize( d, false, false );


        //System.out.println( actual );

        assertEquals( expected, actual );
    }


    @Test
    public void erode()
    {
        String xml = "<g><a1 x=\"true\"><b>hello</b></a1><a2><b x=\"true\">hello</b></a2></g>";
        String expected = "<g><a2><b x=\"true\">hello</b></a2></g>";

        Document d = XmlUtils.parse( xml );

        XmlUtils.erode( d.getDocumentElement(), element -> element.hasAttribute( "x" ) );

        XmlUtils.removeTrimmedEmptyTextNodes( d );

        String actual = XmlUtils.serialize( d, false, false );


        System.out.println( actual );

        //assertEquals( expected, actual );
    }


    @Test
    public void testRemoveDtd01()
    {
        final String propertiesDTD = "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">";

        final String body = "12345";

        String xmlText = propertiesDTD + "\n" + body;

        String stripped = XmlUtils.removeAnyDtd( xmlText );


        // System.out.println( stripped );

        assertEquals( "DTD not stripped correctly", body, stripped );
    }


    @Test
    public void testRemoveDtd02()
    {
        final String testXml = "<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\"><properties><entry key=\"REQ_TYPE_CD\">M</entry><entry key=\"DIV_NUM\">O2CVDUMMY</entry><entry key=\"ACCT_NUM\">4111111111111111</entry><entry key=\"CURR_CD\">GBP</entry><entry key=\"MOP_TYPE_CD\">??</entry><entry key=\"ORD_DTM\">2014-07-02 11:24:16</entry><entry key=\"ACT_CD\">OA</entry><entry key=\"S_KEY_ID\">136</entry><entry key=\"AMT\">1000</entry><entry key=\"ORD_ID\">4b125668f614</entry><entry key=\"CARD_EXP_DT\">0715</entry></properties>";


        String stripped = XmlUtils.removeAnyDtd( testXml );

        // System.out.println( stripped );

        assertFalse( "DTD not stripped correctly", stripped.startsWith( "<!DOCTYPE" ) );
    }



    @Test
    public void testSortXml() throws Exception
    {
        Document document = XmlUtils.parse( "<x><red c=\"red\"/><green c=\"green\"/><blue c=\"blue\"/></x>" );

        Node node = XmlUtils.sort( document, "@c" );

        assertEquals( "<x><blue c=\"blue\"/><green c=\"green\"/><red c=\"red\"/></x>", XmlUtils.serialize( node, false, false ) );
    }
}