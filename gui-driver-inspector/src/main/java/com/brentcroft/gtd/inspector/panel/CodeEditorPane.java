package com.brentcroft.gtd.inspector.panel;


import com.brentcroft.gtd.inspector.Inspector;
import com.brentcroft.util.FileUtils;
import com.brentcroft.gtd.utilities.WebTemplatePane;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;

@SuppressWarnings( "restriction" )
public abstract class CodeEditorPane extends SplitPane implements InspectorPane
{
    protected static final Logger logger = Logger.getLogger( CodeEditorPane.class );

    private Tab tab;

    private File currentDirectory = new File( new File( "" ).getAbsolutePath() );
    private File codeFile = null;
    private final WebTemplatePane codeEditor;
    protected final TextArea scriptResultArea = new TextArea();

    // scaffolding
    private HBox scriptToolbarBox = new HBox( 15 );
    private VBox scriptAreaBox = new VBox( 5 );
    private VBox scriptResultAreaBox = new VBox( 5 );

    protected abstract FileChooser getFileOpenChooser();

    protected abstract FileChooser getFileSaveChooser();

    public CodeEditorPane( Tab tab, String templateUri, File file )
    {
        this( tab, templateUri, FileUtils.getFileAsString( file ) );

        this.codeFile = file;

        if ( codeFile != null )
        {
            this.currentDirectory = codeFile.getParentFile();
        }
    }

    public CodeEditorPane( Tab tab, String templateUri, String code )
    {
        this.tab = tab;

        codeEditor = new WebTemplatePane( templateUri, code );

        codeEditor.setId( "codeEditor" );
        scriptResultArea.setId( "scriptResultArea" );


        VBox.setVgrow( codeEditor, Priority.ALWAYS );
        VBox.setVgrow( scriptResultArea, Priority.ALWAYS );


        scriptAreaBox
                .getChildren()
                .addAll(
                        scriptToolbarBox,
                        codeEditor );

        scriptResultAreaBox
                .getChildren()
                .addAll(
                        scriptResultArea );

        // now me
        setOrientation( Orientation.VERTICAL );
        setDividerPositions( 0.7f, 0.3f );

        getItems()
                .addAll(
                        scriptAreaBox,
                        scriptResultAreaBox );


        codeEditor.setOnKeyPressed( actionKeyPressed );

        if ( tab != null )
        {
            tab.setOnCloseRequest( actionCheckIfDirty );
        }
    }


    public void withToolbarNodes( Node... nodes )
    {
        codeEditor.withToolbarNodes( nodes );
    }


    private final EventHandler< Event > actionCheckIfDirty = new EventHandler< Event >()
    {
		@Override
        public void handle( Event event )
        {
            if ( codeEditor.isDirty() && new Alert(
                    Alert.AlertType.WARNING,
                    "There are unsaved changes, \nif you close the tab the changes will be lost!",
                    ButtonType.OK, ButtonType.CANCEL )
                                                 .showAndWait()
                                                 .get() == ButtonType.CANCEL )
            {
                event.consume();
            }
        }
    };

    protected final EventHandler< KeyEvent > actionKeyPressed = new EventHandler< KeyEvent >()
    {
        KeyCombination ctrlS = KeyCodeCombination.keyCombination( "Ctrl+S" );
        KeyCombination ctrlO = KeyCodeCombination.keyCombination( "Ctrl+O" );
        KeyCombination ctrlP = KeyCodeCombination.keyCombination( "Ctrl+P" );

        @Override
        public void handle( KeyEvent event )
        {
            if ( ctrlS.match( event ) )
            {
                try
                {
                    save();
                }
                catch ( Exception e )
                {
                    logger.warn( "Error saving file.", e );
                }
                finally
                {
                    // consume the event to stop further action
                    event.consume();
                }
            }

            else if ( ctrlO.match( event ) )
            {
                try
                {
                    open();
                }
                catch ( Exception e )
                {
                    logger.warn( "Error opening file.", e );
                }
                finally
                {
                    // consume the event to stop further action
                    event.consume();
                }
            }

            else if ( ctrlP.match( event ) )
            {
                try
                {
                    codeEditor.print();
                }
                catch ( Exception e )
                {
                    logger.warn( "Error opening file.", e );
                }
                finally
                {
                    // consume the event to stop further action
                    event.consume();
                }
            }
        }
    };

    public CodeEditorPane withScriptFile( File file ) throws IOException
    {
        this.codeFile = file;

        codeEditor.setText( FileUtils.getFileAsString( file ) );

        return this;
    }

    public File getScriptFile()
    {
        return codeFile;
    }

    public String getText()
    {
        return codeEditor.getText();
    }

    public void setScriptFile( File scriptFile )
    {
        this.codeFile = scriptFile;

    }


    protected Tab getTab()
    {
        return tab;
    }

    public void setCode( String code )
    {
        codeEditor.setText( code );
    }

    public String getCode()
    {
        return codeEditor.getText();
    }


    protected void setResultText( String result, boolean append )
    {
        Runnable r = () ->
        {
            if ( append )
            {
                scriptResultArea.appendText( "\n" + result );
            }
            else
            {
                scriptResultArea.setText( result );
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

    public boolean open()
    {
        if ( codeEditor.isDirty() && new Alert(
                Alert.AlertType.WARNING,
                "There are unsaved changes, \nif you open a new file the changes will be lost!",
                ButtonType.OK, ButtonType.CANCEL )
                                             .showAndWait()
                                             .get() == ButtonType.CANCEL )
        {
            return false;
        }


        FileChooser fileChooser = getFileOpenChooser();

        if ( currentDirectory != null )
        {
            fileChooser.setInitialDirectory( currentDirectory );
        }

        File selectedFile = fileChooser.showOpenDialog( null );

        if ( selectedFile == null )
        {
            return false;
        }

        getTab().setText( selectedFile.getName() );

        codeFile = selectedFile;
        currentDirectory = codeFile.getParentFile();

        codeEditor.setText( FileUtils.getFileAsString( codeFile ) );

        codeEditor.setClean();

        logger.info( "Opened code file: " + codeFile );

        return true;
    }

    public boolean save()
    {
        if ( codeFile == null )
        {
            FileChooser fileChooser = getFileSaveChooser();

            if ( currentDirectory != null )
            {
                fileChooser.setInitialDirectory( currentDirectory );
            }

            File selectedFile = fileChooser.showSaveDialog( null );

            if ( selectedFile == null )
            {
                return false;
            }

            getTab().setText( selectedFile.getName() );

            codeFile = selectedFile;

            currentDirectory = codeFile.getParentFile();
        }


        try ( Writer w = new FileWriter( codeFile ) )
        {
            w.write( getText() );
        }
        catch ( IOException e )
        {
            Inspector.errorAlert( "Failed to save code file: " + codeFile, e );

            return false;
        }

        codeEditor.setClean();

        logger.info( "Saved code file: " + codeFile );

        return true;
    }

    public static String stackTraceToString( Throwable e )
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );

        pw.println( e.getMessage() );

        e.printStackTrace( pw );

        return sw.toString();
    }

}