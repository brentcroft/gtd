package com.brentcroft.gtd.utilities;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.print.PrinterJob;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLTextAreaElement;

/**
 * A syntax highlighting code editor for JavaFX created by wrapping a CodeMirror code editor in a WebView.
 * <p>
 * See http://codemirror.net for more information on using the codemirror editor.
 */
public class CodeEditor extends StackPane
{
    protected static final Logger logger = Logger.getLogger( CodeEditor.class );

    private final WebView webview = new WebView();

    private static final String TRANSFER = "transfer";

    private String editingCode;

    private boolean loaded = false;


    /**
     * Create a new code editor.
     *
     * @param code the initial code to be edited in the code editor.
     */
    public CodeEditor( String templateUri, String code )
    {
        // install the web view
        getChildren().add( webview );

        // set callback to assign the code
        WebEngine we = webview.getEngine();

        we
                .getLoadWorker()
                .stateProperty()
                .addListener(
                        newOnLoadListener( we, () -> {
                            loaded = true;
                            setCode( code );
                        } ) );

        // and load the template
        we
                .load( templateUri );
    }


    public void setCode( String code )
    {
        if ( ! loaded )
        {
            // set callback to assign the code
            WebEngine we = webview.getEngine();

            we
                    .getLoadWorker()
                    .stateProperty()
                    .addListener( newOnLoadListener( we, () -> setCode( code ) ) );
        }
        else
        {
            if ( Platform.isFxApplicationThread() )
            {
                transferCode( code );
            }
            else
            {
                Platform.runLater( () ->
                {
                    transferCode( code );
                } );
            }
        }
    }


    public void dropHistory()
    {
        webview
                .getEngine()
                .executeScript( "editor.getDoc().clearHistory();" );
    }


    private void transferCode( String code )
    {
        Document d = webview
                .getEngine()
                .getDocument();

        if ( d == null )
        {
            return;
        }

        try
        {
            ( ( HTMLTextAreaElement ) d
                    .getElementById( TRANSFER ) )
                    .setValue( code );

            webview
                    .getEngine()
                    .executeScript( "editor.setValue( document.getElementById( 'transfer' ).value );" );
        }
        catch ( Exception e )
        {
            ( ( HTMLTextAreaElement ) d
                    .getElementById( "code" ) )
                    .setValue( code );
        }
        this.editingCode = getCode();
    }


    /**
     * returns the current code in the editor and updates an editing snapshot of the code which can be reverted to.
     */
    public String getCode()
    {
        try
        {
            return ( String ) webview
                    .getEngine()
                    .executeScript( "editor.getValue();" );
        }
        catch ( Exception e )
        {
            Document d = webview
                    .getEngine()
                    .getDocument();

            if ( d != null )
            {
                HTMLElement htmlElement = ( HTMLElement ) d.getElementById( "code" );

                if ( htmlElement instanceof HTMLTextAreaElement )
                {
                    return ( ( HTMLTextAreaElement ) htmlElement ).getValue();
                }
            }

            return ( String ) webview
                    .getEngine()
                    .executeScript( "document.getElementById( 'code' ).getValue();" );
        }
    }

    /**
     * revert edits of the code to the last edit snapshot taken.
     */
    public void revertEdits()
    {
        setCode( editingCode );
    }

    /**
     * If the code has been edited since last saved or loaded.
     */
    public boolean isDirty()
    {
        String code = getCode();

        return code != null && ! code.equals( editingCode );
    }


    /**
     * Tells the CodeEditor that the current code
     */
    public void setClean()
    {
        this.editingCode = getCode();
    }


    public void print()
    {
        PrinterJob job = PrinterJob.createPrinterJob();

        if ( job == null )
        {
            throw new RuntimeException( "No printer job was created!" );
        }

        if ( job.showPrintDialog( getScene().getWindow() ) )
        {
            webview.getEngine().print( job );
            job.endJob();
        }
    }


    private ChangeListener< State > newOnLoadListener( WebEngine we, Runnable runnable )
    {
        return new ChangeListener< State >()
        {
            @Override
            public void changed( ObservableValue< ? extends State > observable, State oldValue, State newValue )
            {
                switch ( newValue )
                {
                    case SUCCEEDED:
                        runnable.run();

                    case FAILED:

                        // remove if we succeeded or failed
                        we
                                .getLoadWorker()
                                .stateProperty()
                                .removeListener( this );

                    default:
                        //System.out.println( format( "%s: old=[%s] new=[%s].", observable, oldValue, newValue ) );
                }
            }
        };
    }
}


