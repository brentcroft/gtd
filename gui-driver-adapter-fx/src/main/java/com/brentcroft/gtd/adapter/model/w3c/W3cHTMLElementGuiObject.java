package com.brentcroft.gtd.adapter.model.w3c;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLElement;

import com.brentcroft.gtd.adapter.model.DefaultGuiObject;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Attribute;
import com.brentcroft.util.xpath.gob.Gob;
import com.sun.webkit.dom.HTMLElementImpl;

/**
 * Created by Alaric on 15/07/2017.
 */
public class W3cHTMLElementGuiObject< T extends HTMLElement > extends DefaultGuiObject< T >
{
	// private final static transient Logger logger = Logger.getLogger(
	// W3cHTMLElementGuiObject.class );

	public W3cHTMLElementGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public int[] getLocation()
	{
		HTMLElementImpl hi = ( HTMLElementImpl ) getObject();

		return new int[] {
				Double.valueOf( hi.getOffsetLeft() ).intValue(),
				Double.valueOf( hi.getOffsetTop() ).intValue(),
				Double.valueOf( hi.getClientWidth() ).intValue(),
				Double.valueOf( hi.getClientHeight() ).intValue() };
	}

	// TODO: like to remove this
	private int[] getOffset( HTMLElementImpl el )
	{
		int x = 0;
		int y = 0;
		int w = Double.valueOf( el.getClientWidth() ).intValue();
		int h = Double.valueOf( el.getClientHeight() ).intValue();

		while ( el != null )
		{
			x += el.getOffsetLeft() - el.getScrollLeft();
			y += el.getOffsetTop() - el.getScrollTop();

			el = el.getOffsetParent() instanceof HTMLElementImpl
					? ( HTMLElementImpl ) el.getOffsetParent()
					: null;
		}
		return new int[] { x, y, w, h };
	}

	@Override
	public int[] getObjectLocationOnScreen()
	{
		HTMLElementImpl hi = ( HTMLElementImpl ) getObject();

		return getOffset( hi );
	}

	/**
	 * This provides a suitable String for an (HTML) element object.<br/>
	 * <p>
	 * It takes the client name lower case (whereas nodeName is upper case).<br/>
	 *
	 * @return the client name
	 */
	@Override
	public String getComponentTag()
	{
		return getObject().getLocalName();
	}

	@Override
	public List< Attribute > readAttributes()
	{
		List< Attribute > attributes = super.readAttributes();

		NamedNodeMap nnm = getObject().getAttributes();

		for ( int i = 0, n = nnm.getLength(); i < n; i++ )
		{
			org.w3c.dom.Attr w3cAttr = ( org.w3c.dom.Attr ) nnm.item( i );

			if ( w3cAttr.getNamespaceURI() == null )
			{
				// TODO: what should happen in this case
				if ( "xmlns".equals( w3cAttr.getLocalName() ) )
				{
					continue;
				}

				attributes.add( attributeForNameAndValue(
						w3cAttr.getLocalName(),
						DataLimit.MAX_TEXT_LENGTH
								.maybeTruncate( w3cAttr.getValue() ) ) );
			}
			else
			{
				attributes.add( attributeForNSNameAndValue(
						w3cAttr.getNamespaceURI(),
						w3cAttr.getPrefix(),
						w3cAttr.getLocalName(),
						DataLimit.MAX_TEXT_LENGTH
								.maybeTruncate( w3cAttr.getValue() ) ) );
			}
		}

		return attributes;
	}

	@Override
	public boolean hasChildren()
	{
		return getObject().hasChildNodes();
	}

	/**
	 * Load and adapt HTMLElement and Text children.
	 *
	 * @return a list of adaptees (GuiObjects)
	 */
	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		List< GuiObject< ? > > children = new ArrayList<>();

		for ( Node child = getObject().getFirstChild(); child != null; child = child.getNextSibling() )
		{
			if ( child instanceof HTMLElement )
			{
				children.add( getManager().adapt( child, this ) );
			}
			else if ( child instanceof org.w3c.dom.Text )
			{
				org.w3c.dom.Text text = ( org.w3c.dom.Text ) child;

				String data = text.getData();

				if ( (data != null) && !data.trim().isEmpty() )
				{
					children.add( getManager().adapt( child, this ) );
				}
			}
		}

		return children;
	}
}
