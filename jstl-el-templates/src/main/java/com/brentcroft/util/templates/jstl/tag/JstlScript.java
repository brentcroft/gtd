package com.brentcroft.util.templates.jstl.tag;


import static java.lang.String.format;

import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Level;

import com.brentcroft.util.templates.jstl.JstlTemplate;
import com.brentcroft.util.tools.MapBindings;

public class JstlScript extends AbstractJstlElement
{
    private final static String TAG = "c:script";

    private final boolean isPublic;

    private final boolean renderOutput;

    private final ScriptEngine engine;

    private CompiledScript script;


    private final static ScriptEngineManager scriptEngineManager = new ScriptEngineManager();

    public final static String DEFAULT_SCRIPT_ENGINE_NAME = "js";

    private final static ScriptEngine defaultScriptEngine = scriptEngineManager.getEngineByName( DEFAULT_SCRIPT_ENGINE_NAME );


    public JstlScript( boolean publicScope, boolean renderOutput, String engineName )
    {
        this.isPublic = publicScope;
        this.renderOutput = renderOutput;

        if ( DEFAULT_SCRIPT_ENGINE_NAME.equalsIgnoreCase( engineName ) )
        {
            engine = defaultScriptEngine;
        }
        else
        {
            engine = scriptEngineManager.getEngineByName( engineName );
        }

        if ( engine == null )
        {
            throw new RuntimeException( format( TagMessages.ENGINE_NAME_NOT_FOUND, engineName ) );
        }


        innerRenderable = new JstlTemplate( this );
    }

    private void compile()
    {
        try
        {
            final String source = innerRenderable.render( EMPTY_MAP );

            script = ( (Compilable) engine ).compile( source );
        }
        catch ( ScriptException e )
        {
            throw new RuntimeException( e );
        }
    }

    @Override
    public void normalize()
    {
        compile();
    }


    public String render( final Map<String, ? super Object> bindings )
    {
        if ( script == null )
        {
            return "";
        }


        /*
         * problem is that Nashorn doesn't update bindings unless it created
         * them.
         *
         * http://stackoverflow.com/questions/24142979/reading-updated-variables-after-evaluating-a-script
         */
        final Bindings engineBindings = engine.createBindings();


        if ( bindings instanceof MapBindings )
        {
            ( (MapBindings) bindings ).copyTo( engineBindings );
        }
        else
        {
            engineBindings.putAll( bindings );
        }

        try
        {
            Object result;

            // evaluate the script
            result = script.eval( engineBindings );

            if ( isPublic  )
            {
                // need to copy back all first level members
                // this must be copying out loads of other crap
                // TODO: figure out how to handle bindings
                final String[] keys = engineBindings.keySet().toArray( new String[engineBindings.size()] );

                for ( String key : keys )
                {
                    bindings.put( key, engineBindings.get( key ) );
                }
            }

            return ( renderOutput && result != null ) ? result.toString() : "";
        }
        catch ( ScriptException e )
        {
            throw new RuntimeException( e );
        }
    }


    public String toText()
    {
        return String.format( "<%s%s%s>%s</%s>",
                              TAG,
                              !isPublic ? "" : " public=\"true\"",
                              !renderOutput ? "" : " render=\"true\"",
                              innerRenderable,
                              TAG );
    }
}
