package com.brentcroft.util.templates.jstl.tag;

import java.util.Map;

import org.apache.log4j.Level;

import com.brentcroft.util.templates.jstl.JstlTemplate;
import com.brentcroft.util.tools.MapBindings;

public class JstlLog extends AbstractJstlElement
{
    private final static String TAG = "c:log";

    final Level level;

    public JstlLog( Level level )
    {
        this.level = level;

        innerRenderable = new JstlTemplate( this );
    }


    public String render( Map<String, ? super Object> bindings )
    {
        if ( logger != null )
        {
            if ( logger.isEnabledFor( level ) )
            {
                // protect external bindings from pollution in local scope
                final String msg = innerRenderable.render( new MapBindings( bindings ) );

                logger.log( level, msg );
            }
        }
        return "";
    }


    public String toText()
    {
        return String.format( "<%s level=\"%s\">%s</%s>", TAG, level, innerRenderable, TAG );
    }
}
