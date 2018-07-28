package com.brentcroft.gtd.js.context;

import com.brentcroft.gtd.js.driver.JSGuiSession;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.Bindings;
import java.io.File;
import java.util.Collection;
import java.util.Properties;

/**
 * Created by Alaric on 07/03/2017.
 */
public abstract class Context
{
    private final Properties properties;

    public Context( Properties parentProperties )
    {
        properties = new Properties( parentProperties );
    }

    public Properties getProperties()
    {
        return properties;
    }

    public abstract void addSession( String key, JSGuiSession session );

    public abstract Collection< JSGuiSession > getSessions();

    public abstract JSGuiSession getSession( String key );

    public abstract Object execute( String script );

    public abstract Bindings getBindings();

    public abstract Bindings getBindingsGlobal();

    public abstract void refreshSessionModel( JSGuiSession session );

    public abstract void activateModels();

    public abstract ScriptObjectMirror generateModel( JSGuiSession session, String modelJson );

    public abstract File getRoot();

    public abstract Modeller getModeller();
}
