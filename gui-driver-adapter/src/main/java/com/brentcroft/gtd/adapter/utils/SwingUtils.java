package com.brentcroft.gtd.adapter.utils;

import com.brentcroft.util.Waiter8;
import javax.swing.SwingUtilities;
import org.apache.log4j.Logger;

import static java.lang.String.format;

public class SwingUtils
{
    private final static transient Logger logger = Logger.getLogger( SwingUtils.class );

    private final static long DEFAULT_CALL_TIMEOUT = 5 * 1000;

    public static void maybeInvokeNowOnEventThread( Runnable runnable )
    {
        if ( SwingUtilities.isEventDispatchThread() )
        {
            runnable.run();
        }
        else
        {
            final Exception[] exception = { null };
            final Boolean[] completed = { false };

            SwingUtilities.invokeLater( () ->
            {
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
                    .onTimeout( millis ->
                    {
                        throw new Waiter8.TimeoutException( format( "Gave up waiting after [%s] millis.", millis ) );
                    } )
                    .withTimeoutMillis( DEFAULT_CALL_TIMEOUT )
                    .until( () -> completed[ 0 ] );

            if ( exception[ 0 ] != null )
            {
                throw exception[ 0 ] instanceof RuntimeException
                        ? ( RuntimeException ) exception[ 0 ]
                        : new RuntimeException( exception[ 0 ] );
            }
        }
    }

    public static void maybeInvokeLaterOnEventThread( Runnable runnable )
    {
        if ( SwingUtilities.isEventDispatchThread() )
        {
            runnable.run();
        }
        else
        {
            try
            {
                SwingUtilities.invokeLater( runnable );
            }
            catch ( Exception e )
            {
                throw new RuntimeException( e );
            }
        }
    }


}
