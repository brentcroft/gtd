package com.brentcroft.gtd.inspector.panel;

import com.brentcroft.gtd.inspector.Inspector;
import com.brentcroft.gtd.utilities.TextInputControlStream;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * @author Yuhi Ishikura
 * @see: https://github.com/uphy/javafx-console
 */
@SuppressWarnings( "restriction" )
public class ConsoleView extends SplitPane implements InspectorPane
{
    protected static final Logger logger = Logger.getLogger( ConsoleView.class );

    private boolean grab = false;

    private PrintStream out;
    //private final InputStream in;


    private final TextArea textArea;
    private TextInputControlStream inputControlStream;

    private ListView< Inspector.Task > taskListView = null;

    private HBox toolbarBox = null;
    private VBox textAreaBox = null;
    private VBox taskAreaBox = null;


    public ConsoleView()
    {
        this( Charset.forName( "UTF-8" ) );
    }


    public ConsoleView( Charset charset )
    {
        textArea = new TextArea();

        if ( grab )
        {

            inputControlStream = new TextInputControlStream( textArea, charset, logger );

            try
            {
                // redirect to old system out and text area
                this.out = new PrintStream(
                        new TextInputControlStream.TeeOutputStream(
                                System.out,
                                inputControlStream.getOut() ), true, charset.name() );
            }
            catch ( UnsupportedEncodingException e )
            {
                throw new RuntimeException( e );
            }

            // read from textArea
            // this.in = inputControlStream.getIn();


            // System.setIn( getIn() );
            System.setOut( getOut() );
            System.setErr( getOut() );

            System.out.println( "Grabbed System out." );
        }
    }

    public void init()
    {

        // scaffolding
        toolbarBox = new HBox( 15 );
        textAreaBox = new VBox( 5 );
        taskAreaBox = new VBox( 5 );


        final ContextMenu menu = new ContextMenu();

        menu.getItems().add( createItem( "Clear console", e ->
        {
            try
            {
                inputControlStream.clear();
                this.textArea.clear();
            }
            catch ( IOException e1 )
            {
                throw new RuntimeException( e1 );
            }
        } ) );

        menu.getItems().add( createItem( "Copy", e ->
        {
            copySelectedTextToClipboard();
        } ) );

        textArea.setContextMenu( menu );

        setOnKeyPressed( actionKeyPressed );


        //
        taskListView = new ListView< Inspector.Task >();
        taskListView.setItems( Inspector.getObservableTaskList() );

        //
        getStyleClass().add( "console" );


        textArea.setWrapText( false );


        VBox.setVgrow( textArea, Priority.ALWAYS );
        VBox.setVgrow( taskListView, Priority.ALWAYS );


//        RadioButton[] levelControls = {
//                Inspector.newRadioButton( "off", actionChangeLogLevel ),
//                Inspector.newRadioButton( "fatal", actionChangeLogLevel ),
//                Inspector.newRadioButton( "warn", actionChangeLogLevel ),
//                Inspector.newRadioButton( "info", actionChangeLogLevel ),
//                Inspector.newRadioButton( "debug", actionChangeLogLevel ),
//                Inspector.newRadioButton( "trace", actionChangeLogLevel ),
//                Inspector.newRadioButton( "all", actionChangeLogLevel )
//        };
//
//        ToggleGroup group = new ToggleGroup();
//
//        group
//                .getToggles()
//                .addAll( levelControls );
//
//        withToolbarNodes( levelControls );


        textAreaBox
                .getChildren()
                .addAll(
                        toolbarBox,
                        textArea );

        taskAreaBox
                .getChildren()
                .addAll( taskListView );


        final ContextMenu taskAreaMenu = new ContextMenu();

        taskAreaMenu.getItems().add( createItem( "Refresh", e ->
        {
            //
        } ) );

        taskAreaMenu.getItems().add( createItem( "Clear finished tasks", e ->
        {
            Inspector.clearFinishedTasks();
        } ) );


        taskListView.setContextMenu( taskAreaMenu );


        // now me
        setOrientation( Orientation.VERTICAL );
        setDividerPositions( 0.7f, 0.3f );


        getItems()
                .addAll(
                        textAreaBox,
                        taskAreaBox );
    }


    public ConsoleView withToolbarNodes( Node... nodes )
    {
        toolbarBox
                .getChildren()
                .addAll( nodes );

        return this;
    }


    public void copySelectedTextToClipboard()
    {
        String selectedText = this.textArea.getSelectedText();

        if ( selectedText == null )
        {
            return;
        }

        Toolkit
                .getDefaultToolkit()
                .getSystemClipboard()
                .setContents( new StringSelection( selectedText ), null );
    }

    private MenuItem createItem( String name, EventHandler< ActionEvent > a )
    {
        final MenuItem menuItem = new MenuItem( name );
        menuItem.setOnAction( a );
        return menuItem;
    }

    public PrintStream getOut()
    {
        return out;
    }

//    public InputStream getIn()
//    {
//        return in;
//    }


    protected final EventHandler< ActionEvent > actionChangeLogLevel = new EventHandler< ActionEvent >()
    {

        @Override
        public void handle( ActionEvent event )
        {
            if ( event.getSource() instanceof RadioButton )
            {
                RadioButton rb = ( RadioButton ) event.getSource();

                String levelText = rb.getText();

                if ( levelText != null )
                {
                    Level level = Level
                            .toLevel(
                                    levelText
                                            .toUpperCase(),
                                    Level.DEBUG );

                    Logger
                            .getRootLogger()
                            .setLevel( level );

                    logger.info( "Set root logger level: " + level );

                    System.out.println( "Set root logger level: " + level );
                }
                else
                {
                    System.out.println( "RadioButton text was null: " + rb );
                }
            }
            else
            {
                System.out.println( "event.getSource() not a radio button: " + event.getSource() );
            }
        }
    };


    protected final EventHandler< KeyEvent > actionKeyPressed = new EventHandler< KeyEvent >()
    {
        KeyCombination ctrlC = KeyCodeCombination.keyCombination( "Ctrl+C" );

        @Override
        public void handle( KeyEvent event )
        {
            if ( ctrlC.match( event ) )
            {
                copySelectedTextToClipboard();

                // consume the event to stop further action
                event.consume();

            }
        }
    };

    @Override
    public InspectorPane withScriptFile( File scriptFile ) throws IOException
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public File getScriptFile()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getText()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setScriptFile( File scriptFile )
    {
        // TODO Auto-generated method stub

    }

}

