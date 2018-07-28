package com.brentcroft.util.templates.jstl.tag;

import java.util.Map;

import com.brentcroft.util.templates.jstl.JstlTemplate;
import com.brentcroft.util.tools.MapBindings;

public class JstlOtherwise extends AbstractJstlElement
{
    private final static String TAG = "c:otherwise";

    public JstlOtherwise()
    {
        innerRenderable = new JstlTemplate( this );
    }

    public String render( Map<String, ? super Object> bindings )
    {
        // protect external bindings from pollution in local scope
        return innerRenderable.render( new MapBindings( bindings ) );
    }

    public String toText()
    {
        return String.format( "<%s>%s</%s>", TAG, innerRenderable, TAG );
    }
}