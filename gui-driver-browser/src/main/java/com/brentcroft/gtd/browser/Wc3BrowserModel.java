package com.brentcroft.gtd.browser;

import static java.lang.String.format;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.function.BiConsumer;

import javax.swing.JFileChooser;

import javafx.application.Platform;

public class Wc3BrowserModel
{
    //private final static transient Logger logger = Logger.getLogger( Wc3BrowserModel.class );

    /**
     * see: http://java-tech-world.blogspot.co.uk/2016/01/how-to-load-https-url-from-java-fx.html
     *
     * @param url
     */
    public static void downloadURLToFile( final String url, File saveFile )
    {
        Platform.runLater( () -> {

            try
            {
                try (
                        BufferedInputStream is = new BufferedInputStream( new URL( url ).openStream() );
                        BufferedOutputStream os = new BufferedOutputStream( new FileOutputStream( saveFile ) ) )
                {
                    int b = is.read();
                    while ( b != - 1 )
                    {
                        os.write( b );
                        b = is.read();
                    }
                }
            }
            catch ( Exception e )
            {
                throw new RuntimeException( format( "Error downloading to file [%s] from url [%s]: %s", saveFile, url, e ), e );
            }
        } );
    }


    public static void maybeChooseLocalFile( String url, BiConsumer< String, File > fileSaver )
    {
        String filename = getCandidateFilename( url );

        final JFileChooser fileChooser = new JFileChooser();

        fileChooser.setDialogTitle( "Download url: " + url);

        File directory = new File( "." );
        File candidate = new File( directory, filename );

        fileChooser.setCurrentDirectory( directory );
        fileChooser.setSelectedFile( candidate );

        if ( JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog( null ) )
        {
            fileSaver.accept( url, fileChooser.getSelectedFile() );
        }
    }


    public static String getCandidateFilename( String url )
    {
        int filenameIdx = url.lastIndexOf( "/" ) + 1;

        return ( filenameIdx < 0 )
                ? url
                : url.substring( filenameIdx );
    }
}
