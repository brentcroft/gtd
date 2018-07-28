package com.brentcroft.util.templates.jstl;

import org.junit.Test;

import com.brentcroft.util.templates.JstlTemplateManager;
import com.brentcroft.util.tools.MapBindings;


public class JstlLogTest
{
    private final JstlTemplateManager jstl = new JstlTemplateManager();

    @Test
    public void testLevelAttributes()
    {
        final String[] templates = {
                "<c:log level='info'>this is info</c:log>",
                "<c:log level='debug'>this is debug</c:log>",
                "<c:log level='fatal'>this is fatal</c:log>",
                "<c:log>this is default</c:log>",
        };

        for ( String template : templates )
        {
            jstl.expandText( template, new MapBindings() );
        }
    }


    @Test
    public void testOutputVariables()
    {
        final String[] badtemplates = {
                "<c:log level='info'>alfredo ${ alfredo }</c:log>",
        };

        for ( String template : badtemplates )
        {
            jstl.expandText( template, new MapBindings("alfredo", "montana" ) );
        }
    }
}
