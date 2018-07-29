package com.brentcroft.gtd.camera;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

/**
 * A synthetic top-level parent for all AWT and FX (and HTML) gui components.
 *
 * Created by Alaric on 14/07/2017.
 */
public class Snapshot
{
	private final static transient Logger logger = Logger.getLogger( Snapshot.class );

	@SuppressWarnings( "unchecked" )
	public < T extends Object > List< T > getChildren()
	{
		List< T > children = new ArrayList<>();

		children
				.addAll( ( List< ? extends T > ) Arrays
						.asList( java.awt.Window.getWindows() ) );

		try
		{
			children
					.addAll(
							( List< ? extends T > ) com.sun.javafx.stage.StageHelper.getStages()
									.stream()
									.map( stage -> stage.getScene().getRoot() )
									.collect( Collectors.toList() )
					);
		}
		catch ( Throwable e )
		{
			logger.warn( e );

			// java 9

			// children.addAll(
			// javafx.stage.Window.getWindows()
			// .stream()
			// .map( s -> s.getScene().getRoot() )
			// .collect( Collectors.toList() )
			// );
		}

		return children;
	}
}
