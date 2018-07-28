package com.brentcroft.util.tools;

import static org.junit.Assert.fail;

import java.util.logging.Logger;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore( "Only for manual use; requires JS environment" )
public class ScriptManagerTest
{
    private ScriptManager scriptManager;

    private MapBindings globalObjects;

    private MapBindings localObjects;


    @Before
    public void setUp()
    {
        scriptManager = new ScriptManager( "js" );

        globalObjects = new MapBindings();
        localObjects = new MapBindings();
    }

    @Test
    public void testGlobalScope()
    {
        globalObjects.withEntries(
                                   "red", 100,
                                   "blue", 200,
                                   "logger", Logger.getLogger( "GLOBAL" )
                     );
        localObjects.withEntries(
                                  "green", 300,
                                  "yellow", 400,
                                  "logger", Logger.getLogger( "LOCAL" )
                    );


        String script = "var fred = red + blue + green + yellow + '0'; logger.info( 'fred: ' + fred ); fred";


        final ScriptContext context = scriptManager.createScriptContext();

        context.setBindings( globalObjects, ScriptContext.GLOBAL_SCOPE );
        context.setBindings( localObjects, ScriptContext.ENGINE_SCOPE );

        try
        {
            eval( script, context );
        }
        catch ( ScriptException e )
        {
            e.printStackTrace();

            fail( "Unexpected Exception: " + e );
        }
    }

    @Test
    public void testLocalScope()
    {
        globalObjects.withEntries(
                                   "red", 100,
                                   "blue", 200,
                                   "logger", Logger.getLogger( "GLOBAL" )
                     );
        localObjects.withEntries(
                                  "green", 300,
                                  "yellow", 400,
                                  "logger", Logger.getLogger( "LOCAL" )
                    );


        String script = "var fred = red + blue + green + yellow + '0'; logger.info( 'fred: ' + fred ); fred";


        final ScriptContext context = scriptManager.createScriptContext();

        context.setBindings( globalObjects, ScriptContext.GLOBAL_SCOPE );
        context.setBindings( localObjects, ScriptContext.ENGINE_SCOPE );

        try
        {
            eval( script, context );
        }
        catch ( ScriptException e )
        {
            e.printStackTrace();

            fail( "Unexpected Exception: " + e );
        }
    }


    @Test
    public void testTripleScopeDoesntRequireThisToAccessGeneratedJsObject()
    {
        globalObjects.withEntries(
                                   "red", 100,
                                   "blue", 200,
                                   "logger", Logger.getLogger( "GLOBAL" )
                     );
        localObjects.withEntries(
                                  "green", 300,
                                  "yellow", 400,
                                  "logger", Logger.getLogger( "LOCAL" )
                    );

        String script = "var fred = red + blue + green + yellow + '0'; var bob = 28; logger.info( 'triple-fred: ' + fred ); fred";


        final ScriptContext context = scriptManager.createScriptContext();

        context.setBindings( globalObjects, ScriptContext.GLOBAL_SCOPE );
        context.setBindings( localObjects, ScriptContext.ENGINE_SCOPE );

        try
        {
            eval( script, context );
        }
        catch ( ScriptException e )
        {
            e.printStackTrace();

            fail( "Unexpected Exception: " + e );
        }


        final MapBindings freshObjects = new MapBindings()
                                                          .withEntries(
                                                                        "black", 500,
                                                                        "white", 600,
                                                                        "logger", Logger.getLogger( "FRESH" )
                                                          );


        /*
         * Forced to use "this.fred" to access "fred" [see, just below here]
         * created in localObjects by evaluating previous script.
         */
        /**
         * Not sure whether being found in local scope really, since local has
         * global as parent MapBindings
         */
        script = "var bloggs = black + white + green + yellow + '_fred:' + fred; var bob = bloggs + '_bob:' + bob; logger.info( 'triple-bloggs: ' + bob ); bob";

        // switch up a level
        context.setBindings( localObjects, ScriptContext.GLOBAL_SCOPE );
        context.setBindings( freshObjects, ScriptContext.ENGINE_SCOPE );

        try
        {
            eval( script, context );
        }
        catch ( ScriptException e )
        {
            e.printStackTrace();

            fail( "Unexpected Exception: " + e );
        }
    }


    private void eval( String script, ScriptContext context ) throws ScriptException
    {
        Object result = scriptManager.eval( script, context );


        System.out.println(
                  "result: " + result +
                          "\n global: " + globalObjects +
                          "\n local: " + localObjects );


    }
}
