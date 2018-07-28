package com.brentcroft.gtd.browser;


import com.brentcroft.gtd.events.DOMEventHandler;
import com.brentcroft.gtd.events.DOMEventSource;
import com.brentcroft.gtd.events.DOMEventUtils;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.events.EventTarget;

import static com.brentcroft.gtd.browser.Wc3BrowserModel.downloadURLToFile;
import static com.brentcroft.gtd.browser.Wc3BrowserModel.getCandidateFilename;
import static com.brentcroft.gtd.browser.Wc3BrowserModel.maybeChooseLocalFile;
import static java.lang.String.format;
import static javafx.concurrent.Worker.State.FAILED;
import static javafx.concurrent.Worker.State.SUCCEEDED;


public class WebUIController implements Initializable
{
    private final static transient Logger logger = Logger.getLogger( WebUIController.class );

    private TextInputControlStream statusStream;

    private final DOMEventUtils domEventUtils = new DOMEventUtils();

    private final DOMEventHandler listener = e -> {

        // TODO: causing a few crashes!!
        // not on FX thread??
        if ( Platform.isFxApplicationThread() )
        {
            String type = e.getType();
            EventTarget t = e.getTarget();
            EventTarget ct = e.getCurrentTarget();

            // assume are xml nodes
            Node node = ( Node ) t;

            log( format( "dom: type=[%s] %s",
                    type,
                    node == null ? "" : node.getNodeName() ) );
        }
        else
        {
            log( format( "dom: not on FX event thread" ) );
        }
    };

    private boolean isAborting = false;


    private String[] downloadableExtensions = {
            ".doc",
            ".docx",
            ".pdf",
            ".xls",
            ".odt",
            ".zip",
            ".tgz",
            ".jar",
            ".csv",
            ".ashx"
    };

    @FXML
    TextField url;

    @FXML
    BorderPane window;

    @FXML
    WebView document;

    @FXML
    TextArea console;

    @FXML
    VBox status;


    private WebEngine webEngine;

    @FXML
    private void goAction()
    {
        loadURL( getUrl() );
    }

    @FXML
    private void statusClear()
    {
        Platform.runLater( () -> console.clear() );
    }


    @FXML
    private void statusEvents( ActionEvent evt )
    {
        Platform.runLater( () -> {
            if ( evt.getSource() instanceof CheckBox )
            {
                CheckBox cb = ( CheckBox ) evt.getSource();
                if ( cb.isSelected() )
                {
                    domEventUtils.addListener( listener );
                }
                else
                {
                    domEventUtils.removeListener( listener );
                }
            }
        } );
    }

    private void doDownload( String uri )
    {
        maybeChooseLocalFile( uri, ( s, f ) -> {
            installTrussty();
            downloadURLToFile( s, f );
            log( format( "download: to=[%s], from=[%s]", f, uri ) );
        } );
    }


    private ChangeListener< ? super String > locationPropertyListener = ( observable, oldLocation, newLocation ) ->
    {
        if ( isDownload( newLocation ) && ! isAborting )
        {
            doDownload( newLocation );
        }
        else
        {
            log( format( "jump: to=[%s], from=[%s]", newLocation, oldLocation ) );

            url.setText( newLocation );
        }
    };

    private Callback< PopupFeatures, WebEngine > popupCallback = ( popupFeatures ) ->
    {
        log( format(
                "popup: resizable=[%s], menu=[%s], toolbar=[%s], status=[%s].",
                popupFeatures.isResizable(),
                popupFeatures.hasMenu(),
                popupFeatures.hasToolbar(),
                popupFeatures.hasStatus() ) );

        WebView popupWebView;

        try
        {
            if ( popupFeatures.isResizable() && popupFeatures.hasMenu() && popupFeatures.hasToolbar() && popupFeatures.hasStatus() )
            {
                popupWebView = Browser.newBrowser( null ).getPage();
            }
            else
            {
                popupWebView = new WebView();

                initialiseEngine( popupWebView.getEngine(), true );
            }
        }
        catch ( IOException e )
        {
            logError( e );

            return null;
        }

        return popupWebView.getEngine();
    };


    private void logError( Throwable throwable )
    {
        Optional
                .ofNullable( throwable )
                .ifPresent( t -> {

                    Optional
                            .ofNullable( throwable.getMessage() )
                            .map( String::trim )
                            .filter( s -> ! s.isEmpty() )
                            .ifPresent( s -> statusStream
                                    .getPrintWriter()
                                    .println( s ) );

                    t.printStackTrace( statusStream.getPrintWriter() );
                } );
    }

    private void logError( String message, Throwable throwable )
    {
        Optional
                .ofNullable( message )
                .map( String::trim )
                .filter( s -> ! s.isEmpty() )
                .ifPresent( s -> statusStream
                        .getPrintWriter()
                        .println( s ) );

        logError( throwable );
    }


    private void log( String message )
    {
        logError( message, null );
    }


    @Override
    public void initialize( URL url, ResourceBundle rb )
    {
        SplitPane.setResizableWithParent( status, false );

        // intercept a download from a link with a target
        String filename = getCandidateFilename( url.toExternalForm() );
        if ( isDownload( filename ) )
        {
            // abort and close
            isAborting = true;

            Stage stage = ( Stage ) document.getScene().getWindow();
            stage.close();

            return;
        }


        // decorate the console text field
        statusStream = new TextInputControlStream(
                console,
                Charset.defaultCharset(),
                logger );


        webEngine = document.getEngine();

        initialiseEngine( webEngine, true );
    }

    private void initialiseEngine( WebEngine webEngine, boolean allowPopups )
    {
        webEngine
                .locationProperty()
                .addListener( locationPropertyListener );


        webEngine.setOnAlert( event -> new Alert( Alert.AlertType.INFORMATION, event.getData() ).show() );

        //webEngine.setOnStatusChanged( event -> log( format( "status: %s", event ), null ) );

        webEngine.setOnError( event -> logError( format( "error: [%s] %s", event.getEventType(), event.getMessage() ), event.getException() ) );


        webEngine.setConfirmHandler( info -> new Alert( Alert.AlertType.CONFIRMATION, info )
                .showAndWait()
                .map( response -> response == ButtonType.OK )
                .orElse( false ) );

        //handle popup windows
        if ( allowPopups )
        {
            webEngine.setCreatePopupHandler( popupCallback );
        }

        webEngine.getLoadWorker()
                .exceptionProperty()
                .addListener( ( ov, oldState, exception ) ->
                        {
                            if ( webEngine.getLoadWorker().getState() == FAILED )
                            {
                                String message = format(
                                        "error: loading [%s] %s",
                                        webEngine.getLocation(),
                                        ( exception != null
                                                ? exception.getMessage()
                                                : "unexpected: " + ov ) );

                                logError( message, exception );

                                new Alert( Alert.AlertType.ERROR, message ).show();
                            }
                        }
                );

        webEngine
                .getLoadWorker()
                .stateProperty()
                .addListener( ( ov, oldState, newState ) ->
                {
                    if ( newState == SUCCEEDED )
                    {
                        installListeners();
                    }
                } );
    }


    private void installListeners()
    {
        DOMEventSource.notifyNewDocument( webEngine.getDocument() );
    }


    private boolean isDownload( String newValue )
    {
        for ( String ext : downloadableExtensions )
        {
            if ( newValue.endsWith( ext ) )
            {
                return true;
            }
        }
        return false;
    }


    public String getUrl()
    {
        final String address = url.getText();

        if ( address == null )
        {
            throw new NullPointerException( "url is null." );
        }

        return
                address.startsWith( "http://" ) || address.startsWith( "https://" )
                        ? url.getText()
                        : "http://" + url.getText();
    }


    /**
     * see: http://java-tech-world.blogspot.co.uk/2016/01/how-to-load-https-url-from-java-fx.html
     *
     * @param url the url to load
     */
    private void loadURL( final String url )
    {
        installTrussty();

        Platform.runLater( () -> document.getEngine().load( url ) );
    }


    private void installTrussty()
    {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager()
                {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }

                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType )
                    {
                    }

                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType )
                    {
                    }
                }
        };


        // Install the all-trusting trust manager
        try
        {
            SSLContext sc = SSLContext.getInstance( "SSL" );

            sc.init( null, trustAllCerts, new java.security.SecureRandom() );

            HttpsURLConnection.setDefaultSSLSocketFactory( sc.getSocketFactory() );
        }
        catch ( GeneralSecurityException e )
        {
            logError( e );
        }

    }

    public WebView getPage()
    {
        return document;
    }
}