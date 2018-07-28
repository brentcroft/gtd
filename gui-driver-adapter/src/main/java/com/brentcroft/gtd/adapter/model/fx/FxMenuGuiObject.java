package com.brentcroft.gtd.adapter.model.fx;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.Menu;

/**
 * Created by Alaric on 14/07/2017.
 */
public class FxMenuGuiObject< T extends Menu > extends FxMenuItemGuiObject< T >
{
    public FxMenuGuiObject( T t, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager hgom )
    {
        super( t, parent, guiObjectConsultant, hgom );
    }

    @Override
    public List< GuiObject > loadChildren()
    {
        return getObject()
                .getItems()
                .stream()
                .map( child -> getManager().adapt( child, this ) )
                .collect( Collectors.toList() );
    }
}
