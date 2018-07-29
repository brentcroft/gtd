package com.brentcroft.gtd.adapter.model.swing;

import static com.brentcroft.gtd.adapter.model.DefaultGuiObject.Converter.maybeConvertValue;
import static com.brentcroft.gtd.adapter.model.DefaultGuiObject.Converter.maybeGetValueType;
import static com.brentcroft.util.XmlUtils.maybeSetElementAttribute;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JComboBox;

import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.adapter.utils.SwingUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JComboBoxGuiObject< T extends JComboBox< ? > > extends JComponentGuiObject< T >
		implements GuiObject.Text, GuiObject.Index
{
	public static final String MODEL_TAG = "model";

	public JComboBoxGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		buildComboBoxModel( element, options );

		// the last action in the list is the prime action
		addTextAction( element, options );
		addIndexAction( element, options );
	}

	@Override
	public String getText()
	{
		return getObject().getSelectedItem() == null ? "" : getObject().getSelectedItem().toString();
	}

	@Override
	public void setText( String text )
	{
		FXUtils.maybeInvokeNowOnFXThread( () -> getObject().getEditor().setItem( text ) );
	}

	@Override
	public Integer getItemCount()
	{
		return getObject().getItemCount();
	}

	@Override
	public Integer getSelectedIndex()
	{
		return getObject().getSelectedIndex();
	}

	@Override
	public void setSelectedIndex( int index )
	{
		SwingUtils.maybeInvokeNowOnEventThread( () -> {
			getObject().setSelectedIndex( index );
		} );
	}

	public void buildComboBoxModel( Element element, Map< String, Object > options )
	{
		JComboBox< ? > comboBox = getObject();

		Element modelElement = element.getOwnerDocument().createElement( MODEL_TAG );

		element.appendChild( modelElement );

		modelElement.setAttribute( "type", "select" );

		if ( !GuiObject.isShallow( options ) )
		{
			int itemCount = DataLimit.MAX_COMBO_DEPTH.getMin( comboBox.getItemCount(), options );

			for ( int i = 0, n = itemCount; i < n; i++ )
			{
				Element itemElement = modelElement.getOwnerDocument().createElement( "c" );

				itemElement.setAttribute( "index", "" + i );

				modelElement.appendChild( itemElement );

				final Object value = comboBox.getItemAt( i );

				maybeSetElementAttribute( itemElement, "text",
						format( "[%s]%s", maybeGetValueType( value ), maybeConvertValue( value ) ) );
			}
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

	// "text", "size", "selected-index"
	enum Attr implements AttrSpec< JComboBox< ? > >
	{
		SELECTED_INDEX( "selected-index", go -> "" + go.getSelectedIndex() ),

		SIZE( "size", go -> "" + go.getItemCount() ),

		TEXT( "text", go -> ofNullable( go.getSelectedItem() ).map( t -> "" + t ).orElse( null ) );

		final String n;
		final Function< JComboBox< ? >, String > f;

		Attr( String name, Function< JComboBox< ? >, String > f )
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
		public String getAttribute( JComboBox< ? > go )
		{
			return f.apply( go );
		}
	}
}