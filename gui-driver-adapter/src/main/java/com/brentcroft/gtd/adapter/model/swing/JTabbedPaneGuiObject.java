package com.brentcroft.gtd.adapter.model.swing;

import static com.brentcroft.util.XmlUtils.maybeSetElementAttribute;

import java.awt.Component;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JTabbedPane;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.SwingUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.Backend;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JTabbedPaneGuiObject< T extends JTabbedPane > extends JComponentGuiObject< T > implements GuiObject.Index
{
	public JTabbedPaneGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		buildTabbedPaneModel( element, options );

		// the last action in the list is the prime action
		addIndexAction( element, options );
		addTabsAction( element, options );
	}

	@Override
	public Integer getItemCount()
	{
		return getObject().getTabCount();
	}

	@Override
	public Integer getSelectedIndex()
	{
		return getObject().getSelectedIndex();
	}

	@Override
	public void setSelectedIndex( int index )
	{
		SwingUtils.maybeInvokeNowOnEventThread( () -> getObject().setSelectedIndex( index ) );
	}

	private void buildTabbedPaneModel( Element element, Map< String, Object > options )
	{
		T tabbedPane = getObject();

		NodeList childNodes = element.getChildNodes();

		for ( int i = 0, n = childNodes.getLength(); i < n; i++ )
		{
			Node childNode = childNodes.item( i );

			if ( !(childNode instanceof Element) )
			{
				continue;
			}

			Element childElement = ( Element ) childNode;

			Object candidate = childElement.getUserData( Backend.GUI_OBJECT_KEY );

			if ( (candidate == null) || !(candidate instanceof GuiObject) )
			{
				continue;
			}

			Object tabCandidate = (( GuiObject< ? > ) candidate).getObject();

			if ( !(tabCandidate instanceof Component) )
			{
				continue;
			}

			int tab = tabbedPane.indexOfComponent( ( Component ) tabCandidate );

			if ( tab < 0 )
			{
				continue;
			}

			String tip = tabbedPane.getToolTipTextAt( tab );
			String title = tabbedPane.getTitleAt( tab );

			maybeSetElementAttribute( childElement, "tab-title", title );
			maybeSetElementAttribute( childElement, "tab-tooltip", tip );

			addTabAction( childElement, options );
		}
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public List< AttrSpec< T > > loadAttrSpec()
	{
		if ( attrSpec == null )
		{
			attrSpec = super.loadAttrSpec();
			attrSpec.addAll( Arrays.asList( ( AttrSpec< T >[] ) Attr.values() ) );
		}

		return attrSpec;
	}

	enum Attr implements AttrSpec< JTabbedPane >
	{
		SIZE( "size", go -> "" + go.getTabCount() ),
		SELECTED_INDEX( "selected-index", go -> "" + go.getSelectedIndex() );

		final String n;
		final Function< JTabbedPane, String > f;

		Attr( String name, Function< JTabbedPane, String > f )
		{
			this.n = name;
			this.f = f;
		}

		@Override
		public String getName()
		{
			return n;
		}

		@Override
		public String getAttribute( JTabbedPane go )
		{
			return f.apply( go );
		}
	}

}
