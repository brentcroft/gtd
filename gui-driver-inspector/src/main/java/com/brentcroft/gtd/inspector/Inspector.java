package com.brentcroft.gtd.inspector;

import com.brentcroft.gtd.driver.Backend;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.CheckBox;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import org.apache.log4j.Logger;

import static java.lang.String.format;

public class Inspector
{
    private final static Logger logger = Logger.getLogger( Inspector.class );

    public static String version()
    {
        return Backend.BUILD_VERSION;
    }

    public static String build()
    {
        return Backend.BUILD_DATE;
    }


    //
    private static final int POOL_SIZE = 5;
    private static ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool( POOL_SIZE );

    static int TRUNCATE_ERROR_MESSAGE_AT = 1500;


    public static Button newButton( String text, EventHandler< ActionEvent > action )
    {
        Button button = new Button( text );
        button.setOnAction( action );

        ButtonBar.setButtonData( button, ButtonData.LEFT );

        return button;
    }

    public static CheckBox newCheckBox( String text, EventHandler< ActionEvent > action )
    {
        CheckBox button = new CheckBox( text );
        button.setOnAction( action );

        ButtonBar.setButtonData( button, ButtonData.LEFT );

        return button;
    }

    public static RadioButton newRadioButton( String text, EventHandler< ActionEvent > action )
    {
        RadioButton button = new RadioButton( text );
        button.setOnAction( action );

        ButtonBar.setButtonData( button, ButtonData.LEFT );

        return button;
    }

    public static MenuItem newMenuItem( String text, EventHandler< ActionEvent > action )
    {
        MenuItem menuItem = new MenuItem( text );
        menuItem.setOnAction( action );
        return menuItem;
    }

    public static void shutdownNow()
    {
        logger.info( "Shutting down the executor..." );

        try
        {
            tasks.clear();

            Inspector
                    .scheduledExecutor
                    .shutdownNow();
        }
        catch ( Exception e )
        {
            logger.warn( "Error shutting down", e );
        }
    }

    public static void execute( String name, Runnable command )
    {
        scheduledExecutor.execute( () -> {
            new Task( name, command ).run();
        } );
    }

    public static void submit( String name, Runnable command )
    {
        scheduledExecutor.submit( () -> {
            new Task( name, command ).run();
        } );
    }


    public static void schedule( String name, Runnable command, int afterDelay, TimeUnit timeUnit )
    {
        scheduledExecutor.schedule( () -> {
            new Task( name, command ).run();
        }, afterDelay, timeUnit );
    }

    public static void scheduleAtFixedRate( String name, Runnable command, int initialDelay, int period, TimeUnit timeUnit )
    {
        scheduledExecutor.scheduleAtFixedRate( () -> {
            new Task( name, command ).run();
        }, initialDelay, period, timeUnit );
    }

    public static class Task implements Runnable
    {
        long created = System.currentTimeMillis();
        Long started;
        Long finished;
        Runnable command;
        Throwable error;
        String name;

        public Task( String name, Runnable command )
        {
            this.name = name;
            this.command = command;

            //
            if ( name != null )
            {
                Platform.runLater( () ->
                {
                    getObservableTaskList().add( this );
                } );
            }
        }

        private static final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd hh:mm.ss" );

        public String toString()
        {
            return format( "Task: created=[%s], started=[%s], finished=[%s], name=[%s] error=[%s].",
                    sdf.format( new Date( created ) ),
                    started == null ? "-" : started - created,
                    finished == null ? "-" : finished - started,
                    name,
                    error );
        }

        @Override
        public void run()
        {
            try
            {
                started = System.currentTimeMillis();
                command.run();
            }
            catch ( Throwable t )
            {
                error = t;
                logger.warn( "Task failed: " + t.getMessage(), t );
            }
            finally
            {
                finished = System.currentTimeMillis();
            }
        }

    }

    private final static ObservableList< Task > tasks = FXCollections.observableList( new ArrayList< Task >() );


    public static ObservableList< Task > getObservableTaskList()
    {
        return tasks;
    }

    public static void clearFinishedTasks()
    {
        List< Task > tasksToRemove = new ArrayList<>();

        for ( Task task : tasks )
        {
            if ( task.finished != null )
            {
                tasksToRemove.add( task );
            }
        }

        tasksToRemove
                .forEach( ( task ) -> {
                    tasks.remove( task );
                } );
    }


    public static String stackTraceToString( Throwable e )
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );

        pw.println( e.getMessage() );

        e.printStackTrace( pw );

        return sw.toString();
    }


    public static void errorAlert( String message )
    {
        errorAlert( message, null );
    }

    public static void errorAlert( Throwable t )
    {
        errorAlert( null, t );
    }

    public static void errorAlert( final String message, final Throwable t )
    {
        Runnable r = new Runnable()
        {

            public void run()
            {
                if ( t == null )
                {
                    logger.warn( message );
                }
                else if ( message == null )
                {
                    logger.warn( t.getMessage(), t );
                }
                else
                {
                    logger.warn( message, t );
                }

                String st = null;

                if ( t != null )
                {
                    st = Inspector.stackTraceToString( t );
                }

                new Alert(
                        Alert.AlertType.ERROR,
                        ( message != null
                                ? "<h1>" + message + "</h1>"
                                : "" )
                        + ( st != null
                                ? st.substring( 0, Math.min( st.length(), TRUNCATE_ERROR_MESSAGE_AT ) )
                                : "" ) )
                        .showAndWait();
            }
        };

        if ( Platform.isFxApplicationThread() )
        {
            r.run();
        }
        else
        {
            Platform.runLater( r );
        }
    }

}
