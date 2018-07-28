package com.brentcroft.gtd.cucumber;


import com.brentcroft.gtd.js.context.Context;
import com.brentcroft.util.FileUtils;
import cucumber.runtime.CucumberBackend;
import java.io.File;
import java.util.List;
import org.apache.log4j.Logger;

import static java.lang.String.format;

/**
 * Registers step definitions and hooks defined in javascript object files.<p/>
 * <p>
 * Each Javascript object file specified by glue path is inserted into a Javascript template which is then evaluated.<p/>
 * <p>
 * The Javascript object files must present an array of step definition objects where each object has a:<br/>
 * <ol>
 * <li>"name" property containing a regular expression - a step selector.</li>
 * <li>"step" property containing a function - with arguments corresponding to groups in the step selector.</li>
 * </ol>
 * <p>
 * For example:<br/>
 * <pre>
 * [
 *   {
 *     "name": "I wait for (\\d+(?:\\.\\d+)) second(?:s)",
 *     "step": function( delaySeconds ) {
 *       waiter.delay( delaySeconds );
 *     }
 *   }
 * ]
 *
 * </pre>
 * <p>
 * <p>
 * <p>
 * Each script object file is injected into the template (replacing the token CUCUMBER_STEPS_ARRAY) and then template is evaluated.<p/>
 * <p>
 * Code in the template is applied to each of the step definition objects to construct a CucumberStepDefinition
 * <pre>
 * var steps = CUCUMBER_STEPS_ARRAY;
 * var Pattern = java.util.regex.Pattern;
 * var CucumberStepDefinition = " + CucumberStepDefinition.class.getName() + ";
 * for ( index in steps ) {
 * var step = steps[ index ];
 * if ( step.name && step.step ) {
 * try {
 * backend.addStepDefinition( new CucumberStepDefinition( Pattern.compile( step.name ), step ) );
 * } catch ( e ) {
 * print( 'Error adding step definition [' + step.name + ']: ' + e );
 * }
 * }
 * }
 * </pre>
 */
public class CucumberMethodScanner
{
    private final static Logger logger = Logger.getLogger( CucumberMethodScanner.class );

    public final static String key = "CUCUMBER_STEPS_ARRAY";

    /**
     * Note that "backend" has to be provided in the context
     */
    public final static String template = "\n"
                                          + "var steps = CUCUMBER_STEPS_ARRAY;\n"
                                          + "var Pattern = java.util.regex.Pattern;\n"
                                          + "var CucumberStepDefinition = " + CucumberStepDefinition.class.getName() + ";\n"
                                          + "for ( index in steps )\n"
                                          + "{\n"
                                          + "    var step = steps[ index ];\n"
                                          + "    \n"
                                          + "    if ( step.name && step.step )"
                                          + "    {\n"
                                          + "      try\n"
                                          + "      {\n"
                                          + "        backend.addStepDefinition( \n"
                                          + "            new CucumberStepDefinition( \n"
                                          + "                Pattern.compile( step.name ), \n"
                                          + "                step\n"
                                          + "            ) \n"
                                          + "        );\n"
                                          + "      }\n"
                                          + "      catch ( e )\n"
                                          + "      {\n"
                                          + "        print( 'Error adding step definition [' + step.name + ']: ' + e );\n"
                                          + "      }\n"
                                          + "   }\n"
                                          + "}";


    private final Context context;
    private String adapter;

    public CucumberMethodScanner( Context context )
    {
        this.context = context;
        this.adapter = template;
    }


    public CucumberMethodScanner( Context context, File stepAdapter )
    {
        this( context );

        adapter = FileUtils.getFileOrResourceAsString( null, stepAdapter.getAbsolutePath() );
    }


    /**
     * Registers step definitions and hooks.
     *
     * @param backend   the backend where stepdefs and hooks will be registered
     * @param gluePaths where to look
     */
    public void scan( CucumberBackend backend, List< String > gluePaths )
    {
        for ( String gluePath : gluePaths )
        {
            for ( String path : gluePath.split( "\\s*,\\s*" ) )
            {
                scan( backend, path );
            }
        }
    }

    /**
     * Registers step definitions and hooks.
     *
     * @param backend  the backend where step definitions and hooks will be registered.
     * @param gluePath a path to a file containing step definitions.
     */
    public void scan( CucumberBackend backend, String gluePath )
    {
        String definitions = FileUtils.getFileOrResourceAsString( null, gluePath );

        int p = adapter.indexOf( key );

        String script = ( p < 0 )
                ? definitions
                : adapter.substring( 0, p )
                  + definitions
                  + adapter.substring( p + key.length() );

        logger.info(
                format( "Ingesting cucumber steps from [%s].",
                        gluePath ) );

        try
        {
            context.execute( script );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( format( "Error ingesting steps from [%s].", gluePath ), e );
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug(
                    format( "Ingested cucumber steps from [%s].",
                            gluePath ) );
        }
    }
}
