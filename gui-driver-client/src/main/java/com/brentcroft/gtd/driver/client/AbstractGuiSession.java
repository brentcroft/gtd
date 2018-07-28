package com.brentcroft.gtd.driver.client;

import com.brentcroft.util.CommentedProperties;
import com.brentcroft.util.TextUtils;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public abstract class AbstractGuiSession implements GuiSession
{
    private State state = State.UNKNOWN;

    private CommentedProperties sessionProperties = new CommentedProperties();

    private double echoTimeoutSeconds = 3.0;
    private double loginTimeoutSeconds = 30.0;

    private GuiAdapter guiAdapter = null;
    protected GuiLauncher launcher = null;
    private GuiLocalDriver guiDriver = null;


    public CommentedProperties getProperties()
    {
        return sessionProperties;
    }

    public void setProperties( CommentedProperties p )
    {
        sessionProperties = p;
    }

    public void setDriver( GuiLocalDriver driver )
    {
        this.guiDriver = driver;
    }

    public void setLauncher( GuiLauncher launcher )
    {
        this.launcher = launcher;
    }

    public String toString()
    {
        return format(
                "adapter=[%s]%n" +
                "echoTimeoutSeconds=[%s]%n" +
                "loginTimeoutSeconds=[%s]%n" +
                "driver=%s",
                this.guiAdapter,
                this.echoTimeoutSeconds,
                this.loginTimeoutSeconds,
                TextUtils.indent( getDriver().toString() )
        );
    }


    public void setLoginTimeoutSeconds( double loginTimeoutSeconds )
    {
        this.loginTimeoutSeconds = loginTimeoutSeconds;
    }

    public double getEchoTimeoutSeconds()
    {
        return echoTimeoutSeconds;
    }

    public void setEchoTimeoutSeconds( double seconds )
    {
        this.echoTimeoutSeconds = seconds;
    }

    public double getLoginTimeoutSeconds()
    {
        return loginTimeoutSeconds;
    }


    public GuiAdapter getGuiAdapter()
    {
        return guiAdapter;
    }

    public void setGuiAdapter( GuiAdapter adapter )
    {
        this.guiAdapter = adapter;
    }

    public GuiLauncher getLauncher()
    {
        return launcher;
    }

    public GuiLocalDriver getDriver()
    {
        return guiDriver;
    }


    @Override
    public State getState()
    {
        return state;
    }

    protected void changeState( State newState )
    {
        State oldState = state;

        this.state = newState;

        for ( StateListener sl : listeners )
        {
            sl.stateChange( oldState, newState );
        }
    }


    public interface StateListener
    {
        void stateChange( State oldState, State newState );
    }

    private List< StateListener > listeners = new ArrayList<>();

    public void addStateListener( StateListener l )
    {
        listeners.add( l );
    }

}