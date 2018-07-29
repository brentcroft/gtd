package com.brentcroft.gtd.adapter.model.swing;

import javax.swing.JEditorPane;

import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JEditorPaneGuiObject< T extends JEditorPane > extends JTextComponentGuiObject< T >
{
	public JEditorPaneGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}
}
