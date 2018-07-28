package com.brentcroft.gtd.driver.client;

import java.util.Properties;

/**
 * Created by Alaric on 20/10/2016.
 */
public interface GuiSession
{
    Properties getProperties();

    GuiSession withCheckInstanceTimeout( double seconds );

    void setOnStarted( String onStartedScript );

    void start();

    void stop();

    GuiSession login();

    boolean isLoggedIn();

    GuiSession logout();

    GuiDriver getDriver();

    GuiLauncher getLauncher();

    GuiAdapter getGuiAdapter();


    State getState();

    enum State
    {
        UNKNOWN,
        STARTING,
        STARTED,
        STOPPING,
        STOPPED,
        FAILED
    }
}
