package com.brentcroft.gtd.adapter.model.w3c;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLButtonElement;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

public class W3cHTMLButtonElement< T extends HTMLButtonElement > extends W3cHTMLElementGuiObject< T > implements GuiObject.Click
{
	public W3cHTMLButtonElement( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
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
		FXUtils.reflectiveCallRunAndWait( getObject(), "click" );
	}
}
