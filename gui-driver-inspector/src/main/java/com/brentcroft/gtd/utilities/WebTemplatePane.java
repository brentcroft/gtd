package com.brentcroft.gtd.utilities;

import com.brentcroft.gtd.inspector.Inspector;
import com.brentcroft.util.FileUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class WebTemplatePane extends VBox
{
    public static final String DEFAULT_TEMPLATE_FILENAME = "html/defaultEditor-template.html";
    public static final String PROPERTIES_TEMPLATE_FILENAME = "html/propertiesEditor-template.html";
    public static final String XML_TEMPLATE_FILENAME = "html/xmlEditor-template.html";
    public static final String JS_TEMPLATE_FILENAME = "html/codeEditor-template.html";
    public static final String GHERKIN_TEMPLATE_FILENAME = "html/gherkinEditor-template.html";

    public enum TemplateType
    {
        text( DEFAULT_TEMPLATE_FILENAME ),
        properties( PROPERTIES_TEMPLATE_FILENAME ),
        gherkin( GHERKIN_TEMPLATE_FILENAME ),
        javascript( JS_TEMPLATE_FILENAME ),
        xml( XML_TEMPLATE_FILENAME );

        final String uri;

        TemplateType( String uri )
        {
            this.uri = uri;
        }

        public static TemplateType getTypeForUri( String templateUri )
        {
            for ( TemplateType tt : values() )
            {
                if ( tt.uri != null && tt.uri.equals( templateUri ) )
                {
                    return tt;
                }
            }
            return null;
        }

        public String getUri()
        {
            return uri;
        }
    }


    private ToolBar toolbar = null;

    private Button printButton = new Button( "print" );

    private ComboBox< TemplateType > templateTypes = new ComboBox<>( FXCollections.observableArrayList( TemplateType.values() ) );

    private CodeEditor codeEditor;

    private TemplateType templateType = null;

    private String editingCode;

    /**
     * if the code has been edited since last saved
     */
    public boolean isDirty()
    {
        return codeEditor.isDirty();
    }


    /**
     * Tells the CodeEditor that the current code
     */
    public void setClean()
    {
        codeEditor.setClean();
    }


    public static WebTemplatePane newXmlEditor( String text )
    {
        //return new WebTemplatePane( XML_TEMPLATE_FILENAME, text );
        return new WebTemplatePane( DEFAULT_TEMPLATE_FILENAME, text );
    }

    public static WebTemplatePane newJsEditor( String text )
    {
        return new WebTemplatePane( JS_TEMPLATE_FILENAME, text );
    }

    public static WebTemplatePane newGherkinEditor( String text )
    {
        return new WebTemplatePane( GHERKIN_TEMPLATE_FILENAME, text );
    }

    public static WebTemplatePane newPropertiesEditor( String text )
    {
        return new WebTemplatePane( PROPERTIES_TEMPLATE_FILENAME, text );
    }

    public WebTemplatePane( String templateUri, String text )
    {
        printButton.setOnAction( ( event ) -> print() );

        templateTypes.setOnAction( ( event ) ->
        {
            withTemplateType(
                    templateTypes
                            .getSelectionModel()
                            .getSelectedItem() );
        } );

        withDefaultButtons();

        installCodeEditor( templateUri, text );
    }

    private void installCodeEditor( String templateUri, String code )
    {
        if ( codeEditor != null )
        {
            getChildren()
                    .remove(
                            codeEditor );

            codeEditor = null;
        }

        // renew
        templateType = TemplateType.getTypeForUri( templateUri );

        codeEditor = new CodeEditor(
                FileUtils
                        .getResourceAsUrl( templateUri )
                        .toExternalForm(),
                code );

        codeEditor.setId( "codeEditor" );

        VBox.setVgrow( codeEditor, Priority.ALWAYS );

        getChildren()
                .add(
                        codeEditor );


        if ( templateType != templateTypes.getSelectionModel().getSelectedItem() )
        {
            // this shouldn't recurse uncontrollably
            Platform.runLater( () -> templateTypes.getSelectionModel().select( templateType ) );
        }

    }


    public void setText( String text )
    {
        codeEditor.setCode( text );
    }


    public String getText()
    {
        return codeEditor.getCode();
    }


    public void selectRange( int start, int end )
    {
        // not implemented
    }

    public WebTemplatePane withToolbarNodes( Node... nodes )
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

            if ( ! getChildren().contains( toolbar ) )
            {
                getChildren().add( 0, toolbar );
            }
        }

        return this;
    }

    public WebTemplatePane withoutToolbarNodes( Node... nodes )
    {
        if ( nodes != null && nodes.length > 0 && toolbar != null )
        {
            toolbar
                    .getItems()
                    .removeAll(
                            toolbar
                                    .getItems()
                                    .filtered( node -> {
                                        for ( int i = 0, n = nodes.length; i < n; i++ )
                                        {
                                            if ( nodes[ i ] == node )
                                            {
                                                return true;
                                            }
                                        }
                                        return false;
                                    } )
                    );
        }

        return this;
    }

    public WebTemplatePane withDefaultButtons()
    {
        // only install if not already there
        if ( toolbar == null
             || ! toolbar
                .getItems()
                .stream()
                .anyMatch( n -> n == printButton ) )
        {
            withToolbarNodes( printButton, templateTypes );
        }

        return this;
    }


    public void print()
    {
        Inspector.execute( "print", () -> codeEditor.print() );
    }


    public WebTemplatePane withTemplateType( TemplateType newTemplateType )
    {
        if ( newTemplateType != null && newTemplateType != templateType )
        {
            Platform.runLater( () ->
            {
                installCodeEditor(
                        newTemplateType
                                .getUri(),
                        codeEditor
                                .getCode() );
            } );

        }
        return this;
    }
}


