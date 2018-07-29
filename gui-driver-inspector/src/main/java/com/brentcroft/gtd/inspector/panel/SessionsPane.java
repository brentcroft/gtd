package com.brentcroft.gtd.inspector.panel;


import static java.lang.String.format;

import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.brentcroft.gtd.inspector.Inspector;
import com.brentcroft.gtd.inspector.InspectorApplication;
import com.brentcroft.gtd.inspector.panel.modeller.ModellerPane;
import com.brentcroft.gtd.js.context.ContextUnit;
import com.brentcroft.gtd.js.driver.JSGuiSession;
import com.sun.javafx.stage.StageHelper;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

@SuppressWarnings( "restriction" )
public class SessionsPane extends VBox
{
    protected final static Logger logger = Logger.getLogger( SessionsPane.class );

    private static final String SESSION_NAME_CLASS = "session-status-name";
    private static final String SESSION_ICON_CLASS = "session-status-icon";
    private static final String SESSION_ICON_SPIN_CLASS = "session-status-icon-spin fa-spin";

    private static final String SESSION_STATUS_UNKNOWN_CLASS = "session-status-unknown";
    private static final String SESSION_STATUS_STARTING_CLASS = "session-status-starting fa-pulse";
    private static final String SESSION_STATUS_STARTED_CLASS = "session-status-started";
    private static final String SESSION_STATUS_STOPPING_CLASS = "session-status-stopping fa-pulse";
    private static final String SESSION_STATUS_STOPPED_CLASS = "session-status-stopped";
    private static final String SESSION_STATUS_FAILED_CLASS = "session-status-failed";

    private static final String CSS_STYLESHEET_URI = "css/sessions-pane.css";


    private static final String FONT_AWESOME = "FontAwesome";


    private ListView< JSGuiSession > sessionList = new ListView<>();

    private ObservableList< JSGuiSession > olist;

    //private InspectorApplication contextManager;
    private ContextUnit contextUnit;


    
	private void replaceList()
    {
        contextUnit
                .getSessions()
                .forEach( session -> session.addStateListener( ( oldState, newState ) -> scheduleRefreshListView( session ) ) );


        olist = FXCollections.observableArrayList( contextUnit.getSessions() );

        olist.sort( Comparator.comparing( JSGuiSession::getName ) );

        sessionList.setItems( olist );
    }


    public SessionsPane( InspectorApplication contextManager )
    {
        //this.contextManager = contextManager;
        this.contextUnit = contextManager.getUnit();


        sessionList.getStylesheets().add( CSS_STYLESHEET_URI );

        replaceList();

        // listen to myself
        contextManager.addNewUnitListener( ( unit ) ->
        {
            contextUnit = unit;
            replaceList();
        } );

        sessionList.setId( "sessionList" );

        VBox.setVgrow( sessionList, Priority.ALWAYS );
        HBox.setHgrow( sessionList, Priority.ALWAYS );

        getChildren()
                .addAll(
                        sessionList );

        // context menu
        sessionList.setCellFactory( ( ListView< JSGuiSession > listView ) ->
        {

            ListCell< JSGuiSession > cell = new SessionListCell();


            ContextMenu contextMenu = new ContextMenu();

            {
                MenuItem refreshSessions = new MenuItem();
                refreshSessions.textProperty().bind( Bindings.format( "Refresh" ) );

                refreshSessions.setOnAction( event -> Inspector.execute(
                        "refresh-sessions",
                        () -> listView
                                .getItems()
                                .forEach( this::scheduleRefreshListView ) ) );

                contextMenu.getItems().add( refreshSessions );
            }

            contextMenu.getItems().add( new SeparatorMenuItem() );

            {
                MenuItem startSession = new MenuItem();
                startSession.textProperty().bind( Bindings.format( "Start" ) );
                startSession.setOnAction( event -> Inspector.execute( "start", () -> cell.getItem().start() ) );

                contextMenu.getItems().add( startSession );
            }

            {
                MenuItem stopSession = new MenuItem();
                stopSession.textProperty().bind( Bindings.format( "Stop" ) );
                stopSession.setOnAction( event -> Inspector.execute( "stop session", () -> cell.getItem().stop() ) );

                contextMenu.getItems().add( stopSession );
            }

            contextMenu.getItems().add( new SeparatorMenuItem() );

            {
                MenuItem modelSession = new MenuItem();
                modelSession.textProperty().bind( Bindings.format( "Modeller" ) );
                modelSession.setOnAction( event ->
                {
                    String sessionKey = cell.getItem().getName();

                    String modellerTabTitle = getSessionModellerKey( sessionKey );

                    // if existing modeller for this session then just bring it to the front
                    if ( contextManager.hasTab( modellerTabTitle ) )
                    {
                        contextManager.selectTab( modellerTabTitle );
                    }
                    else
                    {
                        // check for existing modeller
                        Optional< Stage > maybeStage = StageHelper
                                .getStages()
                                .filtered( s -> modellerTabTitle.equals( s.getTitle() ) )
                                .stream()
                                .findFirst();

                        if ( maybeStage.isPresent() )
                        {
                            maybeStage
                                    .get()
                                    .toFront();
                        }
                        else
                        {
                            contextManager
                                    .newTab(
                                            modellerTabTitle,
                                            new ModellerPane(
                                                    contextManager,
                                                    sessionKey,
                                                    null ) );
                        }
                    }
                } );

                contextMenu.getItems().add( modelSession );
            }


            {
                MenuItem getModelScript = new MenuItem();
                getModelScript.textProperty().bind( Bindings.format( "Model script" ) );
                getModelScript.setOnAction( event -> Inspector.execute( "get-session-adapter", () ->
                {
                    contextManager.newScriptTab(
                            format( "Session [%s] adapter script", cell.getItem().getName() ),
                            contextUnit.getModelScript( cell.getItem() ) );
                } ) );

                contextMenu.getItems().add( getModelScript );
            }


            cell.emptyProperty().addListener( ( obs, wasEmpty, isNowEmpty ) ->
            {
                if ( isNowEmpty )
                {
                    cell.setContextMenu( null );
                }
                else
                {
                    cell.setContextMenu( contextMenu );
                }
            } );
            return cell;
        } );
    }

//    private String getSessionEventerKey( String sessionKey )
//    {
//        return format( "Events[%s]", sessionKey );
//    }

    private String getSessionModellerKey( String sessionKey )
    {
        return format( "Modeller[%s]", sessionKey );
    }
//
//    private ModellerPane getSessionModeller( String sessionKey )
//    {
//        String modellerTabTitle = getSessionModellerKey( sessionKey );
//
//        // if available, use existing modeller event pane
//        Optional< Stage > maybeModellerStage = StageHelper
//                .getStages()
//                .filtered( s -> modellerTabTitle.equals( s.getTitle() ) )
//                .stream()
//                .findFirst();
//
//        ModellerPane mo = maybeModellerStage.isPresent()
//                ? ( ModellerPane ) maybeModellerStage.get().getScene().getRoot()
//                : contextManager.hasTab( modellerTabTitle )
//                        ? ( ModellerPane ) contextManager.getTab( modellerTabTitle ).getContent()
//                        : null;
//
//        if ( mo == null )
//        {
//            logger.info( format( "Creating new EventPane on Object tree." ) );
//        }
//        else
//        {
//            logger.info( format( "Using existing EventPane on session modeller." ) );
//        }
//
//        return mo;
//    }
//
//    private EventPane getSessionEventer( String sessionKey )
//    {
//        String eventerTabTitle = getSessionEventerKey( sessionKey );
//
//        // scavenge existing orphan eventer
//        Optional< Stage > maybeEventerStage = StageHelper
//                .getStages()
//                .filtered( s -> eventerTabTitle.equals( s.getTitle() ) )
//                .stream()
//                .findFirst();
//
//        EventPane ep = maybeEventerStage.isPresent()
//                ? ( EventPane ) maybeEventerStage.get().getScene().getRoot()
//                : contextManager.hasTab( eventerTabTitle )
//                        ? ( EventPane ) contextManager.getTab( eventerTabTitle ).getContent()
//                        : null;
//
//        return ep;
//    }
//

    private void scheduleRefreshListView( JSGuiSession item )
    {
        Inspector.schedule( "update-list-view", () ->
        {
            int index = olist.indexOf( item );

            if ( index >= 0 )
            {
                Platform.runLater( () ->
                        {
                            olist.set( index, null );
                            olist.set( index, item );
                        }
                );
            }

        }, 100, TimeUnit.MILLISECONDS );
    }


    class SessionListCell extends ListCell< JSGuiSession >
    {

        private GridPane grid = new GridPane();
        private Label icon = new Label();
        private Label name = new Label();
        private Label status = new Label();

        {
            configureGrid();
            configureIcon();
            configureName();
            configureStatus();
            addControlsToGrid();
        }

        private void configureGrid()
        {
            grid.setHgap( 10 );
            grid.setVgap( 4 );
            grid.setPadding( new Insets( 0, 10, 0, 10 ) );
        }

        private void configureName()
        {
            name.getStyleClass().add( SESSION_NAME_CLASS );
        }


        private void configureIcon()
        {
            icon.setFont( Font.font( FONT_AWESOME, FontWeight.BOLD, 24 ) );
            icon.getStyleClass().add( SESSION_ICON_CLASS );
        }

        private void configureStatus()
        {
            status.getStyleClass().add( SESSION_STATUS_UNKNOWN_CLASS );
        }

        private void addControlsToGrid()
        {
            grid.add( icon, 0, 0, 1, 2 );
            grid.add( name, 1, 0 );
            grid.add( status, 1, 1 );
        }


        private void setContent( JSGuiSession item )
        {
            setText( null );
            icon.setText( item.getModel().getName() );
            name.setText( item.getName() );
            status.setText( item.getState().name() );
            setStyleClassesDependingOnFoundState( item );
            setGraphic( grid );
        }

        private void setStyleClassesDependingOnFoundState( JSGuiSession item )
        {
            switch ( item.getState() )
            {
                case UNKNOWN:
                    setStyle( status, SESSION_STATUS_UNKNOWN_CLASS );
                    icon.setText( "\u2753" );
                    setStyle( icon, SESSION_ICON_CLASS );
                    break;
                case STARTING:
                    setStyle( status, SESSION_STATUS_STARTING_CLASS );
                    icon.setText( "\u25B2" );
                    setStyle( icon, SESSION_ICON_SPIN_CLASS );
                    break;
                case STARTED:
                    setStyle( status, SESSION_STATUS_STARTED_CLASS );
                    icon.setText( "\u2714" );
                    setStyle( icon, SESSION_ICON_CLASS );
                    break;
                case STOPPING:
                    setStyle( status, SESSION_STATUS_STOPPING_CLASS );
                    icon.setText( "\u25BC" );
                    setStyle( icon, SESSION_ICON_SPIN_CLASS );
                    break;
                case STOPPED:
                    setStyle( status, SESSION_STATUS_STOPPED_CLASS );
                    icon.setText( "\u2718" );
                    setStyle( icon, SESSION_ICON_CLASS );
                    break;
                case FAILED:
                    setStyle( status, SESSION_STATUS_FAILED_CLASS );
                    icon.setText( "\u2757" );
                    setStyle( icon, SESSION_ICON_CLASS );
                    break;
            }
        }


        private void setStyle( Label label, String cssClass )
        {
            label
                    .getStyleClass()
                    .stream()
                    .collect( Collectors.toList() )
                    .forEach( c -> label.getStyleClass().remove( c ) );

            label.getStyleClass().add( cssClass );
        }


        @Override
        protected void updateItem( JSGuiSession item, boolean empty )
        {
            super.updateItem( item, empty );

            if ( empty || item == null )
            {
                setText( null );
                setGraphic( null );
            }
            else
            {
                setContent( item );

                setOnMouseClicked( event ->
                {
                    if ( event.getClickCount() > 1 )
                    {
                        Inspector.execute( "toggle start/stop session", () ->
                        {
                            switch ( item.getState() )
                            {
                                case STOPPED:
                                case FAILED:
                                case UNKNOWN:
                                    item.start();
                                    break;

                                case STARTED:
                                    item.stop();
                                    break;

                                default:
                                    logger.warn(
                                            format( "Ignoring mouse clicks: session=[%s] state=[%s].",
                                                    item.getName(),
                                                    item.getState()
                                            ) );
                            }
                        } );
                    }
                } );
            }
        }
    }
}