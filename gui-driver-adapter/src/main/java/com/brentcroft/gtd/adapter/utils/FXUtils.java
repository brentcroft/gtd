package com.brentcroft.gtd.adapter.utils;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.apache.log4j.Logger;

import com.brentcroft.util.Waiter8;

import javafx.application.Platform;

public class FXUtils
{
	private final static Logger logger = Logger.getLogger( FXUtils.class );

	private final static long DEFAULT_CALL_TIMEOUT = 5 * 1000;

	private static void handleException( Exception originalException, Consumer< Exception > exceptionHandler )
	{
		if ( exceptionHandler == null )
		{
			logger.warn( "Unhandled exception.", originalException );
		}
		else
		{
			try
			{
				exceptionHandler.accept( originalException );
			}
			catch ( Exception e2 )
			{
				logger.warn( format( "Exception Handler raised exception: original exception=[%s]", originalException ),
						e2 );
			}
		}
	}

	public static void maybeInvokeNowOnFXThread( Runnable runnable )
	{
		maybeInvokeNowOnFXThread( runnable, null );
	}

	public static void maybeInvokeNowOnFXThread( Runnable runnable, Consumer< Exception > exHandler )
	{
		if ( Platform.isFxApplicationThread() )
		{
			try
			{
				runnable.run();
			}
			catch ( Exception e )
			{
				handleException( e, exHandler );
			}
		}
		else
		{
			final Exception[] exception = { null };
			final Boolean[] completed = { false };

			Platform.runLater( () -> {
				try
				{
					runnable.run();
				}
				catch ( Exception e )
				{
					exception[ 0 ] = e;
				}
				finally
				{
					completed[ 0 ] = true;
				}
			} );

			new Waiter8()
					.onTimeout( millis -> {
						exception[ 0 ] = new Waiter8.TimeoutException(
								format( "Gave up waiting after [%s] millis.", millis ) );
					} )
					.withTimeoutMillis( DEFAULT_CALL_TIMEOUT )
					.until( () -> completed[ 0 ] );

			if ( exception[ 0 ] != null )
			{
				handleException( exception[ 0 ], exHandler );
			}
		}
	}

	public static Object reflectiveCallRunAndWait( final Object guiObject, final String methodName,
			final Object... args )
	{
		final Object[] result = { null };
		final Boolean[] completed = { false };

		final Method[] method = { null };

		try
		{
			method[ 0 ] = guiObject.getClass().getMethod( methodName );
		}
		catch ( NoSuchMethodException e )
		{
			throw new RuntimeException(
					format( "Unavailable method [%s] on gui object [%s]; %s", methodName, guiObject, e ) );
		}

		Platform.runLater( () -> {
			try
			{
				result[ 0 ] = method[ 0 ].invoke( guiObject, args );
			}
			catch ( Exception e )
			{
				// no stack trace
				throw new RuntimeException(
						format( "Error invoking method [%s] on gui object [%s]; %s", methodName, guiObject, e ) );
			}
			finally
			{
				completed[ 0 ] = true;
			}
		} );

		new Waiter8()
				.withTimeoutMillis( DEFAULT_CALL_TIMEOUT )
				.onTimeout( millis -> {
					throw new Waiter8.TimeoutException( format( "Gave up waiting after [%s] millis.", millis ) );
				} )
				.until( () -> completed[ 0 ] );

		return result[ 0 ];
	}

}
