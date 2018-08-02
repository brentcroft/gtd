package com.brentcroft.gtd.camera.model.swt;

import java.util.Map;

import org.eclipse.swt.widgets.TabFolder;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.camera.model.swt.CompositeGuiObject.IndexCompositeGuiObject;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 * 
 */
public class TabFolderGuiObject< T extends TabFolder > extends IndexCompositeGuiObject< T >
{
	public TabFolderGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager, getMethods( go ) );
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		addIndexAction( element, options );
		addTabsAction( element, options );
	}
}
