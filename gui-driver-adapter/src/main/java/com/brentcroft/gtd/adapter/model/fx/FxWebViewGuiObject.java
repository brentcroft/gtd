package com.brentcroft.gtd.adapter.model.fx;

import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.scene.web.WebView;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxWebViewGuiObject< T extends WebView > extends FxParentGuiObject< T >
{
	public FxWebViewGuiObject( T t, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager hgom )
	{
		super( t, parent, guiObjectConsultant, hgom );
	}
}
