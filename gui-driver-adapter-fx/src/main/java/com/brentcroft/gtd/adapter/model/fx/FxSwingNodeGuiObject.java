package com.brentcroft.gtd.adapter.model.fx;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.embed.swing.SwingNode;

/**
 * Created by Alaric on 15/07/2017.
 */
@SuppressWarnings( "restriction" )
public class FxSwingNodeGuiObject< T extends SwingNode > extends FxNodeGuiObject< T >
{
	public FxSwingNodeGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public boolean hasChildren()
	{
		return null != getObject().getContent();
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		JComponent content = getObject().getContent();

		if ( content == null )
		{
			return null;
		}

		List< GuiObject< ? > > list = new ArrayList<>();

		list.add( getManager().adapt( content, this ) );

		return list;
	}
}
