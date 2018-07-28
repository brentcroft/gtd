package com.brentcroft.util.templates.jstl.tag;

import static java.lang.String.format;

import java.util.Map;

import com.brentcroft.util.templates.JstlTemplateManager.JstlTemplateHandler;

public class JstlInclude extends AbstractJstlElement
{
    private final static String TAG = "c:include";

    private final JstlTemplateHandler jstlTemplateHandler;

    private final String uri;


    public JstlInclude( JstlTemplateHandler jstlTemplateHandler, String uri )
    {
        this.jstlTemplateHandler = jstlTemplateHandler;

        this.uri = uri;


        JstlTemplateHandler parent = jstlTemplateHandler.getParent();

        while ( parent != null )
        {

            if ( uri.equalsIgnoreCase( parent.getUri() ) )
            {
                throw new RuntimeException( format( TagMessages.INCLUDE_CIRCULARITY, uri ) );
            }

            parent = parent.getParent();
        }


        // we're not lazy
        jstlTemplateHandler.loadTemplate( uri );
    }


    public String render( Map<String, ? super Object> bindings )
    {
        return jstlTemplateHandler.expandUri( uri, bindings );
    }

    public String toText()
    {
        return String.format( "<%s page=\"%s\">%s</%s>", TAG, uri, innerRenderable, TAG );
    }
}