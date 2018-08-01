package com.brentcroft.gtd.camera.model.swt;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.ToolBar;

import com.brentcroft.gtd.adapter.model.GuiObject;
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

    @Override
    public boolean hasChildren()
    {
        return onDisplayThread( getObject(), go -> go.getItemCount() ) > 0;
    }

    @Override
    public List< GuiObject<?> > loadChildren()
    {
        return Arrays
                .asList( onDisplayThread( getObject(), go -> go.getItems() ) )
                .stream()
                .map( child -> getManager().adapt( child, this ) )
                .collect( Collectors.toList() );
    }

}