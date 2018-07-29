package com.brentcroft.gtd.adapter.model.fx;

import java.util.List;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.scene.control.Accordion;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxAccordionGuiObject< T extends Accordion > extends FxControlGuiObject< T >
{
	public FxAccordionGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public boolean hasChildren()
	{
		return !getObject().getPanes().isEmpty();
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		List< GuiObject< ? > > children = super.loadChildren();

		getObject()
				.getPanes()
				.forEach( child -> children.add( getManager().adapt( child, this ) ) );

		return children;
	}
}
