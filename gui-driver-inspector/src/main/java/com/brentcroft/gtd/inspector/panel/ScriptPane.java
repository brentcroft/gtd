package com.brentcroft.gtd.inspector.panel;

import com.brentcroft.gtd.inspector.ContextManager;
import com.brentcroft.gtd.inspector.Inspector;
import com.brentcroft.gtd.js.context.Context;
import com.brentcroft.gtd.utilities.WebTemplatePane;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.stage.FileChooser;

@SuppressWarnings( "restriction" )
public class ScriptPane extends CodeEditorPane implements InspectorPane
{
    public static final String TEMPLATE_FILENAME = WebTemplatePane.JS_TEMPLATE_FILENAME;

   
	private final Button scriptExecuteButton = new Button( "Execute" );
    private final Button refreshButton = new Button( "Refresh" );
    //private final Button layerButton = new Button( "Layer" );
    //private final TextField layerKeyTextField = new TextField( "newLayer" );

    private Context context;


    public static ScriptPane newScriptPane( ContextManager contextManager, Tab tab, String code )
    {
        return new ScriptPane(
                contextManager,
                tab,
                WebTemplatePane.JS_TEMPLATE_FILENAME,
                code );
    }


    public ScriptPane( ContextManager contextManager, Tab tab, String templateUri, String code )
    {
        super(
                tab,
                templateUri,
                code );

        this.context = contextManager.getUnit().newContext();

        contextManager.addNewUnitListener( (unit)->{
            this.context = unit.newContext();
        } );


        init();
    }



    private void init()
    {
        scriptExecuteButton.setId( "scriptExecuteButton" );

        withToolbarNodes(
                scriptExecuteButton,
                refreshButton );

        // actions
        scriptExecuteButton.setOnAction( actionExecuteScript );
        refreshButton.setOnAction( actionRefresh );
        //layerButton.setOnAction( actionLayer );
    }


    private final EventHandler< ActionEvent > actionRefresh = new EventHandler< ActionEvent >()
    {
        @Override
        public void handle( ActionEvent event )
        {
            setCode( getCode() );
        }
    };



//    private final EventHandler< ActionEvent > actionLayer = new EventHandler< ActionEvent >()
//    {
//        @Override
//        public void handle( ActionEvent event )
//        {
//            // get the code now - on the FX thread
//            String layerKeyText = layerKeyTextField.getText();
//            String layerCode = getCode();
//
//
//            Inspector.execute( "Set layer", () -> {
//
//                try
//                {
//                    JSGuiLocalDriver driver = getSessionPane()
//                            .getGuiSession()
//                            .getDriver();
//
//                    driver
//                            .getPathUnit()
//                            .setLayer(
//                                    layerKeyText,
//                                    layerCode );
//
//                    setResultText( format( "Set layer [%s].", layerKeyText ) );
//                }
//                catch ( Exception e )
//                {
//                    e.printStackTrace();
//                }
//
//            } );
//        }
//    };


    private final EventHandler< ActionEvent > actionExecuteScript = new EventHandler< ActionEvent >()
    {
        private Pattern lineNoPattern = Pattern.compile( "line number (\\d+)" );
        private Pattern colNoPattern = Pattern.compile( "column number (\\d+)" );
        private Pattern lineEndingPattern = Pattern.compile( ".*\\s*[\\n|\\r]+\\s*" );


        @Override
        public void handle( ActionEvent event )
        {
            // get the code now - on the FX thread
            String code = getCode();


            Inspector.execute( "Execute script", () -> {

                try
                {
                    final Object resultObject = context.execute(code);

                    final String result = ( resultObject == null )
                            ? "(null)"
                            : resultObject
                                    .toString()
                                    .isEmpty()
                                            ? "(empty)"
                                            : resultObject
                                                    .toString();

                    setResultText( result, false );

                }
                catch ( Exception e )
                {

                    String st = Inspector.stackTraceToString( e );

                    try
                    {
                        Matcher lineMatcher = lineNoPattern.matcher( st );
                        Matcher colMatcher = colNoPattern.matcher( st );



                        Integer lineNo = lineMatcher.find() ? Integer.valueOf( lineMatcher.group( 1 ) )
                                : null;
                        Integer colNo = colMatcher.find() ? Integer.valueOf( colMatcher.group( 1 ) ) : null;


                        if ( lineNo != null )
                        {
                            String text = getCode();

                            Matcher lineEndingMatcher = lineEndingPattern.matcher( text );

                            final int[] lineStart = { 0 };

                            int[] lineSize = { 0 };
                            int i = 1;
                            int n = lineNo;

                            // line by line, including line endings
                            while ( lineEndingMatcher.find() && ( i <= n ) )
                            {
                                lineSize[ 0 ] = ( lineEndingMatcher.end() - lineEndingMatcher.start() );

                                if ( i < n )
                                {
                                    lineStart[ 0 ] += lineSize[ 0 ];
                                }

                                i++;
                            }

                            if ( colNo != null )
                            {
                                // scriptArea.selectRange( lineStart[ 0 ], lineStart[ 0 ] + colNo );
                                //
                                // Platform.runLater( new Runnable()
                                // {
                                // public void run()
                                // {
                                // scriptArea.selectRange( lineStart[ 0 ], lineStart[ 0 ] + colNo );
                                // }
                                // } );
                            }
                            else
                            {
                                // Platform.runLater( new Runnable()
                                // {
                                // public void run()
                                // {
                                // scriptArea.selectRange( lineStart[ 0 ],
                                // lineStart[ 0 ] + lineSize[ 0 ] );
                                // }
                                // } );
                            }
                        }
                    }
                    catch ( Exception ignored )
                    {

                    }

                    setResultText( st, true );
                }


            } );

            setResultText( "Javascript dispatched...", false );
        }
    };


    @Override
    protected FileChooser getFileSaveChooser()
    {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle( "Save Javascript File" );

        fileChooser
                .getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter( "Javascript", "*.js" ),
                        new FileChooser.ExtensionFilter( "All Files", "*.*" ) );
        return fileChooser;
    }


    @Override
    protected FileChooser getFileOpenChooser()
    {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle( "Open Javascript File" );

        fileChooser
                .getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter( "Javascript", "*.js" ),
                        new FileChooser.ExtensionFilter( "All Files", "*.*" ) );
        return fileChooser;
    }

}
