package com.brentcroft.util.templates.jstl;


import org.junit.Assert;
import org.junit.Test;

import com.brentcroft.util.templates.JstlTemplateManager;
import com.brentcroft.util.templates.jstl.tag.TagMessages;
import com.brentcroft.util.tools.MapBindings;


public class JstlCatchTest
{
    private final JstlTemplateManager jstl = new JstlTemplateManager();

    private static final String tag = "catch";

    private static final String attr = "var";

    @Test
    public void test_CATCH_NoAttribute()
    {
        final String[] noAttrs = {
                "<c:catch/>",
                "<c:catch>blue</c:catch>"
        };

        for ( String noAttr : noAttrs )
        {
            jstl.expandText( noAttr, new MapBindings().withEntry( "value", true ) );
        }
    }

    @Test
    public void test_CATCH_AttributeBad()
    {
        final String[] badAttrs = {
                "<c:catch test>blue</c:catch>",
                "<c:catch test=>blue</c:catch>",
                "<c:catch test=\">blue</c:catch>",
        };

        for ( String badAttr : badAttrs )
        {
            try
            {
                jstl.expandText( badAttr, new MapBindings() );

                Assert.fail( "Expected an exception" );
            }
            catch ( Exception e )
            {
                Assert.assertEquals( "Parsing Error: Expected closing tag [" + tag + "] but stack is empty", e.getMessage() );
            }
        }
    }

    @Test
    public void test_CATCH_AttributeEmpty()
    {
        final String[] emptyAttrs = {
                "<c:catch var=\"\">blue</c:catch>",
                "<c:catch var=\"\t\n\r\">blue</c:catch>",
                "<c:catch var=\"    \">blue</c:catch>",
        };

        for ( String emptyAttr : emptyAttrs )
        {
            try
            {
                jstl.expandText( emptyAttr, new MapBindings() );

                Assert.fail( "Expected an exception" );
            }
            catch ( Exception e )
            {
                Assert.assertEquals( String.format( TagMessages.OPT_ATTR_EMPTY, attr ), e.getMessage() );
            }
        }
    }


    @Test
    public void test_CATCH_ShortCut()
    {
        final String[] emptyAttrs = {
                "<c:catch />"
        };

        for ( String emptyAttr : emptyAttrs )
        {
            jstl.expandText( emptyAttr, new MapBindings() );
        }
    }


    @Test
    public void test_CATCH_ValidForms()
    {
        final String[] validForms = {
                "<c:catch var=\"${ value }\">${ result }</c:catch>",
                "<c:catch var=\" \n\n\t\t  ${ value }  \">${ result }</c:catch>",
                "<c:catch var=\"value\">${ result }</c:catch>",
                "<c:catch var=\" \n\n\n  value \n\n\n  \">${ result }</c:catch>",

                // with single quotes
                "<c:catch var='${ value }'>${ result }</c:catch>",
                "<c:catch var=' \n\n\t\t  ${ value }  '>${ result }</c:catch>",
                "<c:catch var='value'>${ result }</c:catch>",
                "<c:catch var=' \n\n\n  value \n\n\n  '>${ result }</c:catch>",


        };


        for ( String validForm : validForms )
        {
            final String expected = "Sunday";
            final String actual = jstl.expandText(
                                                   validForm,
                                                   new MapBindings()
                                                                  .withEntry( "value", true )
                                                                  .withEntry( "result", expected ) );
            Assert.assertEquals( expected, actual );
        }

        for ( String validForm : validForms )
        {
            final String expected = "";
            final String actual = jstl.expandText(
                                                   validForm,
                                                   new MapBindings()
                                                                  .withEntry( "value", false )
                                                                  .withEntry( "result", expected ) );
            Assert.assertEquals( expected, actual );
        }
    }


    @Test
    public void test_CATCH_ExposesException()
    {
        final String[][] samples = {
                { "<c:catch><c:script>throw new java.lang.RuntimeException( 'whoops' )</c:script></c:catch>", "caughtException" },
                { "<c:catch var='alfredo'><c:script>throw new java.lang.RuntimeException( 'whoops' )</c:script></c:catch>", "alfredo" },
                { "<c:catch var='fred bloggs'><c:script>throw new java.lang.RuntimeException( 'whoops' )</c:script></c:catch>", "fred bloggs" }
        };

        for ( String[] sample : samples )
        {
            final MapBindings fred = new MapBindings();

            jstl.expandText( sample[ 0 ], fred );

            Assert.assertEquals( RuntimeException.class, fred.get( sample[ 1 ] ).getClass() );
        }
    }

}
