package com.brentcroft.util.buffer;

import com.brentcroft.util.DateUtils;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

/**
 * Created by Alaric on 23/12/2016.
 */
public abstract class DelayedNotificationBuffer< T >
{
    private final static transient Logger logger = Logger.getLogger( DelayedNotificationBuffer.class );
    ;

    private long delay = 1000;

    private ScheduledExecutorService executor = Executors
            .newSingleThreadScheduledExecutor();

    private boolean pending = false;

    private T lastNotification = null;

    // force shutdown
    {
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            public void run()
            {
                List< Runnable > droppedTasks = executor.shutdownNow();

                if ( droppedTasks.size() > 0 )
                {
                    logger.warn( "Executor was abruptly shut down. " + droppedTasks.size() + " tasks will not be executed." );
                }
            }
        } );
    }

    public boolean isPending()
    {
        return pending;
    }


    public long getDelay()
    {
        return delay;
    }

    public void setDelay( long delay )
    {
        this.delay = delay;
    }

    public DelayedNotificationBuffer< T > withDelayMillis( long delayMillis )
    {
        this.delay = delayMillis;
        return this;
    }

    public DelayedNotificationBuffer< T > withDelaySeconds( double delaySeconds )
    {
        return withDelayMillis( DateUtils.secondsToMillis( delaySeconds ) );
    }

    public void shutdownNow()
    {
        executor.shutdownNow();

        logger.debug( "shutdown." );
    }


    public abstract void process( T t );

    private Runnable task = () -> {
        // liable to change during run
        T thisNotification = lastNotification;

        try
        {
            if ( thisNotification != null )
            {
                //logger.debug( "Starting ..." );

                process( thisNotification );

                //logger.debug( "Finished." );
            }
        }
        catch ( Exception e )
        {
            logger.warn( "Error processing.", e );
        }
        finally
        {
            // may still be pending
            if ( thisNotification == lastNotification || lastNotification == null )
            {
                pending = false;

                //logger.debug( "Not pending." );
            }
            else
            {
                logger.debug( "Remain pending." );

                maybeNotify();
            }
        }
    };


    /**
     * Sets the value of notification, maybe starting a task.
     *
     * @param notification
     */
    public void notify( T notification )
    {
        this.lastNotification = notification;

        maybeNotify();
    }

    /**
     * Maybe starts a delayed task to issue the lastNotification (which may have changed by then).
     */
    private synchronized void maybeNotify()
    {
        if ( pending )
        {
            //logger.debug( "Notification already pending." );
        }
        else
        {
            pending = true;

            if ( lastNotification != null )
            {
                executor.schedule( task, delay, TimeUnit.MILLISECONDS );
            }
        }
    }
}
