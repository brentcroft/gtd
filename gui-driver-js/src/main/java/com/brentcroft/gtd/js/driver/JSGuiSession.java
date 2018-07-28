package com.brentcroft.gtd.js.driver;

import com.brentcroft.gtd.driver.client.GuiLocalSession;
import com.brentcroft.gtd.driver.client.GuiSession;
import com.brentcroft.gtd.js.context.Model;
import com.brentcroft.util.TextUtils;
import java.io.File;

import static java.lang.String.format;

public class JSGuiSession extends GuiLocalSession implements GuiSession
{

    private String name;
    private Model model;

    private File workingDirectory;

    public JSGuiSession( String name )
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public String toString()
    {
        return new StringBuffer( format( "name=[%s]", getName() ) )
                .append( "\n" + TextUtils.indent( super.toString(), "    " ) )
                .append( "\n    model:" )
                .append( "\n" + TextUtils.indent( "" + model, "      " ) )
                .toString();
    }

    public JSGuiLocalDriver getDriver()
    {
        return (JSGuiLocalDriver) super.getDriver();
    }

    public Model getModel()
    {
        return model;
    }

    public void setModel( Model model )
    {
        this.model = model;
    }

    public File getWorkingDirectory()
    {
        return workingDirectory;
    }

    public void setWorkingDirectory( File workingDirectory )
    {
        this.workingDirectory = workingDirectory;
    }
}
