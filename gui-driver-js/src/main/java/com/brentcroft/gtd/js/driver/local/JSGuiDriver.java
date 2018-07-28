package com.brentcroft.gtd.js.driver.local;

import com.brentcroft.gtd.driver.client.GuiDriver;

public interface JSGuiDriver extends GuiDriver
{
    void execute( String path, String script );

    void execute( String path, String script, double timeoutSeconds );

    interface ModelChangeListener
    {
        void modelChanged();
    }

    void addModelChangeListener( ModelChangeListener l );
}
