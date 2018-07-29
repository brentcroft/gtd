package com.brentcroft.gtd.driver.harness;


import com.brentcroft.gtd.driver.Backend;
import com.brentcroft.gtd.driver.GuiControllerMBean;
import com.brentcroft.gtd.driver.GuiObjectService;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.apache.log4j.Logger;

import static java.lang.String.format;

public class GuiHarness
{
    private final static transient Logger logger = Logger.getLogger( GuiHarness.class );

    // TODO; where from???
    protected static String mBeanRef = "com.brentcroft.gtd.driver:type=GuiController";

    protected static ObjectName controllerJMXName;

    private static final String DEFAULT_SERVICE_CLASS = "com.brentcroft.gtd.handler.HandlerBasedGuiObjectService";

    private static String applicationClassToLoad = null;
    private static String guiServiceClassToLoad = null;

    private static long notificationEventMask = 0;
    private static long notifySnapshotDelay = - 1;
    private static int hashCacheStrategy = - 1;

    private static String getReport()
    {
        return format( "harness         : [%s]%n", Backend.BUILD_VERSION ) +
               format( "main            : %s%n", applicationClassToLoad ) +
               format( "service         : %s%n", guiServiceClassToLoad ) +
               format( "mbean-ref        : %s%n", mBeanRef ) +
               format( "hash-cache      : %s%n", hashCacheStrategy ) +
               format( "awt-mask        : %s%n", notificationEventMask ) +
               format( "snapshot-delay  : %s%n", notifySnapshotDelay );
    }


    /**
     * GuiHarness
     * <p>
     * <p>
     * -main : the fully qualified name of the application main class under test<br/>
     * -adapter : the fully qualified name of the application adapter class (GuiObjectService)<br/>
     * -config : optional path to a configuration properties file<br/>
     * -notify : optional long value that is an AWT event mask
     *
     * @param args
     */
    public static void main( String[] args )
    {
        if ( args != null )
        {
            args = extractArgs( args );
        }


        logger.info( getReport() );

        if ( applicationClassToLoad == null )
        {
            logger.fatal( "Error: incorrect arguments!\n"
                          + "usage: ... GuiHarness"
                          + " -main <applicationClass>"
                          + "[ -service <serviceClass>]"
                          + "[ -notify <long>]" );

            System.exit( 1 );
        }

        try
        {
            installMBean(
                    createGuiObjectService(
                            guiServiceClassToLoad ) );

            start(
                    applicationClassToLoad,
                    args );
        }
        catch ( Exception e )
        {
            logger.fatal( "Error: failed to start: " + e, e );

            e.printStackTrace();

            System.exit( 1 );
        }
    }

    private static String[] extractArgs( String[] args )
    {
        List< String > newArgs = new ArrayList<>();

        for ( int i = 0, n = args.length; i < n; i++ )
        {
            if ( ( i + 1 ) < args.length )
            {
                if ( "-main".equalsIgnoreCase( args[ i ] ) )
                {
                    applicationClassToLoad = args[ i + 1 ];
                    i++;
                }
                else if ( "-service".equalsIgnoreCase( args[ i ] ) )
                {
                    guiServiceClassToLoad = args[ i + 1 ];
                    i++;
                }
                else if ( "-mbean-ref".equalsIgnoreCase( args[ i ] ) )
                {
                    mBeanRef = args[ i + 1 ];
                    i++;
                }
                else if ( "-hash-cache".equalsIgnoreCase( args[ i ] ) )
                {
                    hashCacheStrategy = Integer.valueOf( args[ i + 1 ] );
                    i++;
                }
                else if ( "-awt-mask".equalsIgnoreCase( args[ i ] ) )
                {
                    notificationEventMask = Long.valueOf( args[ i + 1 ] );
                    i++;
                }
                else if ( "-snapshot-delay".equalsIgnoreCase( args[ i ] ) )
                {
                    notifySnapshotDelay = Long.valueOf( args[ i + 1 ] );
                    i++;
                }

                else
                {
                    newArgs.add( args[ i ] );
                    newArgs.add( args[ i + 1 ] );
                }
            }
            else
            {
                newArgs.add( args[ i ] );
            }
        }

        return newArgs.toArray( new String[ newArgs.size() ] );
    }


    private static GuiObjectService< ? > createGuiObjectService( String guiObjectServiceClassName )
            throws IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        if ( guiObjectServiceClassName == null )
        {
            guiObjectServiceClassName = DEFAULT_SERVICE_CLASS;

            logger.warn( format( "No GuiObjectService (adapter) has been specified. Will try using [%s].",
                    guiObjectServiceClassName ) );
        }


        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "About to load GuiObjectService [%s].", guiObjectServiceClassName ) );
        }

        Class< ? > clazz = Class.forName( guiObjectServiceClassName );

        GuiObjectService< ? > client = ( GuiObjectService< ? > ) clazz.newInstance();

        logger.info( format( "Created GuiObjectService: %s", client ) );

        return client;
    }


    private static void installMBean( GuiObjectService< ? > gos )
    {
        try
        {
            GuiControllerMBean controller = gos.getController();

            mBeanRef = Backend.getMBeanRef( controller.getClass() );

            if ( logger.isDebugEnabled() )
            {
                logger.debug( String.format( "About to install MBean: [%s].", mBeanRef ) );
            }


            controller.notifyAWTEvents( notificationEventMask );

            controller.notifyFXEvents( "" );
            controller.notifyDOMEvents( "" );

            if ( hashCacheStrategy > 0 )
            {
                gos
                        .getObjectManager()
                        .getHashCache()
                        .setEnabled( true );

                controller.notifySnapshotEventDelay( notifySnapshotDelay );
            }
            else
            {
                gos
                        .getObjectManager()
                        .getHashCache()
                        .setEnabled( false );
            }


            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

            controllerJMXName = new ObjectName( mBeanRef );

            mbs.registerMBean( controller, controllerJMXName );

            logger.info( format( "Installed MBean [%s]: [%s].", mBeanRef, controller ) );


            // clean shutdown
            Runtime.getRuntime().addShutdownHook( new Thread( () -> shutdown() ) );


        }
        catch ( Exception e )
        {
            throw new RuntimeException( format( "Failed to install MBean [%s]", mBeanRef ), e );
        }
    }


    private static Object start( String applicationClassToLoad, String[] args )
            throws ClassNotFoundException, NoSuchMethodException, SecurityException, IllegalAccessException,
                   IllegalArgumentException, InvocationTargetException
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "About to load class [%s].", applicationClassToLoad ) );
        }

        Class< ? > clazz = Class.forName( applicationClassToLoad );

        final Method method = clazz.getMethod( "main", String[].class );

        logger.info( format( "About to call main method [%s] with args %s.", method, Arrays.asList( args ) ) );

        return method.invoke( null, ( Object ) args );
    }

    public static void shutdown()
    {
        if ( controllerJMXName == null )
        {
            return;
        }

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        try
        {
            mbs.unregisterMBean( controllerJMXName );
        }
        catch ( InstanceNotFoundException | MBeanRegistrationException e )
        {
            logger.warn( "Failure to de-register mbean: " + controllerJMXName, e );
        }

        logger.info( "De-registered mbean: " + controllerJMXName );
    }
}
