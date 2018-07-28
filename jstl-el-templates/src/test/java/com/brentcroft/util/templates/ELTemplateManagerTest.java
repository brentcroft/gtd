package com.brentcroft.util.templates;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Ignore;
import org.junit.Test;

import com.brentcroft.util.templates.el.StandardELFilter;
import com.brentcroft.util.tools.MapBindings;
import com.brentcroft.util.tools.XsdDateUtils;


public class ELTemplateManagerTest
{
    private final ELTemplateManager el = new ELTemplateManager();


    @Test
    public void test_BasicEL01()
    {
        el.setExpressionFactoryClass( "de.odysseus.el.ExpressionFactoryImpl" );

        assertEquals( "", el.expandText( "", null ) );

        assertEquals( "blue-grey", el.expandText( "blue-grey", null ) );
    }

    @Test
    public void test_BasicEL02()
    {
        el.setExpressionFactoryClass( null );

        final String color = "blue-grey";

        assertEquals(
                      color,
                      el.expandText( "${oof}",
                                     new MapBindings( "oof", color ) ) );
    }

    @Test
    public void test_BasicEL03()
    {
        el.setExpressionFactoryClass( "de.odysseus.el.ExpressionFactoryImpl" );

        assertEquals(
                      "true",
                      el.expandText( "${'foo'.matches('foo|bar')}",
                                     null ) );

        assertEquals(
                      "true",
                      el.expandText( "${oof.matches('foo|bar')}",
                                     new MapBindings( "oof", "bar" ) ) );
    }

    @Test
    public void test_BasicEL04()
    {
        final String letters = "skjaksjhakjsdhkajsdhkajsdh";

        assertEquals(
                      letters.toUpperCase(),
                      el.expandText( String.format( "${'%s'.toUpperCase()}", letters ),
                                     null ) );

        assertEquals(
                      letters.toUpperCase(),
                      el.expandText( "${'skjaksjhakjsdhkajsdhkajsdh'.toUpperCase()}",
                                     new MapBindings( "oof", "bar" ) ) );
    }


    @Test
    public void test_BasicELFunctions01()
    {
        // see:
        // http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html

        assertEquals(
                      "20 30 40",
                      el.expandText( "${c:format( a, x, y, z)}",
                                     new MapBindings()
                                                    .withEntry( "a", "%s %s %s" )
                                                    .withEntry( "x", 20 )
                                                    .withEntry( "y", 30 )
                                                    .withEntry( "z", 40 ) ) );


        assertEquals(
                      " 40  30  20  10",
                      el.expandText( "${c:format( text, a, b, c, d)}",
                                     new MapBindings()
                                                    .withEntry( "text", "%4$3s %3$3s %2$3s %1$3s" )
                                                    .withEntry( "a", 10 )
                                                    .withEntry( "b", 20 )
                                                    .withEntry( "c", 30 )
                                                    .withEntry( "d", 40 ) ) );

        assertEquals(
                      "Amount gained or lost since last statement: $ (1,750.23)",
                      el.expandText( "${c:format( text, money)}",
                                     new MapBindings()
                                                    .withEntry( "text", "Amount gained or lost since last statement: $ %(,.2f" )
                                                    .withEntry( "money", -1750.23 ) ) );


        el.setValueExpressionFilter( StandardELFilter.XML_ESCAPE_FILTER );

        assertEquals(
                      "Duke&apos;s Birthday: 05 23,1995",
                      el.expandText( "${c:format( text, date)}",
                                     new MapBindings()
                                                    .withEntry( "text", "Duke's Birthday: %1$tm %1$te,%1$tY" )
                                                    .withEntry( "date", new GregorianCalendar( 1995, Calendar.MAY, 23 ) ) ) );


        el.setValueExpressionFilter( null );

        assertEquals(
                      "Duke's Birthday: 05 23,1995",
                      el.expandText( "${c:format( text, date)}",
                                     new MapBindings()
                                                    .withEntry( "text", "Duke's Birthday: %1$tm %1$te,%1$tY" )
                                                    .withEntry( "date", new GregorianCalendar( 1995, Calendar.MAY, 23 ) ) ) );

    }


    @Test
    public void test_DateFormatting()
    {
        assertEquals(
                     "2018-08-21",
                     el.expandText( "${ c:format( '%1$tY-%1$tm-%1$td', c:date( 2018, 7, 21 ) ) }", null ) );

        assertEquals(
                     new SimpleDateFormat("yyyy-MM-dd").format( new Date() ),
                     el.expandText( "${ c:format( '%1$tY-%1$tm-%1$td', c:currentTimeMillis() ) }", null ) );

        assertEquals(
                     new SimpleDateFormat("yyyy-MM-dd").format( new Date() ),
                     el.expandText( "${ c:format( '%1$tY-%1$tm-%1$td', c:currentDate() ) }", null ) );

        assertEquals(
                     new SimpleDateFormat("yyyy-MM-dd").format( XsdDateUtils.getCurrentDate( 1,2,3 ) ),
                     el.expandText( "${ c:format( '%1$tY-%1$tm-%1$td', c:currentDate( 1, 2, 3) ) }", null ) );
    }


    @Test
    public void test_ComplexEL()
    {
        System.out.println(
                  el.expandText(
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
    @Ignore
    public void test_XmlEscaping()
    {
        el.setValueExpressionFilter( StandardELFilter.XML_ESCAPE_FILTER );


        final long serial = System.currentTimeMillis();

        final String expected = "<some-tag serial=\"" + serial + "\">montana &amp; arizona: &apos;black&apos; &amp; &quot;blue&quot; (&#163;20 &gt; $20) <some-tag>";

        final String actual = el.expandText(
                                             "<some-tag serial=\"${serial}\">${body}<some-tag>",
                                             new MapBindings()
                                                            .withEntry( "body", "montana & arizona: \'black\' & \"blue\" (�20 > $20) " )
                                                            .withEntry( "serial", serial ) );

        assertEquals( expected, actual );

        System.out.println( actual );
    }


    @Test
    public void test_XmlDoubleEscaping()
    {
        el.setValueExpressionFilter( StandardELFilter.XML_UNESCAPE_ESCAPE_FILTER );


        final long serial = System.currentTimeMillis();

        final String expected = "<some-tag serial=\"" + serial + "\">montana &amp; arizona: &apos;black&apos; &amp; &quot;blue&quot; &gt; 20 £ <some-tag>";

        final String actual = el.expandText(
                                             "<some-tag serial=\"${serial}\">${body}<some-tag>",
                                             new MapBindings()
                                                            .withEntry( "body", "montana &amp; arizona: &apos;black&apos; &amp; &quot;blue&quot; &gt; 20 £ " )
                                                            .withEntry( "serial", serial ) );

        assertEquals( expected, actual );

        System.out.println( actual );
    }
}
