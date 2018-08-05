package com.brentcroft.gtd.camera;

import java.util.Iterator;
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
	private static final Logger logger = Logger.getLogger( FxSnapshot.class );
	
	private Snapshot snapshot = new Snapshot();

	@SuppressWarnings( { "unchecked", "deprecation" } )
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
			logger.warn("Failed loading top-level Stages [method 1]: " + e);
			
			try
			{
				// see:
				// https://stackoverflow.com/questions/15239122/how-to-get-all-top-level-window-javafx
				for ( Iterator< ? > windowIt = javafx.stage.Window.impl_getWindows(); windowIt.hasNext(); )
				{
					children.add( ( T ) windowIt.next() );
				}

			}
			catch ( Throwable t )
			{
				logger.warn("Failed loading top-level Stages [method 2]: " + e);
			}

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
