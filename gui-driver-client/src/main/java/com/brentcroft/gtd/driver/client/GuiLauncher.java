package com.brentcroft.gtd.driver.client;

import com.brentcroft.gtd.driver.harness.GuiHarness;
import com.brentcroft.util.Waiter8;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

import static com.brentcroft.util.DateUtils.secondsToMillis;
import static java.lang.String.format;

/**
 * Created by adobson on 06/05/2016.
 */
public class GuiLauncher
{
    private final static transient Logger logger = Logger.getLogger( GuiLauncher.class );
    private final static transient Logger harnessLogger = Logger.getLogger( "HARNESS" );

    protected File workingDirectory = null;

    private String applicationUri = null;
    private String javaCommand = null;
    private String javaVmOptions = "";
    private String javaClassPathRoot = "";
    private String javaClassPath = "";

    private String applicationMainClass = null;
    private String applicationServiceClass = null;

    private Integer applicationNotifyAWTMask = null;
    private Double applicationNotifySnapshotDelay = null;
    private Integer applicationHashCache = null;

    //
    private double firstEchoTimeout = 1.0;

    private boolean addShutdownHook = true;

    private Thread shutdownHook;

    private Process processRun = null;

    private GuiDriver driver = null;

    public GuiLauncher( GuiDriver driver )
    {
        this.driver = driver;
    }

    public String toString()
    {
        return format(
                "applicationUri=%s%n" +
                        "javaCommand=%s%n" +
                        "javaVmOptions=%s%n" +
                        "javaClassPathRoot=%s%n" +
                        "javaClassPath=%s%n" +
                        "applicationMainClass=%s%n" +
                        "applicationServiceClass=%s%n" +
                        "applicationNotifyAWTMask=%s%n" +
                        "applicationNotifySnapshotDelay=%s%n" +
                        "applicationHashCache=%s%n" +
                        "firstEchoTimeout=%s%n" +
                        "addShutdownHook=%s",
                applicationUri,
                javaCommand,
                javaVmOptions,
                javaClassPathRoot,
                javaClassPath,
                applicationMainClass,
                applicationServiceClass,
                applicationNotifyAWTMask,
                applicationNotifySnapshotDelay,
                applicationHashCache,
                firstEchoTimeout,
                addShutdownHook );
    }
    

    public boolean isLaunchable()
    {
        return javaCommand != null;
    }    

    public boolean isWebStart()
    {
        if ( javaCommand == null )
        {
            throw new RuntimeException( "Java command string has not been assigned!" );
        }
        return javaCommand.contains( "javaws" );
    }

    private List< String > buildProcessCommands()
    {
        final List< String > commands = new ArrayList< String >();

        if ( isWebStart() )
        {
            commands.add( javaCommand );
            commands.add( applicationUri );
        }
        else
        {
            Collections.addAll( commands, javaCommand.split( "\\s+" ) );

            if ( javaVmOptions != null && !javaVmOptions.isEmpty() )
            {
                Collections.addAll( commands, javaVmOptions.split( "\\s+" ) );
            }

            commands.add( "-cp" );
            commands.add( buildClasspath() );

            commands.add( GuiHarness.class.getName() );

            commands.add( "-main" );
            commands.add( applicationMainClass );

            if ( applicationServiceClass != null )
            {
                commands.add( "-service" );
                commands.add( applicationServiceClass );
            }

            if ( applicationHashCache != null )
            {
                commands.add( "-hash-cache" );
                commands.add( applicationHashCache.toString() );
            }

            if ( applicationNotifyAWTMask != null )
            {
                commands.add( "-awt-mask" );
                commands.add( applicationNotifyAWTMask.toString() );
            }

            if ( applicationNotifySnapshotDelay != null )
            {
                commands.add( "-snapshot-delay" );
                commands.add( "" + secondsToMillis( applicationNotifySnapshotDelay ) );
            }

        }

        return commands;
    }

    private String buildClasspath()
    {
        StringBuilder cp = new StringBuilder();

        cp
                .append( "." )
                .append( File.pathSeparator );

        // can't make sense as "." IS the working directory
        // since the process is started in the working directory
        // if ( workingDirectory != null )
        // {
        // cp.append( ( File.pathSeparator + workingDirectory.getAbsolutePath() ).trim()
        // );
        // }

        if ( javaClassPath != null && !javaClassPath.isEmpty() )
        {
            final String ident = "###@@@";
            final String protocolA = ":/";
            final String protocolAE = ident + "\\";
            final String protocolB = ":\\";
            final String protocolBE = ident + "\\";

            cp.append(
                    Arrays.asList( javaClassPath
                            .replace( protocolA, protocolAE )
                            .replace( protocolB, protocolBE )
                            .split( "\\s*[:;]+\\s*" ) )
                            .stream()
                            .map( path -> path.contains( ident )
                                    ? path.trim()
                                    : ( javaClassPathRoot + path.trim() ).trim() )
                            .collect( Collectors.joining( File.pathSeparator ) )
                            .replace( protocolAE, protocolA )
                            .replace( protocolBE, protocolB ) );
        }

        return cp.toString();
    }

    public void startApplication()
    {
        if ( processRun != null )
        {
            throw new RuntimeException( "Process is still running" );
        }

        final List< String > commands = buildProcessCommands();

        final ProcessBuilder pb = new ProcessBuilder( commands );

        pb.directory( workingDirectory );
        pb.redirectErrorStream( true );

        logger.info( format( "Starting program under test:\n  DIRECTORY: %s%n  COMMAND: %s", workingDirectory,
                pb.command() ) );

        new Thread( () -> {
            try
            {
                processRun = pb.start();

                InputStream is = processRun.getInputStream();
                InputStreamReader isr = new InputStreamReader( is );
                BufferedReader br = new BufferedReader( isr );

                // this thread will keep blocking here until the process stream is closed
                String line;
                while ( ( line = br.readLine() ) != null )
                {
                    if ( harnessLogger.isDebugEnabled() )
                    {
                        harnessLogger.debug( line );
                    }
                }

                logger.info( "Stream gobbler terminated." );

                processRun = null;

            }
            catch ( IOException e )
            {
                logger.warn( "Exception raised during launch.", e );

                if ( processRun != null )
                {
                    try
                    {
                        // processRun.destroyForcibly();
                        processRun.destroy();
                    }
                    catch ( Exception e2 )
                    {
                        logger.warn( "Exception raised destroying process forcibly.", e2 );
                    }
                }

                processRun = null;
            }
        } ).start();

        if ( addShutdownHook )
        {
            logger.info( "Adding stop hook to close program under test..." );

            shutdownHook = newShutdownHook();

            // add a stop hook so when this scope closes we also stop the application
            Runtime.getRuntime().addShutdownHook( shutdownHook );
        }

        logger.info(
                format( "Waiting for harness to become available ...: firstEchoTimeout=[%s].", firstEchoTimeout ) );

        if ( !isHarnessAccessible( driver, firstEchoTimeout ) )
        {
            throw new RuntimeException(
                    format( "Harness was not accessible after [%s] seconds (first echo timed out).",
                            firstEchoTimeout ) );
        }
    }

    /**
     * Call the appHandlers to logout and cleanup.
     */
    public void stopApplication()
    {

        if ( shutdownHook != null )
        {
            Runtime.getRuntime().removeShutdownHook( shutdownHook );

            logger.debug( format( "Removed ShutdownHook: shutdownHook=[%s].", shutdownHook ) );

            shutdownHook = null;
        }

        if ( processRun == null )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( format( "Process has already been disposed." ) );
            }
        }
        else
        {
            processRun.destroy();

            int exitValue = processRun.exitValue();

            processRun = null;

            if ( logger.isDebugEnabled() )
            {
                logger.debug( format( "Disposed with process: exitValue=[%s].", exitValue ) );
            }
        }
    }

    /**
     * Wait for the harness to be accessible and ready for instructions.
     * <p/>
     * This is determined by whether we can get an <code>echo( "hello" )</code>
     * response using the driver.
     * <p>
     * In general this should be pretty quick, as the harness sets up the JMX
     * apparatus early.
     * <p>
     *
     * @param timeoutSeconds
     *            how long to wait before timing out
     * @return true if the harness is accessible
     */
    public boolean isHarnessAccessible( final GuiDriver driver, double timeoutSeconds )
    {
        final boolean[] wasAccessed = { false };

        Waiter8 w = new Waiter8()
                .withDelayMillis( 500 )
                .withTimeoutMillis( secondsToMillis( timeoutSeconds ) )
                .onTimeout( millis -> wasAccessed[ 0 ] = false )
                .until( () -> {
                    try
                    {
                        wasAccessed[ 0 ] = ( "hello".equals( driver.echo( "hello" ) ) );
                    }
                    catch ( Exception e )
                    {
                        // expect all kinds of reasons why it's not accessible
                        logger.trace( "Harness Unaccessible..." );
                    }

                    return wasAccessed[ 0 ];
                } );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( String.format( "echo response [%s]: waited [%s], timeout=[%s].",
                    wasAccessed[ 0 ],
                    w.getWait(),
                    w.getTimeout() ) );
        }

        return wasAccessed[ 0 ];
    }

    public void setApplicationUri( String applicationUri )
    {
        this.applicationUri = applicationUri;
    }

    public void setJavaCommand( String javaCommand )
    {
        this.javaCommand = javaCommand;
    }

    public void setJavaVmOptions( String javaVmOptions )
    {
        this.javaVmOptions = javaVmOptions;
    }

    public void setJavaClassPathRoot( String javaClassPathRoot )
    {
        this.javaClassPathRoot = javaClassPathRoot;
    }

    public void setJavaClassPath( String javaClassPath )
    {
        this.javaClassPath = javaClassPath;
    }

    public void setWorkingDirectory( File directory )
    {
        this.workingDirectory = directory;
    }

    public void setApplicationMainClass( String applicationMainClass )
    {
        this.applicationMainClass = applicationMainClass;
    }

    public void setApplicationServiceClass( String applicationServiceClass )
    {
        this.applicationServiceClass = applicationServiceClass;
    }

    public void setApplicationNotifyAWTMask( Integer applicationNotifyAWTMask )
    {
        this.applicationNotifyAWTMask = applicationNotifyAWTMask;
    }

    public void setApplicationNotifySnapshotDelay( Double applicationNotifySnapshotDelay )
    {
        this.applicationNotifySnapshotDelay = applicationNotifySnapshotDelay;
    }

    public void setApplicationHashCache( Integer applicationHashCache )
    {
        this.applicationHashCache = applicationHashCache;
    }

    public void setFirstEchoTimeout( double firstEchoTimeout )
    {
        this.firstEchoTimeout = firstEchoTimeout;
    }

    public void setAddShutdownHook( boolean addShutdownHook )
    {
        this.addShutdownHook = addShutdownHook;
    }

    private Thread newShutdownHook()
    {
        return new Thread( () -> {
            logger.info( "Running stop hook: process=" + processRun );

            if ( driver != null )
            {
                try
                {
                    Object response = driver.shutdown( 0 );

                    if ( logger.isDebugEnabled() )
                    {
                        logger.debug( "Stop hook: Driver.shutdown: response=" + response );
                    }
                }
                catch ( Exception e )
                {
                    logger.warn( "Stop hook: Driver.shutdown: ERROR: process=" + processRun, e );
                }
            }

            if ( processRun != null )
            {
                try
                {
                    stopApplication();
                }
                catch ( Exception e )
                {
                    logger.warn( "Stop hook: stopApplication: FAILED: process=" + processRun, e );
                }
            }
        } );
    }

}
