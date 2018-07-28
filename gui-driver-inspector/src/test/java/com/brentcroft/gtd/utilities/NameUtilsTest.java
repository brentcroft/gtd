package com.brentcroft.gtd.utilities;

import com.brentcroft.util.XmlUtils;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

@RunWith( Parameterized.class )
public class NameUtilsTest
{
    @Parameterized.Parameters
    public static Collection< Object[] > data()
    {
        return Arrays.asList( new Object[][]{
                { "a=href#url|text()", "alfredo", "alfredo" },
                { "a=href#url|text()", "/alfredo", "alfredo" },
                { "a=href#url|text()", "alfredo/", "alfredo" },
                { "a=href#url|text()", "/alfredo/", "alfredo" },
                { "a=href#url|text()", "http://brentcroft.com/alfredo", "alfredo" },
                //
                { "a=href#url|text()", "https://plus.google.com/?gpsrc=ogpy0&amp;tab=wX", "text-content" },
                { "a=href#url|text()", "https://plus.google.com/alfredo?gpsrc=ogpy0&amp;tab=wX", "alfredo" },
                { "a=href#url|text()", "https://plus.google.com/alfredo.jsp?gpsrc=ogpy0&amp;tab=wX", "alfredo.jsp" },

                { "a=href#url|text()", "http://www.google.co.uk/history/optout?hl=en", "optout" },

                { "a=href#url|text()", "http://www.google.co.uk/intl/en/about.html", "about.html" },

                { "a=href#url|text()", "http://www.google.co.uk/intl/en/policies/privacy/", "privacy" },
        } );
    }

    private NameUtils nameUtils = new NameUtils();

    private String tagAttributes;
    private String xmlText;
    private String expected;

    public NameUtilsTest( String tagAttributes, String url, String expected )
    {
        this.tagAttributes = tagAttributes;
        this.xmlText = format( "<a href=\"%s\">text-content</a>", url );
        this.expected = expected;
    }


    @Test
    public void usesNamers()
    {
        nameUtils.withTagAttributes( tagAttributes );

        assertEquals( expected, nameUtils.buildName( XmlUtils.parse( xmlText ).getDocumentElement() ) );
    }
}