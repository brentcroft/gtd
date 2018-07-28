package com.brentcroft.gtd.inspector.panel.modeller;

import com.brentcroft.gtd.inspector.ContextManager;
import com.brentcroft.gtd.inspector.Inspector;
import com.brentcroft.util.Waiter8;
import com.brentcroft.util.XmlUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static com.brentcroft.util.StringUpcaster.upcast;
import static java.lang.String.format;

public class HistoryPane extends VBox
{
    protected final static Logger logger = Logger.getLogger( HistoryPane.class );

    private final ContextManager contextManager;
    private final AbstractModellerPane modeller;
    private String historyFilename = "history.xml";

    protected ObservableList< HistoryItem > history = FXCollections.observableArrayList();
    private ListView< HistoryItem > list = new ListView<>();


    private ToolBar toolbar = new ToolBar();

    private long snapDelay = 500;


    public void addItem( long seq, String timestamp, String item, boolean enabled )
    {
        Platform.runLater( () -> history.add( new HistoryItem( seq, timestamp, item, enabled ) ) );
    }

    public void addItems( List< HistoryItem > items )
    {
        Platform.runLater( () -> history.addAll( items ) );
    }


    public ListView< HistoryItem > getList()
    {
        return list;
    }

    public AbstractModellerPane getModeller()
    {
        return modeller;
    }

    public ContextManager getContextManager()
    {
        return contextManager;
    }

    public void setHistoryFilename( String historyFilename )
    {
        this.historyFilename = historyFilename;
    }

    public String getHistoryFilename()
    {
        return historyFilename;
    }

    public class HistoryItem
    {
        private final String text;
        private final Long seq;
        private final String timestamp;

        private boolean enabled = true;

        public HistoryItem( long seq, String timestamp, String text, boolean enabled )
        {
            this.text = text;
            this.seq = seq;
            this.timestamp = timestamp;
            this.enabled = enabled;
        }


        public int compareTo( HistoryItem item )
        {
            return timestamp.compareTo( item.timestamp );
        }

        public String getText()
        {
            return text;
        }

        public String toString()
        {
            return format( "[%s]: %s%s",
                    seq,
                    timestamp,
                    isEnabled() ? "" : " (disabled)" );
        }

        public boolean isEnabled()
        {
            return enabled;
        }

        public void setEnabled( boolean enabled )
        {
            this.enabled = enabled;
        }
    }

    public HistoryPane( double spacing, ContextManager contextManager, AbstractModellerPane modeller )
    {
        super( spacing );

        this.contextManager = contextManager;
        this.modeller = modeller;

        toolbar
                .getItems()
                .addAll(
                        Inspector.newButton( "save", ( event ) -> saveHistory() ),
                        Inspector.newButton( "load", ( event -> openHistory() ) ),
                        new Separator( Orientation.VERTICAL ),
                        Inspector.newButton( "drop", ( event -> dropHistory() ) )
                );

        VBox.setVgrow( list, Priority.ALWAYS );
        HBox.setHgrow( list, Priority.ALWAYS );

        //list.setPadding( new Insets( 10, 10, 10, 10 ) );

        list.setItems( history );

        getChildren()
                .addAll(
                        toolbar,
                        list );

        // context menu
        list.setCellFactory( ( ListView< HistoryItem > listView ) ->
        {
            HistoryItemCell cell = new HistoryItemCell();

            ContextMenu contextMenu = new ContextMenu();

            {
                MenuItem menuItem = new MenuItem();
                menuItem.textProperty().bind( Bindings.format( "replay item" ) );

                menuItem.setOnAction( event -> Inspector.execute(
                        "replay item",
                        () -> {
                            if ( cell.getItem().isEnabled() )
                            {
                                logger.debug( format( "replaying [%s]", cell.getItem() ) );

                                Waiter8.delay( 500 );

                                modeller.snap( XmlUtils.parse( cell.getItem().getText() ) );
                            }
                        } ) );

                contextMenu.getItems().add( menuItem );
            }

            {
                MenuItem menuItem = new MenuItem();
                menuItem.textProperty().bind( Bindings.format( "enable/disable item" ) );

                menuItem.setOnAction( event -> Inspector.execute(
                        "enable/disable item",
                        () -> {
                            cell
                                    .getItem()
                                    .setEnabled( ! cell
                                            .getItem()
                                            .isEnabled() );

                            Platform.runLater( () -> list.refresh() );
                        } ) );

                contextMenu.getItems().add( menuItem );
            }


            contextMenu.getItems().add( new SeparatorMenuItem() );

            {
                MenuItem menuItem = new MenuItem();
                menuItem.textProperty().bind( Bindings.format( "drop item" ) );
                menuItem.setOnAction( event -> {
                    list
                            .getItems()
                            .remove( cell.getItem() );
                } );

                contextMenu.getItems().add( menuItem );
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

    class HistoryItemCell extends ListCell< HistoryItem >
    {

        private void setContent( HistoryItem item )
        {
            setText( item.toString() );
        }


        @Override
        protected void updateItem( HistoryItem item, boolean empty )
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
                        item.setEnabled( ! item.isEnabled() );

                        Platform.runLater( () -> list.refresh() );
                    }
                } );
            }
        }
    }


    public void replayAll()
    {
        getModeller().reset();

        list
                .getItems()
                .stream()
                .filter( HistoryItem::isEnabled )
                .sorted( HistoryItem::compareTo )
                .forEach( a -> {

                    logger.debug( format( "replaying [%s]", a ) );

                    Waiter8.delay( snapDelay );

                    getModeller().snap( XmlUtils.parse( a.getText() ) );
                } );
    }


    private File currentDirectory;
    private File currentFile;

    private FileChooser newFileChooser( String title )
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle( title );

        if ( currentDirectory == null )
        {
            currentDirectory = getContextManager().getCurrentDirectory();
        }

        if ( currentFile == null )
        {
            currentFile = new File( currentDirectory, historyFilename );
        }

        fileChooser.setInitialDirectory(
                currentFile.getParentFile().exists()
                        ? currentFile.getParentFile()
                        : getContextManager().getCurrentDirectory() );

        fileChooser.setInitialFileName( currentFile.getName() );

        fileChooser
                .getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter( "History", "*.xml" ),
                        new FileChooser.ExtensionFilter( "All Files", "*.*" ) );

        return fileChooser;
    }

    public void saveHistory()
    {
        File file = newFileChooser( "Save History" ).showSaveDialog( getContextManager().getPrimaryStage() );

        if ( file == null )
        {
            return;
        }

        currentFile = file;

        saveToFile( currentFile );
    }


    public void openHistory()
    {
        File file = newFileChooser( "Open History" ).showOpenDialog( getContextManager().getPrimaryStage() );

        if ( file == null || ! file.exists() )
        {
            return;
        }

        currentFile = file;

        readFromFile( currentFile );
    }


    public void dropHistory()
    {
        getModeller().reset();

        list.getItems().clear();
    }


    private void saveToFile( File file )
    {
        // $raw - just write out the given text
        try ( OutputStream os = new BufferedOutputStream( new FileOutputStream( file ) ) )
        {
            os.write( format(
                    "<history>%n%s</history>",
                    list
                            .getItems()
                            .stream()
                            .sorted( HistoryItem::compareTo )
                            .map( hi -> {
                                Document document = XmlUtils.parse( hi.getText() );
                                Element element = document.getDocumentElement();

                                if ( hi.isEnabled() )
                                {
                                    element.removeAttribute( "disabled" );
                                }
                                else
                                {
                                    element.setAttribute( "disabled", "true" );
                                }

                                return document;
                            } )
                            .map( document -> XmlUtils.serialize( document, true, false ) )
                            .collect( Collectors.joining( "\n\n" ) )
            ).getBytes( "UTF8" ) );
        }
        catch ( IOException e )
        {
            logger.warn( format( "Failed to save history file [%s]", file ), e );
        }
    }

    private void readFromFile( File file )
    {
        try ( InputStream is = new BufferedInputStream( new FileInputStream( file ) ) )
        {
            Document document = XmlUtils.parse( new InputSource( is ) );

            List< HistoryItem > items = new ArrayList<>();

            for ( Node node = document.getDocumentElement().getFirstChild(); node != null; node = node.getNextSibling() )
            {
                if ( node.getNodeType() == Node.ELEMENT_NODE )
                {
                    Element element = ( Element ) node;

                    HistoryItem historyItem = new HistoryItem(
                            upcast( element.getAttribute( "seq" ), - 1L ),
                            element.getAttribute( "timestamp" ),
                            XmlUtils.serialize( element ),
                            ! upcast( element.getAttribute( "disabled" ), false ) );

                    items.add( historyItem );
                }
            }

            items.sort( HistoryItem::compareTo );

            list.getItems().clear();

            addItems( items );
        }
        catch ( IOException | SAXException | ParserConfigurationException e )
        {
            logger.warn( format( "Failed to save history file [%s]", file ), e );
        }
    }
}
