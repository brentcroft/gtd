package com.brentcroft.gtd.adapter.model.fx;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.ButtonBar;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxButtonBarGuiObject< T extends ButtonBar > extends FxControlGuiObject< T >
{
    public FxButtonBarGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }

    @Override
    public boolean hasChildren()
    {
        return ! getObject().getButtons().isEmpty();
    }

    @Override
    public List< GuiObject > loadChildren()
    {
        List< GuiObject > children = new ArrayList<>();

        getObject()
                .getButtons()
                .forEach( child -> children.add( getManager().adapt( child, this ) ) );

        children.addAll( super.loadChildren() );

        return children;
    }
}
