package com.brentcroft.gtd.js.driver.local;

import com.brentcroft.gtd.driver.client.GuiLocalDriver;
import java.util.ArrayList;
import java.util.List;


public abstract class AbstractJSGuiLocalDriver extends GuiLocalDriver implements JSGuiDriver
{
    private List< ModelChangeListener > listeners = new ArrayList< ModelChangeListener >();

    protected void notifyModelChangeListeners()
    {
        for ( ModelChangeListener l : listeners )
        {
            l.modelChanged();
        }
    }

    public void addModelChangeListener( ModelChangeListener l )
    {
        listeners.add( l );
    }

    public void removeModelChangeListener( ModelChangeListener l )
    {
        listeners.remove( l );
    }
}