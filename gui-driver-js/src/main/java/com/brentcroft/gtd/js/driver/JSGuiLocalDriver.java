package com.brentcroft.gtd.js.driver;


import com.brentcroft.gtd.js.driver.local.AbstractJSGuiLocalDriver;
import com.brentcroft.gtd.js.driver.local.JSGuiDriver;
import com.brentcroft.util.Pipes;
import javax.management.Notification;
import javax.management.NotificationListener;
import org.apache.log4j.Logger;

import static java.lang.String.format;

/**
 * Created by adobson on 05/05/2016.
 */
public class JSGuiLocalDriver extends AbstractJSGuiLocalDriver implements JSGuiDriver
{
    private final static Logger logger = Logger.getLogger( JSGuiLocalDriver.class );

    private NotificationListener snapshotNotificationListener = createSnapshotNotificationListener();

    public void cleanup()
    {
        if ( snapshotNotificationListener != null )
        {
            removeNotificationListener( snapshotNotificationListener );
        }

        super.cleanup();
    }


    public void setListenForSnapshots( boolean registerListener )
    {
        if ( registerListener )
        {
            addNotificationListener( snapshotNotificationListener );

            if ( logger.isDebugEnabled() )
            {
                logger.debug( format( "[%s]: Attached snapshot listener [%s].", serial, snapshotNotificationListener ) );
            }
        }
        else
        {
            removeNotificationListener( snapshotNotificationListener );

            if ( logger.isDebugEnabled() )
            {
                logger.debug( format( "[%s]: Detached snapshot listener [%s].", serial, snapshotNotificationListener ) );
            }
        }
    }


    /**
     * Executes a script on the component at the specified path.
     * <p>
     * The script must refer to the component as "component", e.g. "component.setComponentText('fred')"
     *
     * @param path the address of the component to have a script executed.
     */
    @Override
    public void execute( String path, String script, double timeoutSeconds, double pollIntervalSeconds )
    {
        try
        {
            remote()
                    .execute(
                            path,
                            script,
                            timeoutSeconds,
                            pollIntervalSeconds );
        }
        finally
        {
            relax();
        }
    }


    @Override
    public void execute( String path, String script )
    {
        execute( path, script, defaultTimeoutSeconds, defaultPollDelaySeconds );
    }


    @Override
    public void execute( String path, String script, double timeoutSeconds )
    {
        execute( path, script, timeoutSeconds, defaultPollDelaySeconds );
    }


    private NotificationListener createSnapshotNotificationListener()
    {

        return new NotificationListener()
        {
            final String SNAPSHOT_TYPE = "snapshot";
            final double delay = 0.5;
            final int maxEntries = 1;
            final boolean fifo = false;

            @SuppressWarnings( "unchecked" )
			private Pipes.Pipe< Notification, String > bufferPipe = new Pipes.Pipe< Notification, String >()
                    .withConditionIn( notification -> SNAPSHOT_TYPE.equals( notification.getType() ) )
                    .withProcessor( notification -> notification.getMessage() )
                    .withConditionOut( ( snapshot ) -> snapshot != null )
                    .withOutBuffer( delay, maxEntries, fifo )
                    .withListeners( ( snapshot ) ->
                    {
                        if ( logger.isInfoEnabled() )
                        {
                            logger.info( format( "[%s]: Received activation snapshot:\n%s", serial, snapshot ) );
                        }
                    } );


            @Override
            public void handleNotification( Notification notification, Object handback )
            {
                bufferPipe.receive( notification );
            }
        };

    }
}
