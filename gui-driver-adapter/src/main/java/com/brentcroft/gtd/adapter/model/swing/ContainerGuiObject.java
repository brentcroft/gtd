package com.brentcroft.gtd.adapter.model.swing;

import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.util.xpath.gob.Gob;
import java.awt.Container;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Alaric on 14/07/2017.
 */
public class ContainerGuiObject< T extends Container > extends ComponentGuiObject< T >
{
    public ContainerGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }

    @Override
    public boolean hasChildren()
    {
        return getObject().getComponentCount() > 0;
    }

    @Override
    public List< GuiObject > loadChildren()
    {
        return Arrays.asList( getObject().getComponents() )
                .stream()
                .map( child -> getManager().adapt( child, this ) )
                .collect( Collectors.toList() );
    }

}
