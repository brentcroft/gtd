package com.brentcroft.gtd.inspector.panel;

import java.io.File;
import java.io.IOException;

/**
 * Created by adobson on 06/07/2016.
 */
public interface InspectorPane
{

    InspectorPane withScriptFile( File scriptFile ) throws IOException;

    File getScriptFile();

    String getText();

    void setScriptFile( File scriptFile );
}
