package com.brentcroft.gtd.inspector;

import com.brentcroft.gtd.js.context.ContextUnit;
import java.io.File;
import javafx.stage.Stage;

/**
 * Created by Alaric on 27/03/2017.
 */
public interface ContextManager
{
    ContextUnit getUnit();

    Stage getPrimaryStage();

    File getCurrentDirectory();

    void addNewUnitListener( NewUnitListener newUnitListener );

    interface NewUnitListener
    {
        void adviseNewUnit( ContextUnit unit );
    }
}
