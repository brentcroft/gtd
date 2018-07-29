package com.brentcroft.gtd.adapter.model.swing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JMenu;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JMenuGuiObject< T extends JMenu > extends AbstractButtonGuiObject< T >
{
	public JMenuGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public boolean hasChildren()
	{
		JMenu item = getObject();

		return item.getMenuComponentCount() > 0;
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		return Arrays
				.asList( getObject().getMenuComponents() )
				.stream()
				.map( mc -> getManager().adapt( mc, this ) )
				.collect( Collectors.toList() );
	}
}
