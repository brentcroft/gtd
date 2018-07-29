package com.brentcroft.gtd.adapter.model;

import static com.brentcroft.gtd.adapter.model.DefaultGuiObject.Converter.maybeConvertValue;
import static com.brentcroft.gtd.adapter.model.swing.JComboBoxGuiObject.MODEL_TAG;
import static com.brentcroft.gtd.driver.Backend.XML_NAMESPACE_URI;
import static com.brentcroft.util.XmlUtils.maybeAppendElementAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.w3c.dom.Element;

import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 15/07/2017.
 */
public abstract class DefaultGuiObject< T extends Object > extends AbstractGuiObject< T > implements GuiObject.Robot
{
	public DefaultGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	private void setAction( Element element, Map< String, Object > options, String action )
	{
		maybeAppendElementAttribute( options, element, XML_NAMESPACE_URI, ACTIONS_ATTRIBUTE, action );
	}

	protected void addRobotAction( Element element, Map< String, Object > options )
	{
		// TODO: remove the concept of a RobotAction
		// its implicit on every GuiObject
		// setAction( element, options, ATTRIBUTE_VALUE_ROBOT );
	}

	protected void addClickAction( Element element, Map< String, Object > options )
	{
		setAction( element, options, ATTRIBUTE_VALUE_CLICK );
	}

	protected void addTextAction( Element element, Map< String, Object > options )
	{
		setAction( element, options, ATTRIBUTE_VALUE_TEXT );
	}

	protected void addTreeAction( Element element, Map< String, Object > options )
	{
		setAction( element, options, ATTRIBUTE_VALUE_TREE );
	}

	protected void addIndexAction( Element element, Map< String, Object > options )
	{
		setAction( element, options, ATTRIBUTE_VALUE_INDEX );
	}

	protected void addTabAction( Element element, Map< String, Object > options )
	{
		setAction( element, options, ATTRIBUTE_VALUE_TAB );
	}

	protected void addTabsAction( Element element, Map< String, Object > options )
	{
		setAction( element, options, ATTRIBUTE_VALUE_TABS );
	}

	protected void addTableAction( Element element, Map< String, Object > options )
	{
		setAction( element, options, ATTRIBUTE_VALUE_TABLE );
	}

	public static class Converter
	{
		public interface ValueConverter< T >
		{
			String convert( T value );
		}

		private static final Map< Class, ValueConverter > converters = new HashMap< Class, ValueConverter >();

		public static void addConverter( ValueConverter processor, Class... targets )
		{
			for ( Class target : targets )
			{
				converters.put( target, processor );
			}
		}

		@SuppressWarnings( "unchecked" )
		public static String maybeConvertValue( Object value )
		{
			String convert;

			if ( value == null )
			{
				convert = "";
			}
			else if ( (value instanceof String) || !converters.containsKey( value.getClass() ) )
			{
				convert = value.toString();
			}
			else
			{
				convert = converters.get( value.getClass() ).convert( value );
			}

			return convert;
		}

		public static String maybeGetValueType( Object value )
		{
			if ( value == null )
			{
				return "";
			}

			try
			{
				return value.getClass().getSimpleName();
			}
			catch ( Exception ignored )
			{
				// some inner classes have no simple name
			}

			return value.getClass().getName();

		}
	}

	/**
	 *
	 * @param parent
	 * @param options
	 * @param model
	 * @return
	 */
	protected static Element appendListModelElement( Element parent, Map< String, Object > options, int size, final String[] model )
	{
		Element modelElement = parent.getOwnerDocument().createElement( MODEL_TAG );

		parent.appendChild( modelElement );

		modelElement.setAttribute( "type", "list" );
		modelElement.setAttribute( "size", "" + size );

		if ( !GuiObject.isShallow( options ) )
		{
			int itemCount = DataLimit.MAX_LIST_DEPTH.getMin( model.length, options );

			for ( int i = 0, n = itemCount; i < n; i++ )
			{
				String value = model[ i ];

				Element cellElement = modelElement.getOwnerDocument().createElement( "c" );

				modelElement.appendChild( cellElement );

				cellElement.setAttribute( "index", "" + i );
				cellElement.setAttribute( "text", value );
			}
		}

		return modelElement;
	}

	private static < T, S > void appendTreeItems(
			Element parent,
			T[] items,
			Function< T, T[] > itemsGetter,
			Function< T, S > valueGetter )
	{
		for ( int i = 0, n = items.length; i < n; i++ )
		{
			T item = items[ i ];

			Element rowElement = parent.getOwnerDocument().createElement( "n" );

			rowElement.setAttribute( "index", "" + i );
			rowElement.setAttribute( "text", maybeConvertValue( valueGetter.apply( item ) ) );

			parent.appendChild( rowElement );

			appendTreeItems( rowElement, itemsGetter.apply( item ), itemsGetter, valueGetter );
		}
	}

	protected static < T, S > void appendTreeModel(
			Element parent,
			Map< String, Object > options,
			T[] items,
			Function< T, T[] > itemsGetter,
			Function< T, S > valueGetter )
	{
		Element modelElement = parent.getOwnerDocument().createElement( MODEL_TAG );

		parent.appendChild( modelElement );

		modelElement.setAttribute( "type", "tree" );

		if ( !GuiObject.isShallow( options ) )
		{

			for ( int i = 0, n = items.length; i < n; i++ )
			{
				T item = items[ i ];

				Element nodeElement = modelElement.getOwnerDocument().createElement( "n" );

				nodeElement.setAttribute( "index", "" + (i + 1) );
				nodeElement.setAttribute( "text", maybeConvertValue( valueGetter.apply( item ) ) );

				modelElement.appendChild( nodeElement );

				appendTreeItems( nodeElement, itemsGetter.apply( item ), itemsGetter, valueGetter );
			}
		}

	}

	/**
	 * Remember there may be less items than the row count if, for example, the
	 * items were truncated.
	 *
	 * The row count is the number of rows in the actual item and not the length of
	 * items.
	 *
	 * @param parent
	 * @param options
	 * @param rowCount
	 * @param colCount
	 * @param items
	 * @param columnGetter
	 * @return
	 */
	protected static < T, S > Element appendTableModel(
			Element parent,
			Map< String, Object > options,
			int rowCount,
			int colCount,
			String[] headings,
			T[] items,
			BiFunction< T, Integer, S > columnGetter )
	{
		Element modelElement = parent.getOwnerDocument().createElement( MODEL_TAG );

		parent.appendChild( modelElement );

		modelElement.setAttribute( "type", "table" );
		modelElement.setAttribute( "cols", "" + colCount );
		modelElement.setAttribute( "rows", "" + rowCount );

		if ( !GuiObject.isShallow( options ) )
		{
			if ( (headings != null) && (headings.length > 0) )
			{
				Element headingsElement = modelElement.getOwnerDocument().createElement( "h" );

				modelElement.appendChild( headingsElement );

				for ( int i = 0, n = headings.length; i < n; i++ )
				{
					Element headingElement = headingsElement.getOwnerDocument().createElement( "c" );

					headingElement.setAttribute( "index", "" + i );
					headingElement.setAttribute( "text", headings[ i ] );

					headingsElement.appendChild( headingElement );
				}
			}

			for ( int i = 0, n = items.length; i < n; i++ )
			{
				Element rowElement = modelElement.getOwnerDocument().createElement( "r" );

				rowElement.setAttribute( "index", "" + i );

				modelElement.appendChild( rowElement );

				T item = items[ i ];

				for ( int c = 0; c < colCount; c++ )
				{
					Element cellElement = rowElement.getOwnerDocument().createElement( "c" );

					rowElement.appendChild( cellElement );

					S value = columnGetter.apply( item, c );

					cellElement.setAttribute( "index", "" + c );
					cellElement.setAttribute( "text", maybeConvertValue( value ) );

				}
			}
		}
		return modelElement;
	}

}