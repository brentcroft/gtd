package com.brentcroft.gtd.adapter.model;

import static com.brentcroft.util.XmlUtils.maybeSetElementAttribute;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.brentcroft.gtd.driver.Backend;
import com.brentcroft.gtd.driver.GuiObjectManager;
import com.brentcroft.gtd.driver.ObjectLostException;
import com.brentcroft.util.xpath.gob.Attribute;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public interface GuiObject< T > extends Gob
{
	Logger logger = Logger.getLogger( GuiObject.class );

	// these provide qualified names
	String ACTIONS_ATTRIBUTE = "a:actions";
	String ATTRIBUTE_HASH = "hash";

	T getObject();

	GuiObjectManager< ? > getManager();

	@Override
	String getComponentTag();

	int[] ZERO = { 0, 0, 0, 0 };

	default int[] getLocation()
	{
		return ZERO;
	}

	default int[] getObjectLocationOnScreen()
	{
		return ZERO;
	}

	default int[] getObjectMidpointLocationOnScreen()
	{
		int[] loc = getObjectLocationOnScreen();
		// return new int[]{ loc[ 0 ] + loc[ 2 ] / 2, loc[ 1 ] + loc[ 3 ] / 2 };
		return new int[] { loc[ 0 ] + 1, loc[ 1 ] + 1 };
	}

	default GuiObjectConsultant< T > getConsultant()
	{
		return null;
	}

	@Override
	default boolean hasAttribute( String name )
	{
		List< Attribute > attributes = getAttributes();

		if ( (attributes == null) || attributes.isEmpty() )
		{
			return false;
		}
		return attributes
				.stream()
				.anyMatch( attribute -> name.equals( attribute.getName() ) );
	}

	@Override
	default String getAttribute( String name )
	{
		List< Attribute > attributes = getAttributes();

		return (attributes == null) || attributes.isEmpty()
				? null
				: attributes
						.stream()
						.filter( attribute -> name.equals( attribute.getName() ) )
						.map( Attribute::getValue )
						.findFirst()
						.orElseGet( null );
	}

	List< AttrSpec< T > > getAttrSpec();

	default List< Attribute > readAttributes()
	{
		List< Attribute > attributes = new ArrayList<>();

		getAttrSpec()
				.forEach( attr -> {

					String attrName = attr.getName();
					String attrValue = attr.getAttribute( getObject() );

					// only non null and non-empty values
					// to improve name and key generation
					if ( (attrValue != null) && !attrValue.isEmpty() )
					{
						attributes.add(
								attributeForNameAndValue(
										attrName,
										attrValue ) );
					}
				} );

		return attributes;
	}

	static void maybeSetAttribute( Element element, Attribute attribute, Map< String, Object > options )
	{
		final String nsUri = attribute.getNameSpace() == null
				? null
				: attribute.getNameSpace().getUri();

		try
		{
			if ( nsUri == null )
			{
				maybeSetElementAttribute(
						element,
						attribute.getName(),
						attribute.getValue() );
			}
			else
			{
				maybeSetElementAttribute(
						options,
						element,
						nsUri,
						attribute.getName(),
						attribute.getValue() );
			}
		}
		catch ( Exception e )
		{
			throw new RuntimeException( format( "Invalid attribute details: ns=[%s], name=[%s], value=[%s].",
					attribute.getNameSpace(),
					attribute.getName(),
					attribute.getValue() ), e );
		}
	}

	default void buildProperties( Element element, Map< String, Object > options )
	{
		GuiObjectConsultant< T > gos = getConsultant();

		getAttributes()
				.stream()
				.filter( attribute -> (gos == null) || !gos.ignoreAttribute( attribute.getName() ) )
				.forEach( attribute -> maybeSetAttribute( element, attribute, options ) );

		if ( gos != null )
		{
			gos.extendElement( this, element );
		}
	}

	@Override
	default boolean hasChildren()
	{
		return false;
	}

	default List< GuiObject< ? > > loadChildren()
	{
		return new ArrayList<>();
	}

	default boolean maybeIgnore()
	{
		GuiObjectConsultant< T > goc = getConsultant();

		if ( (goc != null)
				&& goc.ignore( this ) )
		{
			return true;
		}

		return false;
	}

	default void accept( GuiObjectVisitor visitor )
	{
		if ( maybeIgnore() )
		{
			return;
		}

		switch ( visitor.open( this ) )
		{
			case LOST:
			case DUPLICATE:
				return;

			default:
				if ( !isShallow( visitor.getOptions() ) && hasChildren() )
				{
					Optional
							.ofNullable( loadChildren() )
							.ifPresent( children -> children
									.forEach( child -> child.accept( visitor ) ) );
				}

				try
				{
					visitor.close( this );
				}
				catch ( ObjectLostException ole )
				{
					logger.warn( format( "Failed to close visitor.", ole ) );
				}
		}
	}

	static boolean isShallow( Map< String, Object > options )
	{
		return (options != null)
				&& options.containsKey( Backend.SHALLOW_ATTRIBUTE )
				&& ( Boolean ) options.get( Backend.SHALLOW_ATTRIBUTE );
	}

	default void shallowSnapshot( Element element )
	{
		// don't want a deep snapshot
		// want a shallow one
		// or the event stream gets swollen
		Map< String, Object > options = new HashMap< String, Object >();

		// TODO: switch sense of attributes
		// TODO: check that these are actually used??
		// so it is a white list and not a blacklist
		options.put( Backend.MODEL_ATTRIBUTE, null );
		options.put( Backend.SHALLOW_ATTRIBUTE, true );

		GuiObjectVisitor visitor = new GuiObjectVisitor( element, options );

		switch ( visitor.open( this ) )
		{
			case INSERT:
				visitor.close( this );
			default:
				break;
		}
	}

	GuiObject< T > getThis();

	default Robot asRobot()
	{
		return ( Robot ) getThis();
	}

	default Click asClick()
	{
		return ( Click ) getThis();
	}

	default Text asText()
	{
		return ( Text ) getThis();
	}

	default Index asIndex()
	{
		return ( Index ) getThis();
	}

	default Table asTable()
	{
		return ( Table ) getThis();
	}

	default Tree asTree()
	{
		return ( Tree ) getThis();
	}

	interface Robot
	{
		// these should all be asynchronous
		default void robotClick()
		{
		}

		default void robotDoubleClick()
		{
		}

		default void robotKeys( String keys )
		{
		}
	}

	interface Click extends Robot
	{
		default void click()
		{
			throw new UnsupportedOperationException();
		}
		
		default void rightClick()
		{
			throw new UnsupportedOperationException();
		}
	}

	interface Text
	{
		String getText();

		void setText( String text );
	}

	interface Index
	{
		Integer getItemCount();

		Integer getSelectedIndex();

		void setSelectedIndex( final int index );
	}

	interface Table
	{
		Integer getColumnCount();

		Integer getRowCount();

		Integer getSelectionIndex();

		void selectRow( int row );

		void selectColumn( int column );

		void selectCell( int row, int column );
	}

	interface Tree
	{
		String getPath( String path );

		void selectPath( String path );
	}
}
