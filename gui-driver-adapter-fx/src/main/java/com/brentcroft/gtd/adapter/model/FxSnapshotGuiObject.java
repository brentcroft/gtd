package com.brentcroft.gtd.adapter.model;

import java.util.List;
import java.util.stream.Collectors;

import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.camera.FxSnapshot;
import com.brentcroft.util.xpath.gob.Gob;

public class FxSnapshotGuiObject< T extends FxSnapshot > extends DefaultGuiObject< T >
{

	public FxSnapshotGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
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
