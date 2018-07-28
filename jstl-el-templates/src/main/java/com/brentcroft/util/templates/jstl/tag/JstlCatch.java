package com.brentcroft.util.templates.jstl.tag;

import java.util.Map;

import org.apache.log4j.Level;

import com.brentcroft.util.templates.jstl.JstlTemplate;
import com.brentcroft.util.tools.MapBindings;

public class JstlCatch extends AbstractJstlElement
{
    private final static String TAG = "c:script";

    private final String exceptionName;


    public JstlCatch( String exceptionName )
    {
        this.exceptionName = exceptionName;

        innerRenderable = new JstlTemplate( this );
    }

    public String render( Map<String, ? super Object> bindings )
    {
        try
        {
            // protect external bindings from pollution in local scope
            return innerRenderable.render( new MapBindings( bindings ) );
        }
        catch ( Throwable t )
        {
            bindings.put( exceptionName, t );


            if ( logger != null )
            {
                final Level level = Level.DEBUG;

                if ( logger.isEnabledFor( level ) )
                {
                    logger.log( level, "Caught exception and inserted as [" + exceptionName + "]: " + t );
                }
            }

            return "";
        }
    }

    public String toText()
    {
        return String.format( "<%s var=\"%s\">%s</%s>", TAG, exceptionName, innerRenderable, TAG );
    }
}