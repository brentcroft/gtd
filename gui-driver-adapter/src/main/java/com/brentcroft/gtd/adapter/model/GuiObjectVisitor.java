package com.brentcroft.gtd.adapter.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.brentcroft.gtd.driver.Backend;
import com.brentcroft.gtd.driver.ObjectLostException;
import com.brentcroft.util.TimeKeySequence;
import com.brentcroft.util.XmlUtils;

/**
 * Created by Alaric on 14/07/2017.
 */
public class GuiObjectVisitor implements ItemVisitor< GuiObject< ? > >
{
	final static TimeKeySequence VISITOR_KEY_SEQUENCE = TimeKeySequence.newSimpleTimeKeySequence();
	final String visitorKey = VISITOR_KEY_SEQUENCE.nextValue();

	/**
	 * The key used to store a GuiObject in an XML node UserData map.
	 */
	final String GUI_OBJECT_KEY = "GUI_OBJECT_KEY";
	final String VISITOR_KEY = "VISITOR";

	final String DUPLICATE_ATTRIBUTE = "duplicate";
	boolean registerDuplicates = true;

	protected final Map< Integer, Element > componentIds = new HashMap<>();

	private final Stack< Node > stack = new Stack<>();

	private final Map< String, Object > options;

	public GuiObjectVisitor( final Node element, final Map< String, Object > options )
	{
		stack.push( element );
		this.options = options;
	}

	@Override
	public boolean isDuplicate( GuiObject< ? > item )
	{
		try
		{
			return componentIds.containsKey( item.getObject().hashCode() );
		}
		catch ( ObjectLostException e )
		{
			return false;
		}
	}

	/**
	 * Create a new element in the document of the provided node with the provided
	 * tag name.
	 * <p>
	 * If the provided node is the document then
	 *
	 * @param node
	 * @param tag
	 * @return
	 */
	private Element createElement( Node node, String tag )
	{
		// getStats().insertingTag( tag );

		Document document = XmlUtils.getDocument( node );

		Element element = document.createElement( tag );

		return element;
	}

	@Override

	public ItemState open( GuiObject< ? > guiObject )
	{
		try
		{
			final String tag = guiObject.getComponentTag();

			Node parent = stack.peek();

			ItemState itemState = ItemState.INSERT;

			Element element = createElement( parent, tag );

			setVisitorKey( element );

			boolean duplicateObject = isDuplicate( guiObject );

			if ( duplicateObject )
			{
				itemState = ItemState.DUPLICATE;

				if ( !registerDuplicates )
				{
					return ItemState.DUPLICATE;
				}
			}

			parent.appendChild( element );

			// TODO: is this the correct place??
			if ( parent.getNodeType() == Node.DOCUMENT_NODE )
			{
				XmlUtils.addXmlnsPrefixNamespaceDeclaration( element, Backend.XML_NAMESPACE_TAG, Backend.XML_NAMESPACE_URI );

				XmlUtils.getDocument( element ).normalizeDocument();
			}

			if ( duplicateObject )
			{
				markAsDuplicate( element, guiObject );

				// never put duplicate on the stack
				return itemState;
			}

			componentIds.put( guiObject.getObject().hashCode(), element );

			// push on stack
			stack.push( element );

			return itemState;
		}
		catch ( ObjectLostException ole )
		{
			return ItemState.LOST;
		}
	}

	@Override
	public boolean close( GuiObject< ? > guiObject )
	{
		// pop off stack
		Node childElement = stack.pop();

		if ( childElement.getNodeType() == Node.ELEMENT_NODE )
		{
			childElement.setUserData( GUI_OBJECT_KEY, guiObject, null );

			// allows the parent to remodel its children
			guiObject.buildProperties( ( Element ) childElement, options );
		}

		return true;
	}

	public void markAsDuplicate( Element element, GuiObject< ? > guiObject )
	{
		// the duplicate master is the first occurrence
		// it has an @duplicate attribute (only once its duplicated)
		// that maintains the count of duplicates (including itself)
		// each new duplicate receives an @duplicate attribute
		// that is the index order of occurrence, beginning at 1.
		Element duplicateMasterElement = componentIds.get( guiObject.getObject().hashCode() );

		if ( duplicateMasterElement.hasAttribute( DUPLICATE_ATTRIBUTE ) )
		{
			String attrValue = duplicateMasterElement.getAttribute( DUPLICATE_ATTRIBUTE );

			int duplicates = Integer.valueOf( attrValue );

			element.setAttribute( DUPLICATE_ATTRIBUTE, "" + duplicates );
			duplicateMasterElement.setAttribute( DUPLICATE_ATTRIBUTE, "" + (duplicates + 1) );
		}
		else
		{
			element.setAttribute( DUPLICATE_ATTRIBUTE, "1" );
			duplicateMasterElement.setAttribute( DUPLICATE_ATTRIBUTE, "2" );
		}
	}

	private void setVisitorKey( Element visited )
	{
		visited.setUserData( VISITOR_KEY, visitorKey, null );
	}

	public Map< String, Object > getOptions()
	{
		return options;
	}
}
