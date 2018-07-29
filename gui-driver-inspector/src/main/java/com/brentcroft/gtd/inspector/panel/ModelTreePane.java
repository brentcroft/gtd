package com.brentcroft.gtd.inspector.panel;

import com.brentcroft.gtd.inspector.ContextManager;
import com.brentcroft.gtd.inspector.Inspector;
import com.brentcroft.gtd.inspector.model.EventHashPipe;
import com.brentcroft.gtd.inspector.model.EventRecord;
import com.brentcroft.gtd.inspector.model.ModelObjectDialog;
import com.brentcroft.gtd.inspector.model.ModelObjectTree;
import com.brentcroft.gtd.js.context.Context;
import com.brentcroft.gtd.js.context.model.ModelFunction;
import com.brentcroft.gtd.js.context.model.ModelMember;
import com.brentcroft.gtd.js.context.model.ModelObject;
import com.brentcroft.gtd.js.context.model.ModelProperty;
import com.brentcroft.util.Pipes;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.log4j.Logger;

import static java.lang.String.format;

@SuppressWarnings( { "restriction", "unchecked" } )
public class ModelTreePane extends SplitPane implements InspectorPane
{
    private final static Logger logger = Logger.getLogger( ModelTreePane.class );

    private final String HASH_KEY = "$hash";


    private final Button refreshModelButton = new Button( "Refresh" );
    private final CheckBox synchAwtEventCheckBox = new CheckBox( "Synch AWT" );
    private final CheckBox synchHashCheckBox = new CheckBox( "Synch hash" );
    private final CheckBox activatedCheckBox = new CheckBox( "Active only" );
    private final Button activateModelButton = new Button( "Activate" );
    private final Button findByHashButton = new Button( "Find by hash" );


    private final TextField findByHashTextField = new TextField();

    private final EventHashPipe hashPipe = new EventHashPipe();

    private Context context;
    private String sessionKey;

    private ScriptObjectMirror somTree;

    {
        hashPipe.withListeners( new Pipes.Listener< EventRecord >()
        {
            @Override
            public void receive( EventRecord eventRecord )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( format( "Received:\n%s", eventRecord ) );
                }

                selectByHash( eventRecord.getHash() );
            }
        } );

    }


    private VBox treeBox = null;

    private final TreeView< ModelMember > modelTreeView = new TreeView< ModelMember >();

    private final TextArea resultTextArea = new TextArea();


    public ModelMember findModelMemberByHash( String hash )
    {
        TreeItem< ModelMember > treeItem = findByHash( hash );

        return treeItem == null ? null : treeItem.getValue();
    }

    public TreeItem< ModelMember > findByHash( String hash )
    {
        return findByHash( modelTreeView.getRoot(), hash );
    }

    public TreeItem< ModelMember > findByHash( TreeItem< ModelMember > treeItem, String hash )
    {
        if ( treeItem == null )
        {
            return null;
        }

        ModelMember member = treeItem.getValue();

        if ( member != null
             && member.isObject()
             && ( ( ModelObject ) member ).has( HASH_KEY ) )
        {
            ModelObject mo = ( ModelObject ) member;

            String currentHash = ( String ) mo.get( HASH_KEY );

            if ( hash.equals( currentHash ) )
            {
                return treeItem;
            }
        }

        for ( TreeItem< ModelMember > treeItemChild : treeItem.getChildren() )
        {
            TreeItem< ModelMember > m2 = findByHash( treeItemChild, hash );

            if ( m2 != null )
            {
                return m2;
            }
        }
        return null;
    }


    public TreeView< ModelMember > getTreeView()
    {
        return modelTreeView;
    }


    private ChangeListener< ? super TreeItem< ModelMember > > changeListener = new ChangeListener< TreeItem< ModelMember > >()
    {

        @Override
        public void changed(
                ObservableValue< ? extends TreeItem< ModelMember > > observable,
                TreeItem< ModelMember > old_val, TreeItem< ModelMember > new_val )
        {
            if ( new_val == null )
            {
                return;
            }

            ModelMember item = new_val.getValue();

            logger.info( "Selected ModelMember : " + item );

            if ( item == null )
            {
                return;
            }


            Platform.runLater( () ->
            {
                if ( item.isFunction() )
                {
                    ModelFunction function = ( ModelFunction ) item;

                    StringBuilder b = new StringBuilder();

                    b.append( format( "/* FUNCTION */\n" ) );
                    b.append( format( "{\n" ) );
                    b.append( format( "  \"%s\": \"%s\",\n", "name", function.name() ) );
                    b.append( format( "  \"%s\": \"%s\"\n", "args", function.getArguments() ) );
                    b.append( format( "  \"%s\": \"%s\",\n", "fullname", function.fullname() ) );
                    b.append( format( "}\n" ) );
                    b.append( "\n" );
                    b.append( format( "  " + function.fullname() + "(%s);\n",
                            function.hasArguments()
                                    ? ( " " + function.getArguments() + " " )
                                    : "" ) );
                    b.append( "\n" );
                    b.append( format( "this.%s = %s\n", function.name(), function.path() ) );


                    resultTextArea.setText( b.toString() );
                }
                else if ( item.isProperty() )
                {
                    ModelProperty field = ( ModelProperty ) item;

                    Object v = field.getValue();

                    Class< ? > clazz = null;

                    if ( v != null )
                    {
                        clazz = v.getClass();

                        if ( v instanceof String )
                        {
                            v = "\"" + ( ( String ) v ).replaceAll( "\"", "\\\"" ) + "\"";

                        }
                    }

                    StringBuilder b = new StringBuilder();

                    b.append( format( "/* PROPERTY */\n" ) );
                    b.append( format( "{\n" ) );
                    b.append( format( "  \"%s\": \"%s\",\n", "name", field.name() ) );
                    b.append( format( "  \"%s\": %s\n", "value", v ) );
                    b.append( format( "  \"%s\": %s\n", "class", clazz ) );
                    b.append( "}\n" );


                    resultTextArea.setText( b.toString() );
                }
                else if ( item.isObject() )
                {
                    ModelObject object = ( ModelObject ) item;

                    StringBuilder b = new StringBuilder();

                    b.append( "item = {\n" );
                    b.append( format( "   \"%s\": \"%s\",\n", "name", object.name() ) );
                    b.append( format( "   \"%s\": \"%s\",\n", "fullname", object.fullname() ) );

                    if ( object.hasXPath() )
                    {
                        b.append( format( "   \"%s\": \"%s\"\n", "path", object.path() ) );

                        b.append( format( "   \"%s\": %s,\n", "$active", object.get( "$active" ) ) );
                        b.append( format( "   \"%s\": \"%s\",\n", "$actions", object.get( "$actions" ) ) );
                        b.append( format( "   \"%s\": %s,\n", "$hash", object.get( HASH_KEY ) ) );
                        b.append( format( "   \"%s\": %s,\n", "$key", object.get( "$key" ) ) );

                        if ( object.has( "$leaf" ) )
                        {
                            b.append( format( "   \"%s\": %s,\n", "$leaf", object.get( "$leaf" ) ) );
                        }

                        b.append( format( "   \"%s\": \"%s\",\n", "$xpath", object.get( "$xpath" ) ) );
                    }
                    b.append( "};\n" );


                    resultTextArea.setText( b.toString() );
                }

            } );
        }

    };

    public ModelTreePane( ContextManager contextManager )
    {
        this( contextManager, null );
    }


    public ModelTreePane( ContextManager contextManager, String sessionKey )
    {
        this.context = contextManager.getUnit().newContext();

        contextManager.addNewUnitListener( ( unit ) ->
        {
            context = unit.newContext();

            buildContextTree();
        } );

        this.sessionKey = sessionKey;


        refreshModelButton.setId( "refreshModelButton" );
        modelTreeView.setId( "modelTreeView" );
        resultTextArea.setId( "resultTextArea" );

        // scaffolding
        treeBox = new VBox( 5 );
        VBox scriptResultAreaBox = new VBox( 5 );


        VBox.setVgrow( modelTreeView, Priority.ALWAYS );
        VBox.setVgrow( resultTextArea, Priority.ALWAYS );


        modelTreeView.setEditable( false );

        modelTreeView.setCellFactory( ( p ) ->
        {
            return new ModelObjectTreeCell();
        } );

        modelTreeView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener( changeListener );


        // buildTree();
        treeBox
                .getChildren()
                .addAll( modelTreeView );

        scriptResultAreaBox
                .getChildren()
                .addAll(
                        resultTextArea );

        // now me
        setOrientation( Orientation.VERTICAL );
        setDividerPositions( 0.7f, 0.3f );

        getItems()
                .addAll(
                        treeBox,
                        scriptResultAreaBox );


        refreshModelButton.setOnAction( ( event ) ->
        {
            Inspector.execute( "Reload", () ->
            {
                buildContextTree();
            } );
        } );

        activateModelButton.setOnAction( ( event ) ->
        {
            Inspector.execute( "Activate Models", () ->
            {
                Inspector.execute( "activate-session-models", () ->
                {
                    context.activateModels();

                    buildContextTree();
                } );
            } );
        } );


        activatedCheckBox.setSelected( buildActiveOnly );
        activatedCheckBox.setOnAction( ( event ->
        {
            buildActiveOnly = activatedCheckBox.isSelected();

            Inspector.execute( "Reload", () ->
            {
                buildContextTree();
            } );
        } ) );


        synchAwtEventCheckBox.setSelected( false );
        synchAwtEventCheckBox.setOnAction( ( event ->
        {
            boolean selected = synchAwtEventCheckBox.isSelected();

            Inspector.execute( "synchAwtEventCheckBox_attachListener", () ->
            {
                context.getSessions().stream().forEach( session ->
                {
                    hashPipe.setSessionListener( session, selected );
                } );
            } );
        } ) );

        synchHashCheckBox.setSelected( false );

        synchHashCheckBox.setOnAction( ( event ->
        {
            boolean synchHash = synchHashCheckBox.isSelected();

            Inspector.execute( "listenForSnapshots", () ->
            {
                context.getSessions().stream().forEach( session ->
                {
                    session.getDriver().setListenForSnapshots( synchHash );
                } );
            } );
        } ) );


        findByHashButton.setOnAction( ( event ) ->
        {
            selectByHash( findByHashTextField.getText() );
        } );


        HBox.setHgrow( findByHashTextField, Priority.ALWAYS );

        withToolbarNodes(
                refreshModelButton,
                activatedCheckBox,
                synchAwtEventCheckBox,
                synchHashCheckBox,
                activateModelButton,
                findByHashButton,
                findByHashTextField );
    }


    private ToolBar toolbar = null;


    public ModelTreePane withToolbarNodes( javafx.scene.Node... nodes )
    {
        if ( nodes != null && nodes.length > 0 )
        {
            if ( toolbar == null )
            {
                toolbar = new ToolBar();
            }

            toolbar
                    .getItems()
                    .addAll( nodes );

            if ( ! treeBox.getChildren().contains( toolbar ) )
            {
                treeBox.getChildren().add( 0, toolbar );
            }
        }

        return this;
    }


    private final EventHandler< ActionEvent > actionCall = new EventHandler< ActionEvent >()
    {
        public void handle( ActionEvent t )
        {
            ModelMember mo = modelTreeView.getSelectionModel().getSelectedItem().getValue();

            if ( ! ( mo instanceof ModelFunction ) )
            {
                return;
            }

            ModelFunction mf = ( ModelFunction ) mo;

            final Object[][] arguments = { null };

            if ( mf.hasArguments() )
            {
                Optional< String > value = new ModelObjectDialog()
                        .withOwner(
                                ModelTreePane.this
                                        .getScene()
                                        .getWindow() )
                        .withModality( Modality.APPLICATION_MODAL )
                        .withTitle( "Set parameters for " + mf.name() )
                        .withHeaderText( "Enter comma separated values for the parameters (no quotes)" )
                        .withArgumentsText( mf.getArguments() )
                        .showAndWait();

                if ( ! value.isPresent() )
                {
                    return;
                }

                // the trimmed text values can be passed directly
                // without conversions
                // thanks to the magic of nashorn

                // NB: trick alert!
                // arguments[ 0 ] holds an array

                String argText = value.get().trim();

                // but might be an object
                // so can only handle one argument that is a literal object
                if ( argText.startsWith( "{" ) )
                {
                    arguments[ 0 ] = new String[]{ argText };
                }
                else
                {
                    arguments[ 0 ] = value.get().split( "\\s*,\\s*" );
                }
            }


            Inspector.execute( format( "Calling %s()", mf.name() ), () ->
            {

                try
                {
                    Object result = mf.call( arguments[ 0 ] );

                    Platform.runLater( () ->
                    {
                        resultTextArea.setText( "" + result );
                    } );
                }
                catch ( Exception e )
                {
                    resultTextArea.setText( Inspector.stackTraceToString( e ) );

                    Inspector.errorAlert( "Error calling function.", e );
                }

            } );
        }
    };

    private boolean buildActiveOnly = false;

    private ModelObjectTree.Include< ModelMember > includer = modelMember -> {
        if ( ! buildActiveOnly )
        {
            return true;
        }
        else if ( ! ( modelMember instanceof ModelObject ) )
        {
            return false;
        }

        ModelObject mo = ( ModelObject ) modelMember;

        // never the root and first level
        if ( mo.ancestor() == null )
        {
            return true;
        }

        // otherwise only if active
        return mo.get( "$active" ) != null;
    };


    public void buildContextTree()
    {
        String[] selectedhash = { null };

        TreeItem< ModelMember > selectedItem = modelTreeView
                .getSelectionModel()
                .getSelectedItem();

        if ( selectedItem != null && selectedItem.getValue() != null && selectedItem.getValue().isObject() )
        {
            ModelObject mo = ( ModelObject ) selectedItem.getValue();
            selectedhash[ 0 ] = ( String ) mo.get( HASH_KEY );
        }

        TreeItem< ModelMember > rootItem = new TreeItem< ModelMember >( null );

        if ( sessionKey != null )
        {
            String modelKey = context.getSession( sessionKey ).getModel().getName();

            Object sessionModel = context.getBindings().get( modelKey );

            if ( sessionModel == null )
            {
                throw new RuntimeException( format( "No adapter found with name [%s] for session [%s].", modelKey, sessionKey ) );
            }
            else if ( ! ( sessionModel instanceof ScriptObjectMirror ) )
            {
                throw new RuntimeException( format( "Model [%s] is not an instance of ScriptObjectMirror.", sessionModel ) );
            }

            // note we pass the session name and not the adapter name
            rootItem
                    .getChildren()
                    .add(
                            ModelObjectTree
                                    .buildTree(
                                            sessionKey,
                                            ( ScriptObjectMirror ) sessionModel,
                                            includer ) );
        }
        else
        {
            context
                    .getBindings()
                    .entrySet()
                    .stream()
                    .filter( entry ->
                    {
                        return entry.getValue() instanceof ScriptObjectMirror;
                    } )
                    .forEach( ( entry ) ->
                    {
                        rootItem
                                .getChildren()
                                .add(
                                        ModelObjectTree
                                                .buildTree(
                                                        entry.getKey(),
                                                        ( ScriptObjectMirror ) entry.getValue(),
                                                        includer ) );

                    } );
        }


        Runnable r = () ->
        {
            rootItem.setExpanded( true );
            modelTreeView.setShowRoot( false );
            modelTreeView.setRoot( rootItem );

            if ( selectedhash[ 0 ] != null )
            {
                selectByHash( selectedhash[ 0 ] );
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

    public ScriptObjectMirror getSomTree()
    {
        return somTree;
    }


    public void buildSOMTree( ScriptObjectMirror sessionModel )
    {
        somTree = sessionModel;

        String[] selectedhash = { null };

        TreeItem< ModelMember > selectedItem = modelTreeView
                .getSelectionModel()
                .getSelectedItem();

        if ( selectedItem != null && selectedItem.getValue() != null && selectedItem.getValue().isObject() )
        {
            ModelObject mo = ( ModelObject ) selectedItem.getValue();
            selectedhash[ 0 ] = ( String ) mo.get( HASH_KEY );
        }

        TreeItem< ModelMember > rootItem = new TreeItem< ModelMember >( null );

        String modelKey = context.getSession( sessionKey ).getModel().getName();

        rootItem
                .getChildren()
                .add(
                        ModelObjectTree
                                .buildTree(
                                        modelKey,
                                        sessionModel,
                                        includer ) );

        Runnable r = () -> {
            rootItem.setExpanded( true );
            modelTreeView.setShowRoot( false );
            modelTreeView.setRoot( rootItem );

            if ( selectedhash[ 0 ] != null )
            {
                selectByHash( selectedhash[ 0 ] );
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

    public void selectByHash( String hash )
    {
        Inspector.execute( "select-by-hash", () ->
        {
            if ( hash == null || hash.isEmpty() )
            {
                return;
            }

            TreeItem< ModelMember > item = findByHash( hash );

            if ( item != null )
            {
                Platform.runLater( () ->
                {
                    modelTreeView
                            .scrollTo(
                                    modelTreeView
                                            .getRow( item ) );

                    modelTreeView
                            .getSelectionModel()
                            .select( item );

                    item.setExpanded( true );

                } );
            }
        } );
    }


    private class ModelObjectTreeCell extends TextFieldTreeCell< ModelMember >
    {
        private ContextMenu addMenu = new ContextMenu();

        private MenuItem callMenuItem = new MenuItem( "Call..." );

        public ModelObjectTreeCell()
        {
            callMenuItem.setOnAction( actionCall );
        }

        public void updateItem( ModelMember item, boolean empty )
        {
            super.updateItem( item, empty );

            addMenu
                    .getItems()
                    .clear();

            setContextMenu( addMenu );

            if ( item == null )
            {
                return;
            }

            if ( item.isFunction() )
            {
                addMenu
                        .getItems()
                        .addAll(
                                callMenuItem );

                setTextFill( Color.CHOCOLATE );
            }
            else if ( item.isObject() )
            {
                ModelObject mo = ( ModelObject ) item;

                if ( mo.has( "$active" ) )
                {
                    setTextFill( Color.BLACK );
                }
                else
                {
                    setTextFill( Color.DARKGRAY );
                    setText( getText() + " (inactive)" );
                }
            }
            else
            {
                setTextFill( Color.BLUE );
            }
        }
    }
}


