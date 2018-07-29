package com.brentcroft.gtd.camera.model.swt;

import java.util.List;
import java.util.stream.Collectors;

import com.brentcroft.gtd.adapter.model.DefaultGuiObject;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.camera.SwtSnapshot;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 15/07/2017.
 */
public class SwtSnapshotGuiObject< T extends SwtSnapshot > extends DefaultGuiObject< T >
{
	public SwtSnapshotGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public boolean hasChildren()
	{
		return true;
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		return getObject()
				.getChildren()
				.stream()
				.map( child -> getManager().adapt( child, this ) )
				.collect( Collectors.toList() );
	}
}
