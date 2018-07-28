package com.brentcroft.gtd.driver;

import java.util.Properties;
import javax.script.ScriptException;

public interface GuiObjectService<T>
{
    /**
     * An implementation should execute the provided script
     * providing itself as a bound variable named "service".
     *
     * @param script
     * @throws ScriptException
     */
    Object configure( String script, GuiControllerMBean driver );

    void setProperties( Properties properties );

    String getReport();

    void shutdown();


    GuiControllerMBean getController();

    GuiObjectLocator<T> getGuiObjectLocator();

    GuiObjectManager<T> getObjectManager();
}
