package com.brentcroft.util.tools;

import static java.lang.String.format;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Scanner;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


/**
 * Utility class providing static methods for reading files and URLs, and basic
 * DOM functions.
 * <p/>
 *
 * Note that many of these methods are designed to be bound into function
 * mappers in UEL & xpath.
 *
 * @author ADobson
 *
 */
public class StreamTools
{
    private static Logger logger = Logger.getLogger( StreamTools.class.getCanonicalName() );

    private static StreamTools instance;

    static
    {
        // force initialisation
        instance = new StreamTools();
    }


    /**
     * Normalize a uri to a local URL.
     * <p/>
     * If clazz.getClassLoader().getResource( <code>templateUri</code>) produces
     * a URL then return it,
     * <p/>
     * otherwise
     * <p/>
     * return new File(<code>templateUri</code> ).toURI().toURL().
     *
     * @param filepath
     *            the unexpanded uri
     * @param clazz
     *            the class whose class-loader should first be used to try to
     *            expand the uri
     * @return the local file URL derived from the templateUri
     */
    public static URL getLocalFileURL( Class<?> clazz, String filepath )
    {
        final URL url = clazz.getClassLoader().getResource( filepath );

        if ( url != null )
        {
            {
                final Level level = Level.DEBUG;

                if ( logger.isEnabledFor( level ) )
                {
                    logger.log( level, "Got URL from class loader: " + url );
                }
            }

            return url;
        }

        try
        {
            return new File( filepath ).toURI().toURL();
        }
        catch ( MalformedURLException e )
        {
            throw new RuntimeException( e );
        }
    }


    public static URL getFileURL( String filepath )
    {
        return getLocalFileURL( instance.getClass(), filepath );

    }


    public static String readUrl( URL url )
    {
        try ( Scanner scanner = new Scanner( url.openStream() ) )
        {
            scanner.useDelimiter( "\\A" );

            return scanner.hasNext() ? scanner.next() : "";
        }
        catch ( Exception e )
        {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException( e );
        }
    }

    public static String readLocalFile( Class<?> clazz, String filepath )
    {
        return readUrl( getLocalFileURL( clazz, filepath ) );
    }

    public static String readFile( String filepath )
    {
        return readLocalFile( instance.getClass(), filepath );
    }


    public static void writeStringToFile( String s, String filename, Charset charset )
    {
        BufferedWriter writer = null;

        try
        {

            writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( filename ), charset ) );

            writer.write( s );

            writer.flush();

            {
                final Level level = Level.DEBUG;

                if ( logger.isEnabledFor( level ) )
                {
                    logger.log( level, "Wrote string to file: " + filename );
                }
            }
        }
        catch ( Exception e )
        {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException( e );
        }
        finally
        {
            if ( writer != null )
            {
                try
                {
                    writer.close();
                }
                catch ( Exception e )
                {
                    throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException( e );
                }
            }
        }
    }

    public static void writeStringToFile( String s, String filename )
    {
        FileWriter fos = null;

        try
        {
            fos = new FileWriter( filename );

            fos.write( s );

            {
                final Level level = Level.DEBUG;

                if ( logger.isEnabledFor( level ) )
                {
                    logger.log( level, "Wrote string to file: " + filename );
                }
            }
        }
        catch ( Exception e )
        {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException( format( "Failed to write string [%s] to file [%s].", s, filename ), e );
        }
        finally
        {
            if ( fos != null )
            {
                try
                {
                    fos.close();
                }
                catch ( Exception e )
                {
                    throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException( e );
                }
            }
        }
    }


    public static void writeBytesToFile( byte[] b, String filename )
    {
        FileOutputStream fos = null;

        try
        {
            fos = new FileOutputStream( filename );

            fos.write( b, 0, b.length );

            {
                final Level level = Level.DEBUG;

                if ( logger.isEnabledFor( level ) )
                {
                    logger.log( level, "Wrote bytes to file: " + filename );
                }
            }
        }
        catch ( Exception e )
        {
            throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException( e );
        }
        finally
        {
            if ( fos != null )
            {
                try
                {
                    fos.close();
                }
                catch ( Exception e )
                {
                    throw e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException( e );
                }
            }
        }
    }


    /**
     * For use in XSLT etc, as won't throw an exception.
     *
     * @param s
     * @param filename
     * @return
     */
    public static boolean stringToFile( String s, String filename )
    {
        try
        {
            writeStringToFile( s, filename );

            return true;
        }
        catch ( Exception e )
        {
            e.printStackTrace();

            return false;
        }
    }
}
