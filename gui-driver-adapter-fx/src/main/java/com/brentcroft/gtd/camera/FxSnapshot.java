package com.brentcroft.gtd.camera;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.sun.javafx.stage.StageHelper;

/**
 * A synthetic top-level parent for all Swing and FX(HTML) gui components.
 *
 * Created by Alaric on 14/07/2017.
 */
@SuppressWarnings( "restriction" )
public class FxSnapshot
{
	private static Logger logger = Logger.getLogger( FxSnapshot.class );

	private Snapshot snapshot = new Snapshot();

	@SuppressWarnings( { "unchecked" } )
	public < T extends Object > List< T > getChildren()
	{
		List< T > children = snapshot.getChildren();

		try
		{
			children
					.addAll(
							( List< ? extends T > ) StageHelper.getStages()
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
