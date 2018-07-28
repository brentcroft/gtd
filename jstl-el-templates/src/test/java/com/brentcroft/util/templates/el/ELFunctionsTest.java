package com.brentcroft.util.templates.el;

import com.brentcroft.util.templates.JstlTemplateManager;
import com.brentcroft.util.tools.MapBindings;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Alaric on 03/07/2017.
 */
public class ELFunctionsTest
{

    private final JstlTemplateManager jstl = new JstlTemplateManager();


    @Test
    public void padLeft() throws Exception
    {
        final String expected = "0000000xyz";

        final String actual = jstl.expandText(
                "${ c:padLeft( text, '0', 10 ) }",
                new MapBindings()
                        .withEntry( "text", "xyz" ) );

        assertEquals( expected, actual );

        System.out.println( actual );
    }

    @Test
    public void padLeftEmpty() throws Exception
    {
        final String expected = "0000000000";

        final String actual = jstl.expandText(
                "${ c:padLeft( text, '0', 10 ) }",
                new MapBindings()
                        .withEntry( "text", "" ) );

        assertEquals( expected, actual );

        System.out.println( actual );
    }

    @Test
    public void padLeftNull() throws Exception
    {
        final String expected = "0000000000";

        final String actual = jstl.expandText(
                "${ c:padLeft( text, '0', 10 ) }",
                new MapBindings()
                        .withEntry( "text", null ) );

        assertEquals( expected, actual );

        System.out.println( actual );
    }

    @Test
    public void padLeftLarge() throws Exception
    {
        final String expected = "1111111111111111";

        final String actual = jstl.expandText(
                "${ c:padLeft( text, '0', 10 ) }",
                new MapBindings()
                        .withEntry( "text", "1111111111111111" ) );

        assertEquals( expected, actual );

        System.out.println( actual );
    }



    @Test
    public void padRight() throws Exception
    {
        final String expected = "xyz0000000";

        final String actual = jstl.expandText(
                "${ c:padRight( text, '0', 10 ) }",
                new MapBindings()
                        .withEntry( "text", "xyz" ) );

        assertEquals( expected, actual );

        System.out.println( actual );
    }

    @Test
    public void padRightEmpty() throws Exception
    {
        final String expected = "0000000000";

        final String actual = jstl.expandText(
                "${ c:padRight( text, '0', 10 ) }",
                new MapBindings()
                        .withEntry( "text", "" ) );

        assertEquals( expected, actual );

        System.out.println( actual );
    }

    @Test
    public void padRightNull() throws Exception
    {
        final String expected = "0000000000";

        final String actual = jstl.expandText(
                "${ c:padRight( text, '0', 10 ) }",
                new MapBindings()
                        .withEntry( "text", null ) );

        assertEquals( expected, actual );

        System.out.println( actual );
    }


    @Test
    public void padRightLarge() throws Exception
    {
        final String expected = "1111111111111111";

        final String actual = jstl.expandText(
                "${ c:padRight( text, '0', 10 ) }",
                new MapBindings()
                        .withEntry( "text", "1111111111111111" ) );

        assertEquals( expected, actual );

        System.out.println( actual );
    }
}