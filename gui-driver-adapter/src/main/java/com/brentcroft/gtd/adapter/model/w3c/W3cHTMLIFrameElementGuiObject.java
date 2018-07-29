package com.brentcroft.gtd.adapter.model.w3c;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLIFrameElement;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 15/07/2017.
 */
public class W3cHTMLIFrameElementGuiObject< T extends HTMLIFrameElement > extends W3cHTMLElementGuiObject< T >
{
	public W3cHTMLIFrameElementGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public boolean hasChildren()
	{
		return null != getObject().getContentDocument();
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		List< GuiObject< ? > > children = new ArrayList<>();

		Document content = getObject().getContentDocument();

		if ( content != null )
		{
			for ( Node child = content.getFirstChild(); child != null; child = child.getNextSibling() )
			{
				if ( (child != null) && (child instanceof HTMLElement) )
				{
					children.add( getManager().adapt( child, this ) );
				}
			}
		}

		return children;
	}
}
