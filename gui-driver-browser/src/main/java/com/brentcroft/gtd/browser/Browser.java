package com.brentcroft.gtd.browser;

import java.io.IOException;
import java.net.URL;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static java.lang.String.format;


/**
 * @author Alvin Tabontabon
 * @see "https://gist.github.com/TabsPH/4142805"
 */
public class Browser extends Application
{
    private static String TITLE = "Brentcroft Browser";
    private static int INST = 0;

    @Override
    public void start( Stage stage ) throws Exception
    {
        stage.setTitle( format( "%s[%s]", TITLE, INST ) );

        newBrowser( stage );
    }


    public static URL getResourceAsUrl( String path )
    {
        return Browser.class
                .getClassLoader()
                .getResource( path );
    }


    public static WebUIController newBrowser( Stage stage ) throws IOException
    {
        FXMLLoader loader = new FXMLLoader( getResourceAsUrl( "fxml/WebUI.fxml" ) );

        Scene scene = new Scene( loader.load() );

        if ( stage == null )
        {
            stage = new Stage();
        }

        stage.setTitle( format( "%s[%s]", TITLE, INST ) );

        stage.setScene( scene );

        stage.show();

        return loader.< WebUIController > getController();
    }


    public static void main( String[] args )
    {
        launch( args );
    }
}
