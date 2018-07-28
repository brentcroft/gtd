package com.brentcroft.gtd.cucumber;

import java.util.Collection;
import java.util.regex.Pattern;

import cucumber.api.Scenario;
import cucumber.runtime.HookDefinition;
import gherkin.formatter.model.Tag;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

public class CucumberHookDefinition implements HookDefinition
{
    private final ScriptObjectMirror method;


    public CucumberHookDefinition(  ScriptObjectMirror method, Pattern pattern  )
    {
        if ( method == null )
        {
            throw new RuntimeException( "ScriptObjectMirror is null." );
        }
        if ( ! method.hasMember( "step" ) )
        {
            throw new RuntimeException( "ScriptObjectMirror must have a method named \"step\"." );
        }

        this.method = method;
    }

    @Override
    public String getLocation( boolean detail )
    {
        return null;
    }

    @Override
    public void execute( Scenario scenario ) throws Throwable
    {
        method.callMember( "step", scenario );
    }

    @Override
    public boolean matches( Collection< Tag > tags )
    {
        return false; //tagExpression.evaluate( tags );
    }

    @Override
    public int getOrder()
    {
        return 0; //order;
    }

    @Override
    public boolean isScenarioScoped()
    {
        return false;
    }

}
