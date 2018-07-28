package com.brentcroft.gtd.inspector.panel.modeller;

import com.brentcroft.gtd.inspector.ContextManager;
import com.brentcroft.gtd.inspector.Inspector;
import com.brentcroft.gtd.inspector.panel.ModelTreePane;
import com.brentcroft.gtd.js.context.Context;
import com.brentcroft.gtd.js.driver.JSGuiSession;
import com.brentcroft.gtd.utilities.KeyUtils;
import com.brentcroft.gtd.utilities.WebTemplatePane;
import com.brentcroft.gtd.utilities.XmlAccumulator;
import com.brentcroft.util.CommentedProperties;
import com.brentcroft.util.Configurator;
import com.brentcroft.util.FileUtils;
import com.brentcroft.util.Pipes;
import com.brentcroft.util.TabPaneDetacher;
import com.brentcroft.util.TextUtils;
import com.brentcroft.util.TriConsumer;
import com.brentcroft.util.XmlUtils;
import com.brentcroft.util.templates.JstlTemplateManager;
import com.brentcroft.util.templates.el.StandardELFilter;
import com.brentcroft.util.tools.Reducer;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javax.xml.transform.Templates;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static com.brentcroft.gtd.driver.Backend.XML_NAMESPACE_TAG;
import static com.brentcroft.gtd.driver.Backend.XML_NAMESPACE_URI;
import static com.brentcroft.util.StringUpcaster.upcast;
import static com.brentcroft.util.StringUpcaster.upcastProperties;
import static com.brentcroft.util.StringUpcaster.upcastSet;
import static com.brentcroft.util.XmlUtils.newTemplates;
import static com.brentcroft.util.XmlUtils.parse;
import static java.lang.String.format;

/**
 * Created by Alaric on 01/12/2016.
 */
public class AbstractModellerPane extends VBox
{
    protected final static Logger logger = Logger.getLogger( AbstractModellerPane.class );


    protected Context context;
    private final String sessionKey;

    protected final JstlTemplateManager jstl = new JstlTemplateManager()
            .withStripComments( true )
            //.withELFilter( StandardELFilter.XML_ESCAPE_FILTER )
            .withELFilter( StandardELFilter.HTML_ESCAPE_FILTER );

    protected KeyUtils keyUtils = null;

    private String txAutoGeneratedFile = null;


    protected final ProgressBar progressBar = new ProgressBar( 0 );
    protected final ProgressIndicator progressIndicator = new ProgressIndicator( 0 );

    protected final TabPane tabPane = new TabPane();
    protected final Tab historyTab = new Tab();
    protected final Tab xmlTab = new Tab();
    protected final Tab jsonTab = new Tab();
    protected final Tab modelTab = new Tab();
    protected final Tab propertiesTab = new Tab();
    protected final Tab eventTab = new Tab();

    protected Document snapshot = null;
    protected Document accumulatedSnapshot = XmlUtils.newDocument();
    protected Document masterDocument = null;
    protected String preReducerTemplatesText = null;
    protected String reducerTemplatesText = null;
    protected Templates jsonTemplates = null;

    protected Reducer preReducer = null;
    protected Reducer reducer = null;

    protected HistoryPane historyPane;


    protected long serial = 1;

    protected Pipes.Pipe< Node, Node > pipe;

    private ToolBar toolbarBox = new ToolBar();

    protected Button snapshotButton = new Button( "snap!" );
    protected Button rerunHistoryButton = new Button( "replay" );
    protected Button clearButton = new Button( "drop-acc" );

    protected CheckBox preReduceCheckBox = new CheckBox( "pre" );
    protected CheckBox integratorCheckBox = new CheckBox( "int" );
    protected CheckBox reduceTransformCheckBox = new CheckBox( "red" );
    protected CheckBox accumulateCheckBox = new CheckBox( "acc" );
    protected CheckBox masterCheckBox = new CheckBox( "mast" );
    protected CheckBox jsonTransformCheckBox = new CheckBox( "json" );
    protected CheckBox modelCheckBox = new CheckBox( "obj" );

    protected WebTemplatePane xmlTextArea = WebTemplatePane.newXmlEditor( newDocumentText( "empty" ) );
    protected WebTemplatePane jsonModelPane = WebTemplatePane.newJsEditor( "{};" );
    protected EventPane eventPane = null;
    protected ModelTreePane modelTreePane = null;


    protected WebTemplatePane propertiesPane = WebTemplatePane.newPropertiesEditor( "" );
    protected PropertiesPaneActions propertiesPaneActions;
    private boolean savePropertiesRaw;


    private Set< String > pipelineSteps;
    private boolean expandGroups = true;
    private boolean countHits = true;

    protected JSGuiSession getSession()
    {
        return context.getSession( sessionKey );
    }


    public void snap( Document document )
    {
        snapshot = document;

        XmlUtils.removeTrimmedEmptyTextNodes( snapshot );

        tabPane.getSelectionModel().select( xmlTab );

        fireUpdateTextAreas();
    }


    protected ToolBar getToolBar()
    {
        return toolbarBox;
    }


    public AbstractModellerPane( double spacing, ContextManager contextManager, String sessionKey, EventPane ep )
    {
        super( spacing );

        TabPaneDetacher.create().makeTabsDetachable( tabPane );

        contextManager.addNewUnitListener( ( unit ) ->
        {
            context = unit.newContext();
            eventPane.setContext( context );
        } );

        this.context = contextManager.getUnit().newContext();
        this.sessionKey = sessionKey;

        historyPane = new HistoryPane( spacing, contextManager, this );


        propertiesPaneActions = new PropertiesPaneActions( contextManager );


        // do it now
        propertiesPane.setText( getSession().getProperties().downcast() );

        //
        modelTreePane = new ModelTreePane( contextManager, sessionKey );

        // use passed in
        eventPane = ep == null
                ? new EventPane( sessionKey, context, modelTreePane )
                : ep;

        eventPane.setModelTreePane( modelTreePane );
        eventPane.setContext( context );


        //setPadding( new Insets( 10, 10, 10, 10 ) );

        VBox.setVgrow( tabPane, Priority.ALWAYS );
        HBox.setHgrow( tabPane, Priority.ALWAYS );

        VBox.setVgrow( jsonModelPane, Priority.ALWAYS );
        VBox.setVgrow( modelTreePane, Priority.ALWAYS );
        VBox.setVgrow( eventPane, Priority.ALWAYS );


        VBox.setVgrow( historyPane, Priority.ALWAYS );
        HBox.setHgrow( historyPane, Priority.ALWAYS );


        toolbarBox
                .getItems()
                .addAll(
                        snapshotButton,
                        rerunHistoryButton,
                        clearButton,
                        new Separator( Orientation.VERTICAL ),
                        preReduceCheckBox,
                        integratorCheckBox,
                        reduceTransformCheckBox,
                        accumulateCheckBox,
                        masterCheckBox,
                        new Separator( Orientation.VERTICAL ),
                        jsonTransformCheckBox,
                        modelCheckBox );

        {
            historyTab.setText( "history" );
            historyTab.setClosable( false );
            historyTab.setContent( historyPane );

            tabPane
                    .getTabs()
                    .add( historyTab );
        }

        {
            xmlTab.setText( "xml" );
            xmlTab.setClosable( false );
            xmlTab.setContent( xmlTextArea );

            tabPane
                    .getTabs()
                    .add( xmlTab );
        }
        {
            jsonTab.setText( "json" );
            jsonTab.setClosable( false );
            jsonTab.setContent( jsonModelPane );

            tabPane
                    .getTabs()
                    .add( jsonTab );
        }
        {
            modelTab.setText( "object" );
            modelTab.setClosable( false );
            modelTab.setContent( modelTreePane );

            tabPane
                    .getTabs()
                    .add( modelTab );
        }
        {
            eventTab.setText( "event" );
            eventTab.setClosable( false );
            eventTab.setContent( eventPane );

            tabPane
                    .getTabs()
                    .add( eventTab );
        }
        {
            propertiesTab.setText( "config" );
            propertiesTab.setClosable( false );
            propertiesTab.setContent( propertiesPane );


            propertiesPane.withToolbarNodes(

                    Inspector.newButton( "refresh", event -> {
                        propertiesPane.setText( getSessionPropertiesAsText() );
                    } ),

                    Inspector.newButton(
                            "apply",
                            event -> {
                                String text = propertiesPane
                                        .getText()
                                        .replaceAll( "\\r", "" );

                                getSession()
                                        .getProperties()
                                        .replaceWith( upcastProperties( text ) );

                                ingest();
                            }
                    ),

                    Inspector.newButton(
                            "save",
                            event -> {
                                String text = propertiesPane
                                        .getText()
                                        .replaceAll( "\\r", "" );

                                getSession()
                                        .getProperties()
                                        .replaceWith( upcastProperties( text ) );

                                propertiesPaneActions.save(
                                        getSession().getProperties(),
                                        savePropertiesRaw ? text : null );

                                ingest();
                            } ),

                    Inspector.newButton(
                            "load",
                            event -> {
                                if ( propertiesPaneActions.open( getSession().getProperties() ) )
                                {
                                    ingest();
                                }
                            } )
            );

            tabPane
                    .getTabs()
                    .add( propertiesTab );
        }


        getChildren()
                .addAll(
                        toolbarBox,
                        tabPane );


        // do this before assigning actions to the checkboxes
        // to avoid triggering any actions
        init();


        preReduceCheckBox.setOnAction( actionSwitchPreReducer );
        integratorCheckBox.setOnAction( e -> fireUpdateTextAreas() );
        reduceTransformCheckBox.setOnAction( actionSwitchReducer );
        accumulateCheckBox.setOnAction( actionSwitchAccumulator );
        masterCheckBox.setOnAction( actionMaster );
        jsonTransformCheckBox.setOnAction( actionSwitchJson );

        modelCheckBox
                .setOnAction( ( e ) ->
                {
                    if ( modelCheckBox.isSelected() )
                    {
                        if ( jsonTransformCheckBox.isSelected() )
                        {
                            tabPane.getSelectionModel().select( modelTab );

                            fireUpdateTextAreas();
                        }
                        else
                        {
                            Platform.runLater( () -> modelCheckBox.setSelected( false ) );
                        }
                    }
                } );

        clearButton
                .setOnAction( ( e ) -> resetAccumulator() );

    }


    private void init()
    {
        CommentedProperties p = getSession().getProperties();

        configure( p );

        Optional
                .ofNullable( this.pipelineSteps )
                .ifPresent( s -> {
                    s.forEach( c -> {
                        switch ( c.toLowerCase() )
                        {
                            case "pre":
                                loadPreReducerTemplates();
                                preReduceCheckBox.setSelected( true );
                                break;
                            case "int":
                                integratorCheckBox.setSelected( true );
                                break;
                            case "red":
                                loadReducerTemplates();
                                reduceTransformCheckBox.setSelected( true );
                                break;
                            case "acc":
                                accumulateCheckBox.setSelected( true );
                                break;
                            case "mast":
                                masterCheckBox.setSelected( true );
                                break;
                            case "json":
                                loadJsonTemplates();
                                jsonTransformCheckBox.setSelected( true );
                                break;
                            case "object":
                                modelCheckBox.setSelected( true );
                        }
                    } );
                } );
    }


    private void ingest()
    {
        CommentedProperties p = getSession().getProperties();

        configure( p );
        export( p );

        propertiesPane.setText( p.downcast() );

        // then send to remote
        try
        {
            getSession().getDriver().setProperties( p );
        }
        catch ( Exception gde )
        {
            logger.warn( "Failed to set remote properties: " + gde.getMessage() );
        }
    }


    public void configure( CommentedProperties p )
    {
        Arrays.asList(
                Property.values() )
                .forEach( property -> property.configure( this, p ) );

        getKeyUtils();
    }

    public void export( CommentedProperties p )
    {
        Arrays.asList(
                Property.values() )
                .forEach( property -> property.export( this, p ) );
    }

    /**
     */
    public enum Property implements Configurator< AbstractModellerPane, CommentedProperties >
    {
        STRICT_PROPERTIES(
                "$strict",
                "enforce strict property expansion (downcasting)",
                ( a, m, p ) -> {
                    //
                },
                ( a, m, p ) -> {
                    p.setProperty( a, "" + p.isStrict() );
                }
        ),

        RAW_PROPERTIES_SAVE(
                "$raw",
                "save the text from the properties pane without modification (except removing \\r)",
                ( a, m, p ) -> m.savePropertiesRaw = upcast( p.getProperty( a ), false ),
                ( a, m, p ) -> p.setProperty( a, "" + m.savePropertiesRaw )
        ),

        PIPELINE_STEPS(
                "pipeline",
                "which pipeline options are switched on",
                ( a, m, p ) -> m.pipelineSteps = upcastSet( p.getProperty( a ) ),
                ( a, m, p ) -> p.setProperty( a, Optional.ofNullable( m.getPipelineStepsAsText() ).orElse( "" ) )
        ),


        TX_AUTO_GENERATED_FILE(
                "txAutoGeneratedFile",
                "filename that auto-generated translations will be saved to",
                ( a, m, p ) -> m.txAutoGeneratedFile = upcast( p.getProperty( a ), "tx-auto-generated.properties" ),
                ( a, m, p ) -> p.setProperty( a, Optional.ofNullable( m.txAutoGeneratedFile ).orElse( "" ) )
        ),

        ACCUMULATOR_COUNT_HITS(
                "accumulator.countHits",
                "maintain a hits array on each element",
                ( a, m, p ) -> m.countHits = upcast( p.getProperty( a ), m.countHits ),
                ( a, m, p ) -> p.setProperty( a, "" + m.countHits )
        ),


        ACCUMULATOR_EXPAND_GROUPS(
                "accumulator.expandGroups",
                "calculate groups from hits arrays",
                ( a, m, p ) -> m.expandGroups = upcast( p.getProperty( a ), m.expandGroups ),
                ( a, m, p ) -> p.setProperty( a, "" + m.expandGroups )
        ),


        HISTORY_DEFAULT_FILENAME(
                "history.src",
                "default filename for history file",
                ( a, m, p ) -> m.historyPane.setHistoryFilename( upcast( p.getProperty( a ), m.historyPane.getHistoryFilename() ) ),
                ( a, m, p ) -> p.setProperty( a, "" + m.historyPane.getHistoryFilename() )
        ),


        SAMPLE(
                "sample",
                "sample",
                ( a, m, p ) -> {
                },
                ( a, m, p ) -> {
                }
        );

        private final static String ATTRIBUTE_PREFIX = "modeller";
        private final Configurator.PropertiesConfigurator< AbstractModellerPane > configurator;

        Property( String attribute,
                  String comment,
                  TriConsumer< String, AbstractModellerPane, CommentedProperties > config,
                  TriConsumer< String, AbstractModellerPane, CommentedProperties > exporter )
        {
            this.configurator = Configurator.create(
                    attribute.startsWith( "$" )
                            ? attribute
                            : ATTRIBUTE_PREFIX + "." + attribute,
                    comment,
                    config,
                    exporter );
        }

        @Override
        public void configure( AbstractModellerPane pane, CommentedProperties properties )
        {
            configurator.configure( pane, properties );
        }

        @Override
        public void export( AbstractModellerPane pane, CommentedProperties properties )
        {
            configurator.export( pane, properties );
        }
    }


    private String getPipelineStepsAsText()
    {
        if ( pipelineSteps == null )
        {
            pipelineSteps = new HashSet<>();
        }
        if ( preReduceCheckBox.isSelected() )
        {
            pipelineSteps.add( "pre" );
        }
        if ( integratorCheckBox.isSelected() )
        {
            pipelineSteps.add( "int" );
        }
        if ( reduceTransformCheckBox.isSelected() )
        {
            pipelineSteps.add( "red" );
        }
        if ( accumulateCheckBox.isSelected() )
        {
            pipelineSteps.add( "acc" );
        }
        if ( masterCheckBox.isSelected() )
        {
            pipelineSteps.add( "mast" );
        }
        if ( jsonTransformCheckBox.isSelected() )
        {
            pipelineSteps.add( "json" );
        }
        if ( modelCheckBox.isSelected() )
        {
            pipelineSteps.add( "object" );
        }
        return pipelineSteps
                .stream()
                .collect( Collectors.joining( "," ) );
    }


    private String getSessionPropertiesAsText()
    {
        CommentedProperties p = getSession().getProperties();

        // I know I'm here
        export( p );

        Optional.ofNullable( preReducer ).ifPresent( r -> r.export( p ) );
        Optional.ofNullable( reducer ).ifPresent( r -> r.export( p ) );
        Optional.ofNullable( keyUtils ).ifPresent( ku -> ku.export( p ) );

        return p.downcast();
    }


    public void reset()
    {
        snapshot = null;
        accumulatedSnapshot = XmlUtils.newDocument();
    }


    protected void resetAccumulator()
    {
        Inspector.execute( "Reset Accumulator", () ->
        {
            accumulatedSnapshot = XmlUtils.newDocument();

            fireUpdateTextAreas();
        } );
    }


    protected void fireUpdateTextAreas()
    {
        Platform.runLater( () ->
        {
            setRunningDisabled();

            Inspector.submit( "Modeller: run pipeline", () ->
            {
                try
                {
                    pipe.receive( snapshot );
                }
                finally
                {
                    setStoppedEnabled();
                }
            } );
        } );
    }


    protected void setProgress( int step, int total )
    {
        double ratio = Integer
                               .valueOf( step )
                               .doubleValue() / total;
        progressBar.setProgress( ratio );
        progressIndicator.setProgress( ratio );
    }

    private AtomicBoolean runningDisabled = new AtomicBoolean( false );

    protected void setRunningDisabled()
    {
        if ( runningDisabled.compareAndSet( false, true ))
        {
            tabPane.setDisable( true );
            getToolBar().getItems().addAll( progressBar, progressIndicator );
        }
    }

    protected void setStoppedEnabled()
    {
        Platform.runLater( () -> {
            getToolBar().getItems().removeAll( progressBar, progressIndicator );
            tabPane.setDisable( false );
            runningDisabled.set( false );
        } );
    }


    protected void updateTextArea( final WebTemplatePane ta, final String value )
    {
        Platform.runLater( () ->
        {
            ta.setText( value == null ? "" : value );
        } );
    }


    protected final EventHandler< ActionEvent > actionSwitchAccumulator = event -> {

        if ( accumulateCheckBox.isSelected() )
        {
            //resetAccumulator();

            logger.info( format( "Accumulator enabled." ) );
        }
        else
        {
            logger.info( format( "Accumulator disabled." ) );
        }

        fireUpdateTextAreas();
    };


    protected final EventHandler< ActionEvent > actionMaster = event -> {

        if ( masterCheckBox.isSelected() )
        {
            try
            {
                loadMasterDocument();

                fireUpdateTextAreas();
            }
            catch ( Exception e )
            {
                Inspector.errorAlert( e );

                Platform.runLater( () -> masterCheckBox.setSelected( false ) );
            }
        }
        else
        {
            masterDocument = null;

            logger.info( format( "Dropped master." ) );

            fireUpdateTextAreas();
        }
    };


    protected final EventHandler< ActionEvent > actionSwitchPreReducer = event -> {

        if ( preReduceCheckBox.isSelected() )
        {
            try
            {
                loadPreReducerTemplates();

                fireUpdateTextAreas();
            }
            catch ( Exception e )
            {
                Inspector.errorAlert( e );

                Platform.runLater( () -> preReduceCheckBox.setSelected( false ) );
            }
        }
        else
        {
            reducer = null;

            logger.info( format( "Dropped pre-reducer." ) );

            fireUpdateTextAreas();
        }
    };


    protected final EventHandler< ActionEvent > actionSwitchReducer = event -> {

        if ( reduceTransformCheckBox.isSelected() )
        {
            try
            {
                loadReducerTemplates();

                fireUpdateTextAreas();
            }
            catch ( Exception e )
            {
                Inspector.errorAlert( e );

                Platform.runLater( () -> reduceTransformCheckBox.setSelected( false ) );
            }
        }
        else
        {
            reducer = null;

            logger.info( format( "Dropped reducer." ) );

            fireUpdateTextAreas();
        }
    };


    protected final EventHandler< ActionEvent > actionSwitchJson = event -> {

        if ( jsonTransformCheckBox.isSelected() )
        {
            try
            {
                loadJsonTemplates();

                tabPane.getSelectionModel().select( jsonTab );

                fireUpdateTextAreas();
            }
            catch ( Exception e )
            {
                Inspector.errorAlert( e );

                Platform.runLater( () -> jsonTransformCheckBox.setSelected( false ) );
            }
        }
        else
        {
            jsonTemplates = null;

            logger.info( format( "Dropped JSON templates." ) );

            fireUpdateTextAreas();
        }
    };


    private void loadPreReducerTemplates()
    {
        String preReducerXslFileName = getSession()
                .getModel()
                .getXslReducerUri();

        if ( preReducerXslFileName == null )
        {
            throw new RuntimeException( "Session modeller has no pre-reducer uri." );
        }

        try
        {
            preReducerTemplatesText = FileUtils
                    .getFileOrResourceAsString(
                            getSession().getWorkingDirectory(),
                            preReducerXslFileName );

            preReducer = new Reducer( "modeller.pre-reducer", TextUtils.getTranslations() )
                    .withStylesheet( preReducerTemplatesText )
                    .configure( getSession().getProperties() );

            logger.info( format( "Loaded pre-reducer [%s].", preReducerXslFileName ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( format( "Failed loading pre-reducer templates [%s].", preReducerXslFileName ), e );
        }
    }


    private void loadReducerTemplates()
    {
        String reducerXslFileName = getSession()
                .getModel()
                .getXslReducerUri();

        if ( reducerXslFileName == null )
        {
            throw new RuntimeException( "Session modeller has no reducer uri." );
        }

        try
        {
            reducerTemplatesText = FileUtils
                    .getFileOrResourceAsString(
                            getSession().getWorkingDirectory(),
                            reducerXslFileName );

            reducer = new Reducer( "modeller.reducer", TextUtils.getTranslations() )
                    .withStylesheet( reducerTemplatesText )
                    .configure( getSession().getProperties() );

            logger.info( format( "Loaded reducer [%s].", reducerXslFileName ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( format( "Failed loading reducer templates [%s].", reducerXslFileName ), e );
        }
    }

    private void loadMasterDocument()
    {
        String masterFileName = getSession()
                .getModel()
                .getXmlUri();

        if ( masterFileName == null )
        {
            throw new RuntimeException( "Session modeller has no master document uri." );
        }

        try
        {
            // passing in a url for the file or resource
            // so relative references work in JSTL tags
            String urlRef = FileUtils
                    .resolvePath( null, masterFileName )
                    .getAbsolutePath();


// get the master file
            final String masterText = FileUtils
                    .getFileOrResourceAsString(
                            null,
                            masterFileName );


            Map< String, Object > parameters = new HashMap< String, Object >( TextUtils.getTranslations() );

            parameters.put( "tx", TextUtils.getTranslations() );
            parameters.put( "context", context.getProperties() );


            // expand - may include other files
            String translatedMasterTex = jstl
                    .expandText(
                            masterText,
                            urlRef,
                            parameters );


            final String master = jstl.expandText( translatedMasterTex, parameters );

            // parse
            masterDocument = parse( master );

            logger.info( format( "Loaded new reducer master [%s].", masterFileName ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( format( "Failed loading master document [%s].", masterFileName ), e );
        }
    }


    private void loadJsonTemplates()
    {
        String jsonTemplatesFileName = getSession()
                .getModel()
                .getXsl2JsonModelUri();

        if ( jsonTemplatesFileName == null )
        {
            jsonTemplatesFileName = context.getModeller().getXsltUri();
        }

        if ( jsonTemplatesFileName == null )
        {
            throw new RuntimeException( "Session modeller has no json uri." );
        }
        try
        {
            jsonTemplates = newTemplates(
                    FileUtils
                            .getFileOrResourceAsReader(
                                    null,
                                    jsonTemplatesFileName ),
                    FileUtils
                            .resolvePath( null, jsonTemplatesFileName )
                            .toURI()
                            .toURL()
                            .toExternalForm() );

            logger.info( format( "Loaded JSON templates [%s].", jsonTemplatesFileName ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( format( "Failed loading JSON templates [%s].", jsonTemplatesFileName ), e );
        }
    }


// DEFAULTS - XmlAccumulator

    private String newDocumentText( String tag )
    {
        return format( "<%s xmlns:%s=\"%s\"/>", tag, XML_NAMESPACE_TAG, XML_NAMESPACE_URI );
    }


    protected KeyUtils getKeyUtils()
    {
        final Properties p = getSession().getProperties();

        if ( keyUtils == null )
        {
            keyUtils = new KeyUtils();
        }

        // always configure
        return keyUtils.configure( p );
    }


    protected Document mergeSnapshotToAccumulate( Document base, Document addition )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Merging snapshot to accumulate." );
        }

        XmlAccumulator accumulator = new XmlAccumulator();

        try
        {
            accumulator
                    .withCountHits( countHits )
                    .withSerial(
                            Long.valueOf(
                                    addition
                                            .getDocumentElement()
                                            .getAttribute( "seq" ) ) );
        }
        catch ( Exception e )
        {
            accumulator.withSerial( serial++ );
        }


        boolean wasMerged = accumulator.merge( base, addition.getDocumentElement() );

        //
        base.normalizeDocument();


        //
        if ( expandGroups )
        {
            accumulator.expandGroups( base.getDocumentElement() );
        }
        //
        accumulator
                .expandAccData(
                        base.getDocumentElement(),
                        XmlAccumulator.ElementAccDataBiC.ALL );


        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "Merged snapshot to accumulate [changed=%s]", wasMerged ) );
        }

        return base;
    }


    protected Document mergeAccumulateToMaster( Document base, Document addition )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Merging accumulate to master." );
        }

        boolean wasMerged = new XmlAccumulator()
                .withCountHits( false )
                .merge( base, addition.getDocumentElement() );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "Merged accumulate to master [changed=%s]", wasMerged ) );
        }


        return base;
    }


    protected void maybeGenerateTxFile()
    {
        if ( txAutoGeneratedFile != null && ! txAutoGeneratedFile.isEmpty() )
        {
            Inspector.schedule( "Saving generated translations: " + txAutoGeneratedFile, () ->
            {
                Properties p = new Properties();

                p.putAll( TextUtils.getNewNames() );

                try
                {
                    p.store( new FileWriter( txAutoGeneratedFile ), null );
                }
                catch ( IOException e )
                {
                    Inspector.errorAlert( format( "Failed to save generated translations file [%s]; %s.", txAutoGeneratedFile, e ) );
                }

            }, 100, TimeUnit.MILLISECONDS );
        }
    }

}
