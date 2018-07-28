package com.brentcroft.util.templates;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import com.brentcroft.util.templates.el.StandardELFilter;
import com.brentcroft.util.templates.jstl.JstlTemplate;
import com.brentcroft.util.tools.MapBindings;

public class JstlTemplateManagerTest
{

    private final String testTemplate = "This morning the sun was " +
                                        "<c:if test='1<2'>${ c1 } or <c:if test='${3 +1 == 4}'>${ c2 }</c:if>, " +
                                        "days:<c:forEach items='days'>item=${item} ${c4} </c:forEach>, " +
                                        "</c:if>and then some more.";

    private final JstlTemplateManager jstl = new JstlTemplateManager();


    @Test
    public void test_ComplexEL()
    {
        System.out.println(
                  jstl.expandText(
                                   "<${tag_envelope}>\n\t<${tag_head}\n serial=\"${serial}\">\n\t\t${header}\n\t</${tag_head}>\n\t<${tag_body}>\n\t\t${request}\n\t</${tag_body}>\n</${tag_envelope}>",
                                   new MapBindings()
                                                  .withEntry( "header", "alfredo" )
                                                  .withEntry( "request", "montana" )
                                                  .withEntry( "serial", System.currentTimeMillis() )

                                                  .withEntry( "tag_envelope", "soap:envelope" )
                                                  .withEntry( "tag_body", "soap:body" )
                                                  .withEntry( "tag_head", "soap:header" ) ) );
    }



    @Test
    public void test_StripComments()
    {
        final String[][] samples = {
                {"<!-- some comment -->", ""},
                {"red <!-- yellow --> lorry", "red  lorry"},
                {"red<!-- 123 -->green<!-- 123 -->blue<!-- 123 -->brown", "redgreenbluebrown"},

                // bad comments
                {"<!-- some comment --", "<!-- some comment --"},
                {"red <!-- yellow -> lorry", "red <!-- yellow -> lorry"},
                {"red<!-- 123 -->green<!- 123 -->blue<!-- 123 -->brown", "redgreen<!- 123 -->bluebrown"},
        };

        for (String[] sample : samples)
        {

            Assert.assertEquals(sample[1], jstl.maybeStripComments(sample[0]));

            Assert.assertEquals(sample[1], jstl.expandText( sample[0], new MapBindings() ) );

        }
    }




    @Test
    public void test_XmlEscaping()
    {
        final long serial = System.currentTimeMillis();

        final String expected = "<some-tag serial=\"" + serial + "\">montana &amp; arizona: &apos;black&apos; &amp; &quot;blue&quot; (£20 &gt; $20) <some-tag>";

        jstl.getELTemplateManager().setValueExpressionFilter( StandardELFilter.XML_ESCAPE_FILTER );


        final String actual = jstl.expandText(
                                               "<some-tag serial=\"${serial}\">${body}<some-tag>",
                                               new MapBindings()
                                                              .withEntry( "body", "montana & arizona: \'black\' & \"blue\" (£20 > $20) " )
                                                              .withEntry( "serial", serial ) );

        jstl.getELTemplateManager().setValueExpressionFilter( null );


        assertEquals( expected, actual );

        System.out.println( actual );
    }


    @Test
    public void test_XmlDoubleEscaping()
    {
        final long serial = System.currentTimeMillis();

        final String expected = "<some-tag serial=\"" + serial + "\">montana &amp; arizona: &apos;black&apos; &amp; &quot;blue&quot; &gt; 20 &#163; <some-tag>";

        final String actual = jstl.expandText(
                                               "<some-tag serial=\"${serial}\">${body}<some-tag>",
                                               new MapBindings()
                                                              .withEntry( "body", "montana &amp; arizona: &apos;black&apos; &amp; &quot;blue&quot; &gt; 20 &#163; " )
                                                              .withEntry( "serial", serial ) );

        assertEquals( expected, actual );

        System.out.println( actual );
    }


    @Test
    public void test()
    {
        final JstlTemplate jstlTemplate = jstl.buildTemplate( testTemplate );


        System.out.println(
                  jstlTemplate.render(
                              new MapBindings()
                                             .withEntry( "maybe", true )
                                             .withEntry( "c1", "red" )
                                             .withEntry( "c2", "blue" )
                                             .withEntry( "c3", "green" )
                                             .withEntry( "c4", "yellow" )
                                             .withEntry( "c5", "brown" )
                                             .withEntry( "days", new String[] { "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday" } ) ) );

    }

    @Test
    public void test2()
    {
        System.out.println(
                  jstl.expandUri( "src/test/resources/templates/jstl/basic-test-sample.txt",
                                  new MapBindings()
                                                 .withEntry( "maybe", true )
                                                 .withEntry( "c1", "red" )
                                                 .withEntry( "c2", "blue" )
                                                 .withEntry( "c3", "green" )
                                                 .withEntry( "c4", "yellow" )
                                                 .withEntry( "fred", 3 )
                                                 .withEntry( "c5", "brown" )
                                                 .withEntry( "days",
                                                             new String[] { "monday", "tuesday", "wednesday", "thursday", "friday", "saturday", "sunday" } ) ) );
    }


}
