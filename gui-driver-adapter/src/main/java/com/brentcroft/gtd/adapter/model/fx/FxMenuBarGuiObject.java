package com.brentcroft.gtd.adapter.model.fx;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.control.MenuBar;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxMenuBarGuiObject< T extends MenuBar > extends FxNodeGuiObject< T >
{
    public FxMenuBarGuiObject( T t, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager hgom )
    {
        super( t, parent, guiObjectConsultant, hgom );
    }

    @Override
    public List< GuiObject > loadChildren()
    {
        List< GuiObject > results = getObject()
                .getMenus()
                .stream()
                .map( child -> getManager().adapt( child, this ) )
                .collect( Collectors.toList() );


        // TODO: justify
        if ( super.hasChildren() )
        {
            results.addAll( super.loadChildren() );
        }
        return results;
    }
}
