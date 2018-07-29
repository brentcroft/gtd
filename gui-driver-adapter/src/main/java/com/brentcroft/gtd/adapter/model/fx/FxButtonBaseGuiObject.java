package com.brentcroft.gtd.adapter.model.fx;

import java.util.Map;

import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.scene.control.ButtonBase;

public class FxButtonBaseGuiObject< T extends ButtonBase > extends FxLabeledGuiObject< T > implements GuiObject.Click
{
	public FxButtonBaseGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		addClickAction( element, options );
	}

	@Override
	public void click()
	{
		FXUtils.maybeInvokeNowOnFXThread( getObject()::fire );
	}
}
