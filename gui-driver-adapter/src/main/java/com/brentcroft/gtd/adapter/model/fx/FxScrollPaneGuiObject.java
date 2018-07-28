package com.brentcroft.gtd.adapter.model.fx;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.ScrollPane;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxScrollPaneGuiObject< T extends ScrollPane > extends FxControlGuiObject< T >
{
    public FxScrollPaneGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }

    @Override
    public boolean hasChildren()
    {
        return getObject().getContent() != null;
    }

    @Override
    public List< GuiObject > loadChildren()
    {
        List< GuiObject > children = new ArrayList<>();

        Optional
                .of( getObject().getContent() )
                .ifPresent( child -> children.add( getManager().adapt( getObject().getContent(), this ) ) );

        children.addAll( super.loadChildren() );

        return children;
    }
}
