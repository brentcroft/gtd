package com.brentcroft.gtd.adapter.model;

import static com.brentcroft.util.StringUpcaster.downcastCollection;
import static com.brentcroft.util.StringUpcaster.upcast;
import static com.brentcroft.util.StringUpcaster.upcastSet;
import static com.brentcroft.util.XmlUtils.maybeSetElementAttribute;
import static java.lang.String.format;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.brentcroft.gtd.driver.Backend;
import com.brentcroft.util.xpath.ParseException;
import com.brentcroft.util.xpath.XParser;
import com.brentcroft.util.xpath.ast.START;
import com.brentcroft.util.xpath.gob.Axis;
import com.brentcroft.util.xpath.gob.Gob;
import com.brentcroft.util.xpath.gob.GobVisitor;
import com.brentcroft.util.xpath.gob.Selection;

public abstract class AbstractGuiObjectConsultant< T > implements GuiObjectConsultant< T >
{
	// reference the interface
	protected final static transient Logger logger = Logger.getLogger( GuiObjectConsultant.class );

	private Map< String, START > pathCache = new HashMap<>();

	public static final String IGNORE_INVISIBLE = "camera.%s.ignoreInvisible";
	public static final String SCREEN_COORDS = "camera.%s.insertScreenCoords";
	public static final String DEBUG_XPATH_AST = "camera.%s.debugXPathAstTree";

	public static final String TAGS_TO_IGNORE_KEY = "camera.%s.tagsToIgnore";
	public static final String ATTRIBUTES_TO_IGNORE_KEY = "camera.%s.attributesToIgnore";

	protected boolean ignoreInvisible = true;
	protected boolean debugXPathAstTree = false;
	protected boolean screenCoords = false;

	protected Set< String > tagsToIgnore = new HashSet<>();
	protected Set< String > attributesToIgnore = new HashSet<>();

	@Override
	public void configure( Properties properties, String name )
	{
		ignoreInvisible = upcast( properties.getProperty( format( IGNORE_INVISIBLE, name ) ), ignoreInvisible );
		screenCoords = upcast( properties.getProperty( format( SCREEN_COORDS, name ) ), screenCoords );
		debugXPathAstTree = upcast( properties.getProperty( format( DEBUG_XPATH_AST, name ) ), debugXPathAstTree );

		tagsToIgnore.clear();
		tagsToIgnore.addAll( properties.containsKey( format( TAGS_TO_IGNORE_KEY, name ) )
				? upcastSet( properties.getProperty( format( TAGS_TO_IGNORE_KEY, name ) ) )
				: Collections.emptySet() );

		attributesToIgnore.clear();
		attributesToIgnore.addAll( properties.containsKey( format( ATTRIBUTES_TO_IGNORE_KEY, name ) )
				? upcastSet( properties.getProperty( format( ATTRIBUTES_TO_IGNORE_KEY, name ) ) )
				: Collections.emptySet() );

		logger.debug( format(
				"Configured consultant for [%s]:%n ignoreInvisible=[%s]%n insertScreenCoords=[%s]%n tagsToIgnore=%s%n attributesToIgnore=%s",
				name,
				ignoreInvisible,
				screenCoords,
				downcastCollection( tagsToIgnore ),
				downcastCollection( attributesToIgnore ) ) );
	}

	@Override
	public boolean ignoreAttribute( String name )
	{
		return attributesToIgnore.contains( name );
	}

	public abstract boolean isHidden( T t );

	@Override
	public boolean ignore( GuiObject< T > go )
	{
		// avoid ObjectLostException when doing ignore
		T t = null;

		try
		{
			t = go.getObject();
		}
		catch ( Exception e )
		{
			return true;
		}

		if ( ignoreInvisible && isHidden( t ) )
		{
			return true;
		}
		// simple tag name
		else if ( tagsToIgnore.contains( go.getComponentTag() ) )
		{
			return true;
		}
		// try xpath
		else if ( tagsToIgnore
				.stream()
				.filter( v -> v.contains( "[" ) || v.contains( "/" ) )
				.map( path -> visitPath( go, path ) )
				.filter( Objects::nonNull )
				.anyMatch( Selection::toBoolean ) )
		{
			return true;
		}

		return false;
	}

	private Selection visitPath( Gob origin, String path )
	{
		START xpathTree = null;

		final String selfPath = "self::" + path;

		try
		{
			if ( pathCache.containsKey( selfPath ) )
			{
				xpathTree = pathCache.get( selfPath );
			}
			else
			{
				xpathTree = new XParser(
						new ByteArrayInputStream(
								selfPath.getBytes( StandardCharsets.UTF_8.name() ) ) )
										.START();

				pathCache.put( selfPath, xpathTree );

				if ( debugXPathAstTree )
				{
					xpathTree.dump( "  " );
				}
			}
		}
		catch ( UnsupportedEncodingException | ParseException e )
		{
			throw new RuntimeException( "Failed to process xpath: " + selfPath, e );
		}

		return new GobVisitor()
				.visit(
						xpathTree,
						origin,
						Axis.SELF.newSelection().withGob( origin ) );
	}

	/**
	 * Add additional attributes to the provided element:<br/>
	 *
	 * <ul>
	 * <li>screen coords: creates an attribute recording the object's location on
	 * screen (a:xywh="x,y,w,h").</li>
	 * </ul>
	 *
	 * @param t
	 * @param element
	 */
	@Override
	public void extendElement( GuiObject< T > t, Element element )
	{
		if ( screenCoords )
		{
			int[] ol = t.getObjectLocationOnScreen();

			maybeSetElementAttribute(
					element,
					Backend.XML_NAMESPACE_URI,
					"a:xywh",
					format( "%s,%s,%s,%s", ol[ 0 ], ol[ 1 ], ol[ 2 ], ol[ 3 ] )
			);
		}
	}
}
