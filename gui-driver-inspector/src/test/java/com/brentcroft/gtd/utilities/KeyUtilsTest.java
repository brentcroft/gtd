package com.brentcroft.gtd.utilities;

import com.brentcroft.util.XPathUtils;
import com.brentcroft.util.XmlUtils;
import java.net.URISyntaxException;
import java.util.Properties;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.junit.Assert.assertEquals;

public class KeyUtilsTest
{
    String xmlText = "<div xmlns:a=\"com.brentcroft.gtd.model\" class=\"_Oj\" hash=\"533466614\">\n" +
                     "\t<div class=\"_Vm\" hash=\"533469286\">\n" +
                     "\t\t<div class=\"_yf\" hash=\"533464230\">\n" +
                     "\t\t\t<label for=\"_cKg\" hash=\"533459397\">\n" +
                     "\t\t\t\t<text a:actions=\"text\" hash=\"735153560\">\n   any of these words:   \n</text>\n" +
                     "\t\t\t</label>\n" +
                     "\t\t</div>\n" +
                     "\t</div>\n" +
                     "\t<div class=\"_Ai\" hash=\"533470550\">\n" +
                     "\t\t<div class=\"_YR\" hash=\"533464902\">\n" +
                     "\t\t\t<input a:actions=\"text\" class=\"jfk-textinput\" hash=\"735408506\" id=\"_cKg\" name=\"as_oq\" type=\"text\"/>\n" +
                     "\t\t</div>\n" +
                     "\t</div>\n" +
                     "\t<div class=\"_Rj _YQ\" hash=\"533470182\">\n" +
                     "\t\t<div class=\"_yf\" hash=\"533464006\">\n" +
                     "\t\t\t<text a:actions=\"text\" hash=\"735151432\">Type</text>\n" +
                     "\t\t\t<span class=\"_fq\" hash=\"533464678\">\n" +
                     "\t\t\t\t<text a:actions=\"text\" hash=\"735156280\">OR</text>\n" +
                     "\t\t\t</span>\n" +
                     "\t\t\t<text a:actions=\"text\" hash=\"735151224\">between all the words you want:</text>\n" +
                     "\t\t\t<span class=\"_fq\" hash=\"533463926\">\n" +
                     "\t\t\t\t<text a:actions=\"text\" hash=\"735151544\">miniature OR standard</text>\n" +
                     "\t\t\t</span>\n" +
                     "\t\t</div>\n" +
                     "\t</div>\n" +
                     "</div>";


//    @Test
//    public void htmlFor_inputLabel()
//    {
//        Properties p = new Properties();
//
//        p.put( "modeller.xpath.lookupLabels", "true" );
//        p.put( "modeller.xpath.tagsAllowedHtmlForLabels", "input=label" );
//
//        Document d = XmlUtils.parse( xmlText );
//
//        Element node = ( Element ) XPathUtils.getNode( d, "//input[ @hash='735408506' ]" );
//
//        String labelPredicate = new KeyUtils()
//                .configure( p )
//                .preCache( d )
//                .maybeUpdateNameFromLabelHtmlFor( node );
//
//        //System.out.println( labelPredicate );
//
//        assertEquals( "( @id=//label[ contains( text/text(), 'any of these words:' ) ]/@for )", labelPredicate );
//
//    }

    @Test
    public void parseX() throws URISyntaxException
    {
        assertEquals(
                "/in/the/room?maybe",
                new KeyUtils().getURI( "elephant /in/the/room?maybe ", 2 ).toString()
        );
    }

    @Test
    public void parseUri() throws URISyntaxException
    {
        assertEquals(
                "/in/the/room?maybe",
                new KeyUtils().getURI( "elephant /in/the/room?maybe", 1 ).toString()
        );

        assertEquals(
                "http://elephant/in/the/room?maybe",
                new KeyUtils().getURI( "http://elephant/in/the/room?maybe", 0 ).toString()
        );

        assertEquals(
                "elephant/in/the/room?maybe",
                new KeyUtils().getURI( "elephant/in/the/room?maybe", 0 ).toString()
        );
    }

    @Test
    public void parseUriPredicate()
    {
        assertEquals(
                "contains( @href, '/in/the/room' ) and contains( @href, 'maybe' )",
                new KeyUtils().getUrlPredicate( "href", "elephant /in/the/room?maybe" )
        );
        assertEquals(
                "contains( @href, '/in/the/room' ) and contains( @href, 'maybe' ) and contains( @href, 'alfredo' )",
                new KeyUtils().getUrlPredicate( "href", "elephant /in/the/room?maybe#alfredo" )
        );
    }
}