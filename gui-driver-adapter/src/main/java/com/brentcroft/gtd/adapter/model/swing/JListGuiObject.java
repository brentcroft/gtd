package com.brentcroft.gtd.adapter.model.swing;

import static com.brentcroft.gtd.adapter.model.DefaultGuiObject.Converter.maybeConvertValue;
import static com.brentcroft.gtd.adapter.model.DefaultGuiObject.Converter.maybeGetValueType;
import static com.brentcroft.gtd.adapter.model.swing.JComboBoxGuiObject.MODEL_TAG;
import static com.brentcroft.util.XmlUtils.maybeSetElementAttribute;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JList;
import javax.swing.ListModel;

import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.SwingUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JListGuiObject< T extends JList > extends JComponentGuiObject< T > implements GuiObject.Index
{
	public JListGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		buildListModel( element, options );

		// the last action in the list is the prime action
		addTextAction( element, options );
		addIndexAction( element, options );
	}

	@Override
	public Integer getItemCount()
	{
		return getObject().getModel().getSize();
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

	public void buildListModel( Element element, Map< String, Object > options )
	{
		JList< ? > list = getObject();

		// rip the cells into a simple xml structure:
		Element modelElement = element.getOwnerDocument().createElement( MODEL_TAG );
		element.appendChild( modelElement );

		modelElement.setAttribute( "type", "list" );

		ListModel< ? > model = list.getModel();

		if ( !GuiObject.isShallow( options ) )
		{

			int itemCount = DataLimit.MAX_LIST_DEPTH.getMin( model.getSize(), options );

			for ( int i = 0, n = itemCount; i < n; i++ )
			{
				Object value = model.getElementAt( i );

				Element cellElement = modelElement.getOwnerDocument().createElement( "c" );

				cellElement.setAttribute( "index", "" + i );

				modelElement.appendChild( cellElement );

				maybeSetElementAttribute( cellElement, "text",
						format( "[%s]%s", maybeGetValueType( value ), maybeConvertValue( value ) ) );
			}
		}
	}

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

	enum Attr implements AttrSpec< JList >
	{
		SELECTED_INDEX( "selected-index", go -> "" + go.getSelectedIndex() ),

		SIZE( "size", go -> "" + ofNullable( go.getModel() ).map( model -> "" + model.getSize() ).orElse( null ) ),

		TEXT( "text", go -> ofNullable( go.getSelectedValue() ).map( t -> "" + t ).orElse( null ) );

		final String n;
		final Function< JList, String > f;

		Attr( String name, Function< JList, String > f )
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
		public String getAttribute( JList go )
		{
			return f.apply( go );
		}
	}

}
