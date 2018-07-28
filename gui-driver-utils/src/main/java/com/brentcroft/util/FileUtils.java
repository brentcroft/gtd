package com.brentcroft.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Scanner;

import static java.lang.String.format;

/**
 * Created by adobson on 13/07/2016.
 */
public class FileUtils
{

    public static String getFileAsString( File file )
    {
        try ( Scanner scanner = new Scanner( file, "UTF-8" ) )
        {
            return scanner.useDelimiter( "\\Z" ).next();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( format( "Failed reading jvmFile [%s]: %s", file, e ), e );
        }
    }


    public static URL getResourceAsUrl( String path )
    {
        return FileUtils.class
                .getClassLoader()
                .getResource( path );
    }

    public static String getResourceAsString( String path )
    {
        return getStreamAsString( getResourceAsStream( path ) );
    }


    public static Reader getResourceAsReader( String path )
    {
        return new InputStreamReader( getResourceAsStream( path ) );
    }


    public static String getFileOrResourceAsString( File parent, String path )
    {
        try
        {
            return getFileAsString( resolvePath( parent, path ) );
        }
        catch ( Exception e1 )
        {
            try
            {
                return getStreamAsString( getResourceAsStream( path ) );
            }
            catch ( Exception e2 )
            {
                throw new RuntimeException( format( "Failed to find [%s]; tried as jvmFile and resource.", path ) );
            }
        }
    }


    public static String getStreamAsString( InputStream input )
    {
        StringBuilder b = new StringBuilder();

        try ( BufferedReader buffer = new BufferedReader( new InputStreamReader( input ) ) )
        {
            String s = buffer.readLine();
            while ( s != null )
            {
                b.append( s ).append( "\n" );
                s = buffer.readLine();
            }

            return b.toString();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( format( "Failed reading stream: %s", e ), e );
        }
    }


    public static InputStream getFileOrResourceAsStream( File parent, String path )
    {
        try
        {
            return new FileInputStream( resolvePath( parent, path ) );
        }
        catch ( Exception e1 )
        {
            try
            {
                return getResourceAsStream( path );
            }
            catch ( Exception e2 )
            {
                throw new RuntimeException( format( "Failed to find [%s]; tried as file and resource.", path ) );
            }
        }
    }


    public static Reader getFileOrResourceAsReader( File parent, String path )
    {
        return new InputStreamReader( getFileOrResourceAsStream( parent, path ) );
    }


    public static InputStream getResourceAsStream( String path )
    {
        InputStream is = FileUtils.class
                .getClassLoader()
                .getResourceAsStream( path );

        if ( is == null )
        {
            throw new RuntimeException( "Resource not found: " + path );
        }

        return is;
    }


    static class JvmBaseFile
    {
        private final File jvmFile;
        private final String jvmPath;
        private final int jvmPathLength;

        public JvmBaseFile()
        {
            try
            {
                jvmFile = new File( "." )
                        .getCanonicalFile();

                jvmPath = jvmFile.getPath();
                jvmPathLength = jvmPath.length();
            }
            catch ( IOException e )
            {
                throw new RuntimeException( "Failed to canonicalize JVM_BASE_FILE.", e );
            }
        }

        public String getPath()
        {
            return jvmPath;
        }


        public String relativizePath( String path )
        {
            int p = path.indexOf( jvmPath );

            if ( p < 0 || ( path.length() <= jvmPathLength ) )
            {
                return path;
            }
            else
            {
                // + 1 to include the inevitable separator
                return path.substring( p + jvmPathLength + 1 );
            }
        }

    }

    public static final JvmBaseFile JVM_BASE_FILE = new JvmBaseFile();


    public static String relativizePath( File file )
    {
        return JVM_BASE_FILE.relativizePath( file.getAbsolutePath() );
    }

    public static File resolvePath( File parent, String path )
    {
        return parent == null ? new File( path ) : new File( parent, path );
    }

    public static String resolveAndRelativizePath( File parent, String path )
    {
        return relativizePath( resolvePath( parent, path ) );
    }
}
