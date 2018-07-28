package com.brentcroft.gtd.cucumber;

import cucumber.runtime.JdkPatternArgumentMatcher;
import cucumber.runtime.ParameterInfo;
import cucumber.runtime.StepDefinition;
import gherkin.I18n;
import gherkin.formatter.Argument;
import gherkin.formatter.model.Step;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Pattern;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.log4j.Logger;

import static java.lang.String.format;

/**
 * Implementation of a StepDefinition backed by a ScriptObjectMirror.<p/>
 * <p>
 * The ScriptObjectMirror must provide a <code>step( args )</code> method.
 */
public class CucumberStepDefinition implements StepDefinition
{
    private final static transient Logger logger = Logger.getLogger( CucumberStepDefinition.class );

    private final ScriptObjectMirror scriptObjectMirror;
    private final String methodName;
    private final Pattern pattern;
    private final JdkPatternArgumentMatcher argumentMatcher;


    public CucumberStepDefinition( Pattern pattern, ScriptObjectMirror method )
    {
        this( pattern, method, "step" );
    }

    public CucumberStepDefinition( Pattern pattern, ScriptObjectMirror scriptObjectMirror, String methodName )
    {
        if ( pattern == null )
        {
            throw new RuntimeException( "Pattern is null." );
        }
        else if ( scriptObjectMirror == null )
        {
            throw new RuntimeException( "ScriptObjectMirror is null." );
        }
        else if ( ! scriptObjectMirror.hasMember( methodName ) )
        {
            throw new RuntimeException( format( "ScriptObjectMirror does not have a method named \"%s\".", methodName ) );
        }

        this.scriptObjectMirror = scriptObjectMirror;
        this.pattern = pattern;
        this.methodName = methodName;

        this.argumentMatcher = new JdkPatternArgumentMatcher( pattern );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Ingested Step: " + pattern.toString() );
        }
    }


    /**
     * Call the member by name passing the supplied arguments.
     */
    public void execute( I18n i18n, Object[] args ) throws Throwable
    {
        scriptObjectMirror.callMember( methodName, args );
    }

    public List< Argument > matchedArguments( Step step )
    {
        return argumentMatcher.argumentsFrom( step.getName() );
    }

    public String getLocation( boolean detail )
    {
        return null;
    }

    @Override
    public Integer getParameterCount()
    {
        return null;
    }

    @Override
    public ParameterInfo getParameterType( int n, Type argumentType )
    {
        return null;
    }

    public boolean isDefinedAt( StackTraceElement e )
    {
        return false;
    }

    @Override
    public String getPattern()
    {
        return pattern.toString();
    }

    @Override
    public boolean isScenarioScoped()
    {
        return false;
    }
}
