package com.brentcroft.gtd.adapter.model.fx;

import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.scene.web.WebView;

/**
 * Created by Alaric on 15/07/2017.
 */
@SuppressWarnings( "restriction" )
public class FxWebViewGuiObject extends FxParentGuiObject< WebView >
{
	public FxWebViewGuiObject( WebView t, Gob parent, GuiObjectConsultant< WebView > guiObjectConsultant, CameraObjectManager hgom )
	{
		super( t, parent, guiObjectConsultant, hgom );
	}
}
