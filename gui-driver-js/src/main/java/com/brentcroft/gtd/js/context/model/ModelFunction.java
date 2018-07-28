package com.brentcroft.gtd.js.context.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.log4j.Logger;

public class ModelFunction extends ModelObject
{
    private final static Logger logger = Logger.getLogger( ModelFunction.class );

    private static final Pattern p = Pattern
            .compile( "function[^\\(]*\\(((\\s*[,]*\\s*[a-zA-Z][a-zA-Z0-9]*)*)\\s*\\)" );

    private final ModelObject caller;

    private final List< String > args = new ArrayList< String >();

    private final String signature;

    public ModelFunction( String name, ScriptObjectMirror function, ModelObject caller )
    {
        super( name, function );
        this.caller = caller;

        signature = extractArguments();

        try
        {
            for ( String argName : signature
                    .replaceAll( "function", "" )
                    .replaceAll( "\\(", "" )
                    .replaceAll( "\\)", "" )
                    .split( "\\s*,\\s*" ) )
            {
                if ( argName.trim().isEmpty() )
                {
                    continue;
                }
                args.add( argName.trim() );
            }
        }
        catch ( Exception e )
        {
            logger.warn( e );
        }
    }

    private String extractArguments()
    {
        String script = path();
        Matcher m = p.matcher( script );
        if ( m.find() && m.groupCount() > 0 )
        {
            return m.group( 1 );
        }
        return null;
    }

    public boolean hasArguments()
    {
        return ! ( args == null || args.isEmpty() );
    }

    public String getArguments()
    {
        if ( ! hasArguments() )
        {
            return null;
        }

        StringBuilder b = new StringBuilder();
        for ( String arg : args )
        {
            if ( b.length() > 0 )
            {
                b.append( ", " );
            }
            b.append( arg );
        }

        return b.toString();
    }

    public String fullname()
    {
        return ( caller != null
                ? caller.fullname() + "."
                : "" )
               + name();
    }


    public String toString()
    {
        return name + "( " + ( hasArguments() ? getArguments() : "" ) + " )";
    }


    @Override
    public boolean isFunction()
    {
        return true;
    }

    @Override
    public boolean isObject()
    {
        return false;
    }

    public Object call( Object... args )
    {
        return mirror.call( caller.mirror, args );
    }
}