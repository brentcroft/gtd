package com.brentcroft.gtd.adapter.model.fx;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.scene.control.TitledPane;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxTitledPaneGuiObject< T extends TitledPane > extends FxLabeledGuiObject< T >
{
	public FxTitledPaneGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		List< GuiObject< ? > > children = new ArrayList<>();

		Optional
				.of( getObject().getContent() )
				.ifPresent( child -> children.add( getManager().adapt( child, this ) ) );

		return children;
	}

}
