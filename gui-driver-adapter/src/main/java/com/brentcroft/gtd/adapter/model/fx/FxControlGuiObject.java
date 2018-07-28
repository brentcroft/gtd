package com.brentcroft.gtd.adapter.model.fx;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.Control;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxControlGuiObject< T extends Control > extends FxParentGuiObject< T >
{
    public FxControlGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }

    @Override
    public List< GuiObject > loadChildren()
    {
        List< GuiObject > results = new ArrayList<>();

        Optional
                .ofNullable( getObject().getContextMenu() )
                .ifPresent( ( cm ) -> cm
                        .getItems()
                        .forEach( ( child ) -> results.add( getManager().adapt( child, this ) ) ) );

        return results;
    }
}
