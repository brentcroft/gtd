package com.brentcroft.util.templates.el;

import java.io.Console;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Random;

public class ELFunctions
{
    public static String padLeft(String text, char pad, int width)
    {
        StringBuffer b = new StringBuffer(text == null ? "" : text.trim());

        while (b.length() < width)
        {
            b.insert( 0, pad );
        }

        return b.toString();
    }

    public static String padRight(String text, char pad, int width)
    {
        StringBuffer b = new StringBuffer(text == null ? "" : text.trim());

        while (b.length() < width)
        {
            b.append( pad );
        }

        return b.toString();
    }


    public static String replaceAll(String source, String regex, String rep)
    {
        return source.replaceAll( regex, rep );
    }


    public static Float boxFloat( Float f )
    {
        return f;
    }


    public static boolean fileExists( String filename)
    {
        return new File(filename).exists();
    }


    public static String bytesAsString( byte[] bytes )
    {
        return new String( bytes );
    }

    public static String bytesAsString( byte[] bytes, String charset ) throws UnsupportedEncodingException
    {
        return new String( bytes, charset );
    }


    public static Random random()
    {
        return new Random();
    }

    public static String console( String prompt, String defaultValue )
    {
        Console console = System.console();

        if ( console == null )
        {
            return defaultValue;
        }

        return console.readLine( prompt );
    }

    public static char[] consolePassword( String prompt, char[] defaultValue )
    {
        Console console = System.console();

        if ( console == null )
        {
            return defaultValue;
        }

        return console.readPassword( prompt );
    }


    public static void consoleFormat( String format, Object... args )
    {
        Console console = System.console();

        if ( console == null )
        {
            return;
        }

        console.format( format, args );
    }
}
