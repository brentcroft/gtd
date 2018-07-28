package com.brentcroft.util.tools;

import java.util.HashMap;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 *
 * See:
 * https://wiki.openjdk.java.net/display/Nashorn/Nashorn+jsr223+engine+notes
 *
 * @author ADobson
 *
 */
public class ScriptManager
{
    private static Logger logger = Logger.getLogger( ScriptManager.class.getCanonicalName() );

    public static final String DEFAULT_TEMPLATE_EXTENSION = ".js";

    protected final ScriptEngineManager engineManager = new ScriptEngineManager();

    protected ScriptEngine scriptEngine;

    private final Map<String, CompiledScript> scripts = new HashMap<>();


    public ScriptManager( String name )
    {
        try
        {
            scriptEngine = engineManager.getEngineByName( name );
        }
        catch ( Throwable e )
        {
            e.printStackTrace();
        }
    }


    public void clearScripts()
    {
        if ( !scripts.isEmpty() )
        {
            final Level level = Level.DEBUG;

            if ( logger.isEnabledFor( level ) )
            {
                logger.log( level, "Clearing cache: " + scripts.keySet() );
            }
        }

        scripts.clear();
    }


    public Object eval( String script, Map<String, Object> bindings ) throws ScriptException
    {
        final ScriptContext context = createScriptContext();

        context.getBindings( ScriptContext.ENGINE_SCOPE ).putAll( bindings );

        return eval( script, context );
    }


    public Object eval( String script, ScriptContext context ) throws ScriptException
    {
        return scriptEngine.eval( script, context );
    }


    /**
     * Create a new ScriptContext with a new ENGINE_SCOPE Bindings backed by a
     * nashorn Global scope.
     *
     * @return a new ScriptContext with a new ENGINE_SCOPE Bindings backed by a
     *         nashorn Global scope.
     */
    public ScriptContext createScriptContext()
    {
        ScriptContext sc = new SimpleScriptContext();
        sc.setBindings( scriptEngine.createBindings(), ScriptContext.ENGINE_SCOPE );
        return sc;
    }


    /**
     * Get the current context of the ScriptEngine.
     *
     * @return the current context of the ScriptEngine.
     */
    public ScriptContext getScriptContext()
    {
        return scriptEngine.getContext();
    }


    public Bindings createBindings()
    {
        return scriptEngine.createBindings();
    }


    public Object evalUri( String script, Map<String, Object> bindings ) throws ScriptException
    {
        final ScriptContext context = createScriptContext();

        context.getBindings( ScriptContext.ENGINE_SCOPE ).putAll( bindings );

        return evalUri( script, context );
    }


    public Object evalUri( final String uri, ScriptContext context ) throws ScriptException
    {
        // if no period then tack a default extension on the end
        final int lastIndexOfPeriod = uri.lastIndexOf( '.' );
        final String newUri = ( lastIndexOfPeriod > -1 ) ? uri : ( uri + DEFAULT_TEMPLATE_EXTENSION );


        // find
        if ( !scripts.containsKey( newUri ) )
        {
            loadScript( newUri );
        }

        // render
        return scripts.get( newUri ).eval( context );
    }


    /**
     * Only want each script loaded once, by a single thread.
     *
     * @param uri
     */
    private synchronized void loadScript( String uri )
    {
        if ( scripts.containsKey( uri ) )
        {
            return;
        }

        // find
        if ( !scripts.containsKey( uri ) )
        {
            // build and cache
            scripts.put( uri,
                         compileScript(
                         StreamTools.readUrl(
                                    StreamTools.getLocalFileURL( getClass(), uri ) ) ) );

            {
                final Level level = Level.DEBUG;

                if ( logger.isEnabledFor( level ) )
                {
                    logger.log( level, "Loaded template: " + uri );
                }
            }
        }
    }


    public CompiledScript compileScript( final String script )
    {
        try
        {
            return ( (Compilable) scriptEngine ).compile( script );
        }
        catch ( ScriptException e )
        {
            throw new RuntimeException( "Error compiling script: " + script, e );
        }
    }

}
