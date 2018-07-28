package cucumber.runtime;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import com.brentcroft.gtd.cucumber.CucumberHookDefinition;
import com.brentcroft.gtd.cucumber.CucumberMethodScanner;
import com.brentcroft.gtd.cucumber.CucumberSnippet;
import com.brentcroft.gtd.cucumber.CucumberStepDefinition;

import com.brentcroft.gtd.js.context.Context;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.snippets.FunctionNameGenerator;
import cucumber.runtime.snippets.SnippetGenerator;
import gherkin.formatter.model.Step;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class CucumberBackend implements Backend
{
    private SnippetGenerator snippetGenerator = new SnippetGenerator( new CucumberSnippet() );

    private Context context;

    private CucumberMethodScanner methodScanner;

    private Glue glue;


    /**
     * The constructor called by reflection by default.
     *
     * Therefore must intercept and call withContext
     */
    public CucumberBackend( ResourceLoader loader )
    {
    }

    public CucumberBackend( Context context )
    {
        this.context = context;
        methodScanner = new CucumberMethodScanner( context );
    }

    public CucumberBackend(Context context, String adapterPath )
    {
        this.context = context;
        methodScanner = new CucumberMethodScanner(
                context,
                new File( adapterPath ) );
    }

    public CucumberBackend withContext( Context context, String adapterPath)
    {
        this.context = context;
        methodScanner = new CucumberMethodScanner(
                context,
                new File( adapterPath ) );
        return this;
    }

    public CucumberBackend withContext( Context context)
    {
        this.context = context;
        methodScanner = new CucumberMethodScanner( context );
        return this;
    }


    @Override
    public void loadGlue( Glue glue, List< String > gluePaths )
    {
        this.glue = glue;

        // TODO: does this remove old glue (i.e. so renamed steps don't collide on pattern)?
        glue.removeScenarioScopedGlue();

        methodScanner.scan( this, gluePaths );
    }


    @Override
    public void setUnreportedStepExecutor( UnreportedStepExecutor executor )
    {
        // Not used here yet
    }

    @Override
    public void buildWorld()
    {
        //
    }

    @Override
    public void disposeWorld()
    {
        //
    }

    @Override
    public String getSnippet( Step step, FunctionNameGenerator functionNameGenerator )
    {
        return snippetGenerator.getSnippet( step, functionNameGenerator );
    }

    public void addStepDefinition( Pattern pattern, ScriptObjectMirror method )
    {
        addStepDefinition(
                new CucumberStepDefinition(
                        pattern,
                        method ) );
    }


    public void addStepDefinition( StepDefinition stepDefinition )
    {
        try
        {
            glue.addStepDefinition( stepDefinition );
        }
        catch ( DuplicateStepDefinitionException e )
        {
            throw e;
        }
        catch ( Throwable e )
        {
            throw new CucumberException( e );
        }
    }

    void addHook( ScriptObjectMirror method, Pattern pattern, boolean before )
    {
        if ( before )
        {
            glue.addBeforeHook( new CucumberHookDefinition( method, pattern ) );
        }
        else
        {
            glue.addAfterHook( new CucumberHookDefinition( method, pattern ) );
        }
    }
}
