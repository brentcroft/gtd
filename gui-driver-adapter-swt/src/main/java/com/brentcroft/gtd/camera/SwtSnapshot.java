package com.brentcroft.gtd.camera;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

/**
 * A synthetic top-level parent for all SWT, Swing, FX, HTML gui components.
 *
 * Created by Alaric on 14/07/2017.
 */
public class SwtSnapshot
{
	private static Logger logger = Logger.getLogger( SwtSnapshot.class );

	private FxSnapshot snapshot = new FxSnapshot();

	@SuppressWarnings( "unchecked" )
	public < T extends Object > List< T > getChildren()
	{
		List< T > children = snapshot.getChildren();

		Display display = Optional
				.ofNullable( Display.getCurrent() )
				.orElse( Display.getDefault() );

		if ( display.getThread() == Thread.currentThread() )
		{
			logger.warn( "Aborting: Display Thread is current" );
		}
		else if ( display.isDisposed() )
		{
			logger.warn( format( "Aborting: Display already disposed" ) );
		}
		else
		{
			Object lock = new Object();

			display.asyncExec( () -> {
				try
				{
					children.addAll( ( Collection< ? extends T > ) Arrays.asList( display.getShells() ) );
				}
				finally
				{
					synchronized ( lock )
					{
						lock.notifyAll();
					}
				}
			} );

			try
			{
				synchronized ( lock )
				{
					lock.wait( 100 );
				}
			}
			catch ( InterruptedException e )
			{
				logger.warn( "Interrupted waiting for Display Thread task to complete." );
			}
		}

		return children;
	}
}
