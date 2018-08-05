package com.brentcroft.gtd.camera.model.swt;

import org.eclipse.swt.widgets.ToolBar;

import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 * 
 */
public class ToolBarGuiObject< T extends ToolBar > extends CompositeGuiObject< T >
{
    public ToolBarGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
            CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }
}
