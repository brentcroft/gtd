package com.brentcroft.gtd.adapter.model;

import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.camera.Snapshot;
import com.brentcroft.util.xpath.gob.Gob;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.w3c.dom.Element;

/**
 * Created by Alaric on 15/07/2017.
 */
public class SnapshotGuiObject< T extends Snapshot > extends DefaultGuiObject< T >
{
    public SnapshotGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }


    @Override
    public boolean hasChildren()
    {
        return true;
    }

    @Override
    public List< GuiObject > loadChildren()
    {
        return getObject()
                .getChildren()
                .stream()
                .map( child -> getManager().adapt( child, this ) )
                .collect( Collectors.toList() );
    }
}
