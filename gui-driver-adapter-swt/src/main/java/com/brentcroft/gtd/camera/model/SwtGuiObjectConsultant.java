package com.brentcroft.gtd.camera.model;

import static com.brentcroft.util.StringUpcaster.downcastCollection;
import static com.brentcroft.util.StringUpcaster.upcastSet;
import static java.lang.String.format;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Widget;

import com.brentcroft.gtd.adapter.model.AbstractGuiObjectConsultant;

public class SwtGuiObjectConsultant< T extends Widget > extends AbstractGuiObjectConsultant< T >
{
	protected final static transient Logger logger = Logger.getLogger( SwtGuiObjectConsultant.class );

	public static final String WIDGETS_TO_SPECIALISE = "camera.%s.widgetsToSpecialise";
	public static final String WIDGETS_TO_MODEL = "camera.%s.widgetsToModel";

	protected Set< String > widgetsToSpecialise = new HashSet<>();
	protected Set< String > widgetsToModel = new HashSet<>();

	public SwtGuiObjectConsultant( Properties properties )
	{
		configure( properties, "SWTWidget" );
	}

	@Override
	public void configure( Properties properties, String name )
	{
		widgetsToSpecialise.clear();
		widgetsToSpecialise.addAll( properties.containsKey( format( WIDGETS_TO_SPECIALISE, name ) )
				? upcastSet( properties.getProperty( format( WIDGETS_TO_SPECIALISE, name ) ) )
				: Arrays.asList( "*" ) );

		widgetsToModel.clear();
		widgetsToModel.addAll( properties.containsKey( format( WIDGETS_TO_MODEL, name ) )
				? upcastSet( properties.getProperty( format( WIDGETS_TO_MODEL, name ) ) )
				: Arrays.asList( "List, Combo" ) );

		logger.debug( format(
				"Configured consultant for [%s]:%n widgetsToSpecialise=[%s]",
				name,
				downcastCollection( widgetsToSpecialise ) ) );
	}

	@Override
	public boolean isHidden( T t )
	{
		return false;
	}

	@Override
	public boolean specialise( T go )
	{
		if ( widgetsToSpecialise.isEmpty() || widgetsToSpecialise.contains( "*" ) )
		{
			return true;
		}

		List< String > parts = Arrays
				.asList( go
						.getClass()
						.getName()
						.split( "\\." ) );
		Collections
				.reverse( parts );

		final StringBuilder b = new StringBuilder();

		// tricky use of "b.insert( 0, "." ) == null" which should always be false (but
		// prepend a period in the process)
		boolean matched = parts
				.stream()
				.map( part -> {
					boolean isSpecialised = widgetsToSpecialise.contains( b.insert( 0, part ).toString() ) ? true : b.insert( 0, "." ) == null;
					return isSpecialised;
				} )
				.filter( f -> f )
				.findAny()
				.isPresent();

		return matched;
	}

	public boolean isListModel( T go )
	{
		if ( widgetsToModel.isEmpty() )
		{
			return false;
		}
		else if ( widgetsToModel.contains( "*" ) )
		{
			return true;
		}

		List< String > parts = Arrays
				.asList( go
						.getClass()
						.getName()
						.split( "\\." ) );
		Collections
				.reverse( parts );

		final StringBuilder b = new StringBuilder();

		// tricky use of "b.insert( 0, "." ) == null" which should always be false (but
		// prepend a period in the process)
		boolean matched = parts
				.stream()
				.map( part -> {
					boolean isSpecialised = widgetsToModel.contains( b.insert( 0, part ).toString() ) ? true : b.insert( 0, "." ) == null;
					return isSpecialised;
				} )
				.filter( f -> f )
				.findAny()
				.isPresent();

		return matched;
	}
}
