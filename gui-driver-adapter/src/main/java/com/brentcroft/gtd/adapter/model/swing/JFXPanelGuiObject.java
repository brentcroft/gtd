package com.brentcroft.gtd.adapter.model.swing;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.embed.swing.JFXPanel;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JFXPanelGuiObject< T extends JFXPanel > extends JComponentGuiObject< T >
{
    public JFXPanelGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }

    @Override
    public boolean hasChildren()
    {
        JFXPanel item = getObject();

        return super.hasChildren()
               || ( item.getScene() != null
                    && item.getScene().getRoot() != null );
    }


    @Override
    public List< GuiObject > loadChildren()
    {
        List< GuiObject > children = new ArrayList<>();

        children.addAll( super.loadChildren() );

        Optional.of( getObject().getScene() )
                .ifPresent( scene -> Optional.of( scene.getRoot() )
                        .ifPresent( root -> children.add( getManager().adapt( root, this ) ) ) );

        return children;
    }
}
