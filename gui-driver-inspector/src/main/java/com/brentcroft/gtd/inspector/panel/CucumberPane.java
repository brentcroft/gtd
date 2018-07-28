package com.brentcroft.gtd.inspector.panel;

import com.brentcroft.gtd.cucumber.Cucumber;
import com.brentcroft.gtd.inspector.ContextManager;
import com.brentcroft.gtd.inspector.Inspector;
import com.brentcroft.gtd.js.context.CancelException;
import com.brentcroft.gtd.js.context.Context;
import com.brentcroft.gtd.js.context.ContextUnit;
import com.brentcroft.gtd.utilities.TextInputControlStream;
import com.brentcroft.gtd.utilities.WebTemplatePane;
import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;

import static com.brentcroft.util.DateUtils.timestamp;
import static java.lang.String.format;

public class CucumberPane extends CodeEditorPane implements InspectorPane
{
    private final Button cucumberExecuteButton = new Button( "Run" );
    private final Button cucumberCancelButton = new Button( "Cancel" );

    private ContextUnit contextUnit;

    public CucumberPane( ContextManager contextManager, Tab tab )
    {
        this( contextManager, tab, "\n"
                                   + "Feature: example\n\n"
                                   + "Background: example\n\n"
                                   + "Scenario: example\n\n"
                                   + "  And I wait for 1.5 seconds" );
    }

    public CucumberPane( ContextManager contextManager, Tab tab, String code )
    {
        super(
                tab,
                WebTemplatePane.GHERKIN_TEMPLATE_FILENAME,
                code );

        this.contextUnit = contextManager.getUnit();

        contextManager.addNewUnitListener( ( unit ) ->
        {
            contextUnit = unit;
        } );

        init();
    }


    public CucumberPane( ContextManager contextManager, Tab tab, File file )
    {
        super(
                tab,
                WebTemplatePane.GHERKIN_TEMPLATE_FILENAME,
                file );

        this.contextUnit = contextManager.getUnit();

        contextManager.addNewUnitListener( ( unit ) ->
        {
            contextUnit = unit;
        } );

        init();
    }


    private void init()
    {
        cucumberExecuteButton.setId( "cucumberExecuteButton" );
        cucumberCancelButton.setId( "cucumberCancelButton" );

        withToolbarNodes(
                cucumberExecuteButton,
                cucumberCancelButton );

        cucumberExecuteButton.setOnAction( actionExecuteCucumber );
        cucumberCancelButton.setOnAction( actionCancelCucumber );
    }

    private Context context = null;


    private final EventHandler< ActionEvent > actionExecuteCucumber = new EventHandler< ActionEvent >()
    {

        @Override
        public void handle( ActionEvent event )
        {
            // get the code now - on the FX thread
            String code = getCode();

            File sf = getScriptFile();

            String featureName = sf == null ? "(anonymous)" : sf.getName();

            cucumberExecuteButton.setDisable( true );
            cucumberCancelButton.setDisable( false );

            Inspector
                    .execute( "Execute cucumber feature", () ->
                    {
                        List< Cucumber.FeatureResult > result = null;

                        try
                        {
                            Logger cucumberLogger = Logger.getLogger( "CUCUMBER" );

                            TextInputControlStream tics = new TextInputControlStream( scriptResultArea, Charset.forName( "UTF-8" ), cucumberLogger );


                            context = contextUnit.newContext();

                            // TODO: remove when certain
                            // just to check if this happens
                            // in case was previously cancelled
                            if ( context.getBindings().containsKey( "CANCEL" ) )
                            {
                                context.getBindings().remove( "CANCEL" );

                                logger.warn( "\n\n\n\nHAD TO REMOVE HANGING CANCEL!!!!\n\n\n\n" );
                            }


                            result = new Cucumber( featureName )
                                    .processFeature(
                                            code,
                                            context,
                                            tics.getPrintWriter() );

                            tics.getPrintWriter().print(
                                    "\nFINISHED\n\n" + result
                                            .stream()
                                            .map( Cucumber.FeatureResult::toString )
                                            .collect( Collectors.joining( "\n" ) ) );

                            tics.getPrintWriter().flush();

                            context.getBindings().remove( "CANCEL" );

                            context = null;
                        }
                        catch ( Exception e )
                        {
                            setResultText( "\n\nDETAIL\n" + stackTraceToString( e ), true );
                        }
                        finally
                        {
                            Platform.runLater( () ->
                            {
                                cucumberCancelButton.setDisable( true );
                                cucumberExecuteButton.setDisable( false );
                            } );
                        }
                    } );

            setResultText( format( "Dispatched cucumber feature [%s].%n%n", featureName ), false );
        }
    };


    /**
     * Set a cancel string into the context.<p/>
     * The next co-operative script function (i.e. one that calls <code>maybeCancel()</code>)
     * will detect it and raise a CancelException containing the cancel string.
     */
    private final EventHandler< ActionEvent > actionCancelCucumber = new EventHandler< ActionEvent >()
    {

        @Override
        public void handle( ActionEvent event )
        {
            Inspector
                    .execute( "Cancel cucumber feature", () ->
                    {
                        if ( context != null )
                        {
                            context
                                    .getBindings()
                                    .put( "CANCEL", new CancelException( format( "Cancel requested at [%s]", timestamp() ) ) );
                        }
                    } );

            setResultText( "Cancel object created in context.", true );

            Platform.runLater( () -> cucumberCancelButton.setDisable( true ) );
        }
    };


    @Override
    protected FileChooser getFileSaveChooser()
    {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle( "Save Cucumber Feature File" );

        fileChooser
                .getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter( "Cucumber Features", "*.feature" ),
                        new FileChooser.ExtensionFilter( "All Files", "*.*" ) );
        return fileChooser;
    }

    @Override
    protected FileChooser getFileOpenChooser()
    {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle( "Open Cucumber Feature File" );

        fileChooser
                .getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter( "Cucumber Features", "*.feature" ),
                        new FileChooser.ExtensionFilter( "All Files", "*.*" ) );
        return fileChooser;
    }
}
