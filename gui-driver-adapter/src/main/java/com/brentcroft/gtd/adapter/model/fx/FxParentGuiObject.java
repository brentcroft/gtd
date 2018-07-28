package com.brentcroft.gtd.adapter.model.fx;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.List;
import java.util.stream.Collectors;
import javafx.scene.Parent;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxParentGuiObject< T extends Parent > extends FxNodeGuiObject< T >
{
    public FxParentGuiObject( T t, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager hgom )
    {
        super( t, parent, guiObjectConsultant, hgom );
    }

    @Override
    public boolean hasChildren()
    {
        return ! getObject()
                .getChildrenUnmodifiable()
                .isEmpty();
    }


    @Override
    public List< GuiObject > loadChildren()
    {
        return getObject()
                .getChildrenUnmodifiable()
                .stream()
                .map( ( child ) -> getManager().adapt( child, this ) )
                .collect( Collectors.toList() );
    }
}
