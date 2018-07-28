package com.brentcroft.util;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.String.format;

/**
 * Created by Alaric on 10/07/2017.
 */
public class TextUtils
{
    private final static transient Logger logger = Logger.getLogger( TextUtils.class.getName() );

    private static Locale currentLocale = Locale.getDefault();


    private static ResourceBundle rb = null;
    private static Map< String, String > forward = new HashMap< String, String >();
    private static Map< String, String > reverse = new HashMap< String, String >();
    private static Map< String, Set< String > > reverseChoices = new HashMap< String, Set< String > >();

    private static Map< String, String > newNames = new HashMap< String, String >();

    public static void setBundle( String baseName, String languageTag )
    {
        currentLocale = Locale.forLanguageTag( languageTag );
        Locale.setDefault( currentLocale );

        ResourceBundle.clearCache();

        rb = ResourceBundle.getBundle( baseName );

        logger.info( format( "Loaded bundle: %s (locale=%s).", baseName, languageTag ) );

        // allow overwrite by file
        forward.putAll( newNames );

        for ( String key : rb.keySet() )
        {
            if ( key != null && ! key.isEmpty() )
            {
                final String text = rb.getString( key );


                if ( text != null && ! text.isEmpty() )
                {
                    if ( reverse.containsKey( text ) )
                    {
                        // multiple keys for text
                        Set< String > choices = reverseChoices.get( text );

                        if ( choices == null )
                        {
                            choices = new HashSet< String >();
                            // put the original one in
                            choices.add( reverse.get( text ) );
                            // and the new one
                            reverseChoices.put( text, choices );
                        }

                        choices.add( key );

                        //logger.warn( format( "Added new tx key [%s] for text [%s]: choices=%s.", key, text, choices ) );
                    }
                    else
                    {
                        reverse.put( text, key );
                    }
                }

                // already there, isn't it.
                // since we putAll?
                forward.put( key, text );
            }
        }

        //logger.warn( format( "Duplicate tx choice: %s", reverseChoices ) );
    }

    public static String translate( String key )
    {
        return forward == null ? "" : forward.get( key );
    }

    public static String reverseTranslate( String text )
    {
        return reverse == null ? "" : reverse.get( text );
    }

    public static boolean isTranslated( String text )
    {
        return reverse == null ? false : reverse.containsKey( text );
    }


    public static String replaceAll( String text, String regex, String replacement )
    {
        return text.replaceAll( regex, replacement );
    }

    public static String removeListDuplicates( String text, String separatorRegex, String listSeparator )
    {
        StringBuilder b = new StringBuilder();

        for ( String s : new LinkedHashSet< String >( Arrays.asList( text.split( separatorRegex ) ) ) )
        {
            if ( b.length() > 0 )
            {
                b.append( listSeparator );
            }
            b.append( s );
        }

        return b.toString();
    }

    public static String getValue( Properties p, String key )
    {
        return getValueOrDefault( p, key, null );
    }


    public static String getValueOrDefault( Properties p, String key, String defaultValue )
    {
        if ( p == null )
        {
            return null;
        }

        return p.getProperty( key, defaultValue );
    }

    public static String getKey( Properties p, String key )
    {
        return getKeyOrDefault( p, key, null );
    }


    public static String getKeyOrDefault( Properties p, String key, String defaultValue )
    {
        if ( p == null )
        {
            return null;
        }

        for ( Map.Entry< Object, Object > entry : p.entrySet() )
        {
            if ( entry.getValue().equals( key ) )
            {
                Object value = entry.getKey();
                return value == null ? null : value.toString();
            }
        }

        return defaultValue;
    }

    public static Map< String, String > getTranslations()
    {
        return forward;
    }

    public static Map< String, String > getReverseTranslations()
    {
        return reverse;
    }

    public static void storeName( String tx, String value )
    {
        if ( tx == null || tx.isEmpty() )
        {
            if ( logger.isLoggable( Level.FINEST ) )
            {
                logger.log( Level.FINEST, "Ignoring empty name: [" + tx + "] = " + value );
            }
            return;
        }

        newNames.put( tx, value );

        forward.put( tx, value );

        // don't reverse engineer new names
        // otherwise may create unintentional shadows
        //reverse.put( value, tx );

        if ( logger.isLoggable( Level.FINEST ) )
        {
            logger.log( Level.FINEST, "Created name: [" + tx + "] = " + value );
        }
    }

    public static Map< String, String > getNewNames()
    {
        return newNames;
    }

    public static String printNewNames()
    {
        StringBuilder b = new StringBuilder( "# new names " + new Date() );

        for ( String key : newNames.keySet() )
        {
            b.append( "\n" ).append( key ).append( "=" ).append( newNames.get( key ).replaceAll( "[\\n\\r]+", "\\$1  " ) );
        }

        return b.toString();
    }


    public static String indentList( Iterable< ? > text )
    {
        return indentList( text, "  " );
    }

    public static String indentList( Iterable< ? > text, String indent )
    {
        if ( text == null)
        {
            return null;
        }

        StringBuilder b = new StringBuilder( "\n" );

        for ( Object t : text )
        {
            b
                    .append( indent( "" + t, indent ) )
                    .append( "\n" );
        }

        return b.toString();
    }

    public static String indentMap( Map< ?, ? > text )
    {
        return indentMap( text, "  " );
    }

    public static String indentMap( Map< ?, ? > text, String indent )
    {
        StringBuilder b = new StringBuilder();

        for ( Map.Entry< ?, ? > t : text.entrySet() )
        {
            b
                    .append( indent )
                    .append( t.getKey() )
                    .append( "=" )
                    .append( t.getValue() )
                    .append( "\n" );
        }

        return b.toString();
    }

    public static String indent( String text )
    {
        return indent( text, "  " );
    }


    public static String indent( String text, String indent )
    {
        return indent( text, indent, "\n" );
    }

    /**
     * sep is used as both a regex and a separator
     * so do not use any regex actions!
     * Just plain text i.e. line separator.
     *
     * @param text
     * @param indent
     * @param sep
     * @return
     */
    public static String indent( String text, String indent, String sep )
    {
        StringBuilder b = new StringBuilder();
        boolean isFirst = true;
        for ( String s : text.split( sep ) )
        {
            if ( isFirst )
            {
                isFirst = false;
            }
            else
            {
                b.append( sep );
            }
            b.append( indent + s );
        }
        return b.toString();
    }

}
