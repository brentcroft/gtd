package com.brentcroft.gtd.inspector.panel.modeller;

import com.brentcroft.gtd.inspector.ContextManager;
import com.brentcroft.util.CommentedProperties;
import com.brentcroft.util.DateUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import static java.lang.String.format;

@SuppressWarnings( "restriction" )
public class PropertiesPaneActions
{
    public final static String SRC_KEY = "$src";

    private Stage primaryStage;
    private File currentDirectory;
    private File currentFile;

    public PropertiesPaneActions( ContextManager contextManager )
    {
        this.primaryStage = contextManager.getPrimaryStage();
        this.currentDirectory = contextManager.getCurrentDirectory();
    }

    public boolean save( CommentedProperties properties )
    {
        return save( properties, null );
    }

    public boolean save( CommentedProperties properties, String propertiesText )
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle( "Save Properties" );
        fileChooser
                .getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter( "Properties", "*.properties" ),
                        new FileChooser.ExtensionFilter( "All Files", "*.*" ) );

        if ( currentFile == null && properties.containsKey( SRC_KEY ) )
        {
            currentFile = new File( currentDirectory, properties.getProperty( SRC_KEY ) );
        }


        if ( currentFile != null )
        {
            fileChooser.setInitialFileName( currentFile.getName() );
            fileChooser.setInitialDirectory( currentFile.getParentFile() );
        }
        else if ( currentDirectory != null )
        {
            fileChooser.setInitialDirectory( currentDirectory );
        }

        currentFile = fileChooser.showSaveDialog( primaryStage );

        if ( currentFile == null || ! currentFile.exists() )
        {
            return false;
        }

        currentDirectory = currentFile.getParentFile();

        if ( propertiesText != null )
        {
            // $raw - just write out the given text
            try ( OutputStream os = new BufferedOutputStream( new FileOutputStream( currentFile ) ) )
            {
                os.write( propertiesText.getBytes( "UTF8" ) );
                return true;
            }
            catch ( IOException e )
            {
                throw new RuntimeException( e );
            }
        }
        else
        {
            try ( OutputStream os = new BufferedOutputStream( new FileOutputStream( currentFile ) ) )
            {
                // any strict key has been copied
                if ( properties.isStrict() )
                {
                    properties.store( os, format( "created: %s", DateUtils.timestamp() ) );
                }
                else
                {
                    os.write( properties.downcast().getBytes( Charset.forName( "UTF8" ) ) );
                    os.flush();
                }
                return true;
            }
            catch ( IOException e )
            {
                throw new RuntimeException( e );
            }
        }
    }


    public boolean open( CommentedProperties properties )
    {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle( "Open Properties" );
        fileChooser
                .getExtensionFilters()
                .addAll(
                        new FileChooser.ExtensionFilter( "Properties", "*.properties" ),
                        new FileChooser.ExtensionFilter( "All Files", "*.*" ) );

        if ( currentFile == null && properties.containsKey( SRC_KEY ) )
        {
            currentFile = new File( currentDirectory, properties.getProperty( SRC_KEY ) );
        }


        if ( currentFile != null )
        {
            fileChooser.setInitialFileName( currentFile.getName() );
            fileChooser.setInitialDirectory( currentFile.getParentFile() );
        }
        else if ( currentDirectory != null )
        {
            fileChooser.setInitialDirectory( currentDirectory );
        }

        currentFile = fileChooser.showOpenDialog( primaryStage );

        if ( currentFile == null )
        {
            return false;
        }

        currentDirectory = currentFile.getParentFile();

        if ( currentFile.exists() )
        {
            try ( InputStream is = new BufferedInputStream( new FileInputStream( currentFile ) ) )
            {
                CommentedProperties p = new CommentedProperties(  );

                p.load( is );

                properties.replaceWith( p );

                return true;
            }
            catch ( IOException e )
            {
                throw new RuntimeException( e );
            }
        }
        else
        {
            throw new RuntimeException( "Selected file does not exist: " + currentFile );
        }
    }
}
