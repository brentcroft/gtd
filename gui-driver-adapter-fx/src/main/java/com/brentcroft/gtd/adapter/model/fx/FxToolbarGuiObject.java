package com.brentcroft.gtd.adapter.model.fx;

import java.util.List;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.scene.control.ToolBar;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxToolbarGuiObject< T extends ToolBar > extends FxControlGuiObject< T >
{
	public FxToolbarGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
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
		List< GuiObject< ? > > children = super.loadChildren();

		getObject()
				.getItems()
				.forEach( child -> children.add( getManager().adapt( child, this ) ) );

		return children;
	}
}
