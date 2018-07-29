package com.brentcroft.gtd.inspector;

import com.brentcroft.gtd.inspector.panel.CucumberPane;
import com.brentcroft.gtd.inspector.panel.ModelTreePane;
import com.brentcroft.gtd.inspector.panel.ScriptPane;
import com.brentcroft.gtd.inspector.panel.SessionsPane;
import com.brentcroft.gtd.js.context.ContextUnit;
import com.brentcroft.util.FileUtils;
import com.brentcroft.util.TabPaneDetacher;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

import static java.lang.String.format;

@SuppressWarnings( "restriction" )
public class InspectorApplication extends Application implements ContextManager
{
    protected final static Logger logger = Logger.getLogger( InspectorApplication.class );

    private static String[] args = null;

    private static final String CSS_STYLESHEET_URI = "css/sessions-pane.css";


    private int width = 500;
    private int height = 800;

    // grabs the System.out and System.err
    //private final ConsoleView consoleView = new ConsoleView();

    private final TabPane tabPane = new TabPane();
    private final MenuBar menuBar = new MenuBar();
    private final ButtonBar buttonBar = new ButtonBar();


    // force the expansion now - or fail
    private static File currentDirectory = new File( new File( "" ).getAbsolutePath() );


    private File currentConfigFile = null;

    private Stage primaryStage;

    private ContextUnit unit;

    private List< NewUnitListener > newUnitListeners = new ArrayList<>();


    public ContextUnit getUnit()
    {
        return unit;
    }

    @Override
    public Stage getPrimaryStage()
    {
        return primaryStage;
    }

    public File getCurrentDirectory()
    {
        return currentDirectory;
    }

    @Override
    public void addNewUnitListener( NewUnitListener newUnitListener )
    {
        newUnitListeners.add( newUnitListener );
    }

    @Override
    public void start( Stage primaryStage )
    {
        this.primaryStage = primaryStage;

        this.primaryStage.setOnCloseRequest( event ->
                {
                    logger.info( format( "Ignoring close request: source=[%s], target=[%s]: %s",
                            event.getSource(),
                            event.getTarget(),
                            event ) );

                    event.consume();
                }
        );

        // don't throw exceptions during start!
        try
        {
            if ( args.length > 0 )
            {
                // otherwise exists can be false
                currentConfigFile = new File( new File( args[ 0 ] ).getAbsolutePath() );

                if ( currentConfigFile.exists() )
                {
                    currentDirectory = currentConfigFile.getParentFile();
                }
                else
                {
                    throw new IllegalArgumentException( "File does not exist: " + args[ 0 ] );
                }
            }

            String title = "GUI Inspector";

            logger.info( format( "Starting %s: [%s %s] args=%s",
                    title,
                    Inspector.version(),
                    Inspector.build(),
                    Arrays.asList( args ) ) );

            primaryStage.setTitle( title );

            primaryStage.setScene( buildScene() );

            primaryStage.show();
        }
        catch ( Throwable e )
        {
            logger.fatal( "Failed to start...", e );

            stop( 1 );
        }
    }

    private void setContext()
    {
        try
        {
            unit = ( currentConfigFile == null )
                    ? new ContextUnit()
                    : new ContextUnit( currentConfigFile );

            newUnitListeners
                    .stream()
                    .forEach( ( listener ) -> listener.adviseNewUnit( unit ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public void stop()
    {
        stop( 0 );
    }

    public void stop( int status )
    {
        logger.info( "Shutting down the inspector ..." );

        //Waiter8.delay( 5 * 1000 );

        Inspector.shutdownNow();

        logger.info( "Shutting down the platform ..." );

        Platform.exit();

        logger.info( format( "Shutting down the system [%s]", status ) );

        // TODO: can we avoid this?
        //Waiter8.delay( 500 );

        // TODO: do we need this?
        System.exit( status );
    }


    private Scene buildScene()
    {
        VBox root = new VBox( 10 );

        root.setPadding( new Insets( 15, 15, 15, 15 ) );

        VBox.setVgrow( tabPane, Priority.ALWAYS );
        HBox.setHgrow( tabPane, Priority.ALWAYS );

        menuBar.prefWidthProperty().bind( primaryStage.widthProperty() );


        setContext();

        // File menu - new, save, exit
        Menu fileMenu = new Menu( "File" );


        fileMenu
                .getItems()
                .addAll(
                        Inspector.newMenuItem( "Open Session Properties", actionLoadConfigUnit ),
                        new SeparatorMenuItem(),
                        Inspector.newMenuItem( "New Script", actionNewScript ),
                        Inspector.newMenuItem( "New Feature", actionNewFeature ),
                        // newObjectMenuItem,
                        new SeparatorMenuItem(),
                        Inspector.newMenuItem( "Open Script", actionLoadScript ),
                        Inspector.newMenuItem( "Open Feature", actionLoadFeature ),
                        new SeparatorMenuItem(),
                        Inspector.newMenuItem( "Open Tasks", actionOpenTasks ),
                        new SeparatorMenuItem(),
                        Inspector.newMenuItem( "Exit", ( e ) ->
                        {
                            Platform.exit();
                        } ) );

        menuBar
                .getMenus()
                .addAll( fileMenu );


        buttonBar
                .getButtons()
                .addAll(
                        Inspector.newButton( "session", actionLoadConfigUnit ),
                        Inspector.newButton( "script", actionNewScript ),
                        Inspector.newButton( "feature", actionNewFeature ),
                        Inspector.newButton( "open-script", actionLoadScript ),
                        Inspector.newButton( "open-feature", actionLoadFeature )
                );


        TabPaneDetacher.create().makeTabsDetachable( tabPane );

        {
            Tab tab = new Tab();
            tab.setText( "Sessions" );
            tab.setClosable( false );

            SessionsPane sessionsPane = new SessionsPane( this );

            tab.setContent( sessionsPane );


            tabPane.getTabs().add( tab );
        }

        {
            Tab tab = new Tab();
            tab.setText( "Object" );
            tab.setClosable( false );

            tab.setContent(
                    new ModelTreePane( this ) );

            tabPane
                    .getTabs()
                    .add( tab );
        }


        root
                .getChildren()
                .addAll(
                        menuBar,
                        buttonBar,
                        tabPane );


        Scene scene = new Scene( root, width, height );

        scene.getStylesheets().add( CSS_STYLESHEET_URI );

        return scene;
    }


    private final EventHandler< ActionEvent > actionLoadConfigUnit = new EventHandler< ActionEvent >()
    {

        @Override
        public void handle( ActionEvent event )
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle( "Open Context File" );
            fileChooser
                    .getExtensionFilters()
                    .addAll(
                            new ExtensionFilter( "Properties", "*.xml" ),
                            new ExtensionFilter( "All Files", "*.*" ) );


            if ( currentConfigFile != null )
            {
                fileChooser.setInitialFileName( currentConfigFile.getName() );
                fileChooser.setInitialDirectory( currentConfigFile.getParentFile() );
            }
            else if ( currentDirectory != null )
            {
                fileChooser.setInitialDirectory( currentDirectory );
            }

            File selectedFile = fileChooser.showOpenDialog( primaryStage );

            if ( selectedFile != null )
            {
                currentDirectory = selectedFile.getParentFile();

                try
                {
                    unit = new ContextUnit( selectedFile );

                    newUnitListeners
                            .stream()
                            .forEach( ( listener ) ->
                            {
                                listener.adviseNewUnit( unit );
                            } );
                }
                catch ( FileNotFoundException e )
                {
                    e.printStackTrace();
                }
            }
        }
    };


    private static int newScriptInstances = 0;
    private static int newFeatureInstances = 0;

    private final EventHandler< ActionEvent > actionNewScript = new EventHandler< ActionEvent >()
    {
        final static String initialScript = "\n\nprint( 'hello world' );\n\n";

        @Override
        public void handle( ActionEvent event )
        {
            newScriptTab( "script" + newScriptInstances++, initialScript );
        }
    };


    public void newScriptTab( String title, String script )
    {
        if ( ! Platform.isFxApplicationThread() )
        {
            Platform.runLater( () -> newScriptTab( title, script ) );
        }
        else
        {
            try
            {
                Tab tab = new Tab();
                tab.setText( title );
                tab
                        .setContent(
                                ScriptPane
                                        .newScriptPane(
                                                InspectorApplication.this,
                                                tab,
                                                script ) );

                tabPane.getTabs().add( tab );

                tabPane.getSelectionModel().select( tab );

            }
            catch ( Exception e )
            {
                Inspector.errorAlert( e );
            }
        }
    }


    public void newTab( String title, Node content )
    {
        if ( ! Platform.isFxApplicationThread() )
        {
            Platform.runLater( () -> newTab( title, content ) );
        }
        else
        {
            try
            {
                Tab tab = new Tab();
                tab.setContent( content );
                tab.setText( title );

                tabPane.getTabs().add( tab );

                tabPane.getSelectionModel().select( tab );

            }
            catch ( Exception e )
            {
                Inspector.errorAlert( e );
            }
        }
    }

    private final EventHandler< ActionEvent > actionOpenTasks = new EventHandler< ActionEvent >()
    {
        final static String TAB_NAME = "Tasks";

        @Override
        public void handle( ActionEvent event )
        {
            if ( hasTab( TAB_NAME ) )
            {
                selectTab( TAB_NAME );
            }
            else
            {
//                Tab tab = new Tab();
//                tab.setText( TAB_NAME );
//                tab.setClosable( false );
//
//                consoleView.init();
//
//                tab.setContent( consoleView );
//
//                tabPane
//                        .getTabs()
//                        .add( tab );
            }
        }
    };


    private final EventHandler< ActionEvent > actionNewFeature = new EventHandler< ActionEvent >()
    {

        @Override
        public void handle( ActionEvent event )
        {
            try
            {
                Tab tab = new Tab();

                tab.setText( "feature" + newFeatureInstances++ );

                // the CucumberPane creates a new context
                // every time it runs a feature
                tab.setContent( new CucumberPane( InspectorApplication.this, tab ) );

                tabPane.getTabs().add( tab );

                tabPane.getSelectionModel().select( tab );

            }
            catch ( Exception e )
            {
                Inspector.errorAlert( e );
            }
        }
    };


    private final EventHandler< ActionEvent > actionLoadScript = new EventHandler< ActionEvent >()
    {

        @Override
        public void handle( ActionEvent event )
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle( "Open Script File" );
            fileChooser
                    .getExtensionFilters()
                    .addAll(
                            new ExtensionFilter( "Javascript", "*.js" ),
                            new ExtensionFilter( "All Files", "*.*" ) );

            if ( currentDirectory != null )
            {
                fileChooser.setInitialDirectory( currentDirectory );
            }

            List< File > selectedFiles = fileChooser
                    .showOpenMultipleDialog( primaryStage );

            if ( selectedFiles != null && selectedFiles.size() > 0 )
            {

                currentDirectory = selectedFiles
                        .get( 0 )
                        .getParentFile();

                File fileInError = null;

                try
                {
                    for ( File file : selectedFiles )
                    {
                        fileInError = file;

                        Tab tab = new Tab();

                        tab.setText( file.getName() );

                        // the ScriptPane reuses a new context
                        // so it can retain created objects
                        tab
                                .setContent(
                                        ScriptPane
                                                .newScriptPane( InspectorApplication.this,
                                                        tab,
                                                        FileUtils
                                                                .getFileAsString( file ) ) );

                        tabPane
                                .getTabs()
                                .add( tab );

                        tabPane
                                .getSelectionModel()
                                .select( tab );
                    }
                }
                catch ( Exception e )
                {
                    Inspector.errorAlert( "Failed opening javascript file: " + fileInError, e );
                }
            }
        }
    };

    private final EventHandler< ActionEvent > actionLoadFeature = new EventHandler< ActionEvent >()
    {

        @Override
        public void handle( ActionEvent event )
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle( "Open Feature File" );
            fileChooser
                    .getExtensionFilters()
                    .addAll(
                            new ExtensionFilter( "Cucumber", "*.feature" ),
                            new ExtensionFilter( "All Files", "*.*" ) );

            if ( currentDirectory != null )
            {
                fileChooser.setInitialDirectory( currentDirectory );
            }

            List< File > selectedFiles = fileChooser
                    .showOpenMultipleDialog( primaryStage );

            if ( selectedFiles != null && selectedFiles.size() > 0 )
            {

                currentDirectory = selectedFiles
                        .get( 0 )
                        .getParentFile();

                File fileInError = null;

                try
                {
                    for ( File file : selectedFiles )
                    {
                        fileInError = file;

                        Tab tab = new Tab();

                        tab.setText( file.getName() );

                        // the CucumberPane creates a new context
                        // every time it runs a feature
                        tab.setContent( new CucumberPane( InspectorApplication.this, tab, file ) );

                        tabPane
                                .getTabs()
                                .add( tab );
                        tabPane
                                .getSelectionModel()
                                .select( tab );
                    }
                }
                catch ( Exception e )
                {
                    Inspector.errorAlert( "Failed opening feature file: " + fileInError, e );
                }
            }
        }
    };


    public boolean hasTab( String tabTitle )
    {
        return tabPane.getTabs()
                .stream()
                .anyMatch( t -> t.getText().equals( tabTitle ) );
    }

    public void selectTab( String tabTitle )
    {
        tabPane.getTabs()
                .stream()
                .filter( t -> t.getText().equals( tabTitle ) )
                .findFirst()
                .ifPresent( t -> tabPane.getSelectionModel().select( t ) );
    }

    public Tab getTab( String tabTitle )
    {
        Optional< Tab > tab = tabPane.getTabs()
                .stream()
                .filter( t -> t.getText().equals( tabTitle ) )
                .findFirst();

        return tab.isPresent() ? tab.get() : null;
    }


    public static void main( String[] args )
    {
        InspectorApplication.args = args;

        try
        {
            launch( args );
        }
        catch ( Throwable e )
        {
            e.printStackTrace();
        }
    }

}
