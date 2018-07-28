package com.brentcroft.util;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.brentcroft.util.StringUpcaster.DelimiterEntity.COMMA;
import static com.brentcroft.util.StringUpcaster.DelimiterEntity.EQUALS;
import static com.brentcroft.util.StringUpcaster.DelimiterEntity.PIPE;
import static java.lang.String.format;

/**
 * Utility class for up-casting String values to other desired Types.
 * <p/>
 *
 * @author ADobson
 */
public class StringUpcaster
{
    /**
     * Delimiters used by StringUpcaster in strings being downcast are replaced with entities,
     * and in strings being upcast any entities are replaced with the corresponding delimiter.<p/>
     */
    public enum DelimiterEntity
    {
        COMMA( ",", "&comma;" ),
        EQUALS( "=", "&equals;" ),
        PIPE( "|", "\\|", "&pipe;" );

        private final String delim, delimRegex, entity;

        DelimiterEntity( String delim, String delimRegex, String entity )
        {
            this.delim = delim;
            this.delimRegex = delimRegex;
            this.entity = entity;
        }

        public String sep()
        {
            return delim;
        }

        public String regex()
        {
            return delimRegex;
        }

        DelimiterEntity( String delim, String entity )
        {
            this( delim, delim, entity );
        }

        public String entitise( String text )
        {
            return text == null ? null : text.replaceAll( delimRegex, entity );
        }

        public String unentitise( String text )
        {
            return text == null ? null : text.replaceAll( entity, delim );
        }
    }

    public static < T > T upcast( final String value, final Class< T > type )
    {
        return upcast( value, type, null );
    }


    public static < T > T upcast( final String value, final Class< T > type, String defaultValue )
    {
        if ( type == null )
        {
            throw new NullPointerException( "type cannot be null" );
        }

        if ( value != null )
        {
            return convert( type, value );
        }
        else if ( defaultValue != null )
        {
            return convert( type, defaultValue );
        }
        else
        {
            return null;
        }
    }

    public static < T > T upcast( final String value, T defaultValue )
    {
        if ( defaultValue == null )
        {
            throw new NullPointerException( "defaultValue cannot be null" );
        }

        if ( value != null )
        {
            return convert( ( Class< T > ) defaultValue.getClass(), value );
        }
        else if ( defaultValue != null )
        {
            return defaultValue;
        }
        else
        {
            return null;
        }
    }

    public static < T > T[] upcast( final String[] values, final Class< T > type )
    {
        if ( type == null )
        {
            throw new NullPointerException( "type cannot be null" );
        }

        if ( values != null )
        {
            @SuppressWarnings( "unchecked" ) final T[] a = ( T[] ) Array.newInstance( type, values.length );

            for ( int i = 0, n = values.length; i < n; i++ )
            {
                a[ i ] = convert( type, COMMA.unentitise( values[ i ] ) );
            }

            return a;
        }
        else
        {
            return null;
        }
    }


    @SuppressWarnings( { "unchecked", "rawtypes" } )
    private static < T > T convert( final Class< T > type, final String rawValue )
    {
        if ( type == String.class )
        {
            return type.cast( rawValue );
        }

        if ( type == Integer.class )
        {
            return type.cast( Integer.valueOf( rawValue ) );
        }
        else if ( type == Long.class )
        {
            return type.cast( Long.valueOf( rawValue ) );
        }
        else if ( type == Boolean.class )
        {
            return type.cast( Boolean.valueOf( rawValue ) );
        }
        else if ( type == Double.class )
        {
            return type.cast( Double.valueOf( rawValue ) );
        }
        else if ( type == Float.class )
        {
            return type.cast( Float.valueOf( rawValue ) );
        }
        else if ( type == Short.class )
        {
            return type.cast( Short.valueOf( rawValue ) );
        }
        else if ( type == Byte.class )
        {
            return type.cast( Byte.valueOf( rawValue ) );
        }
        else if ( type == TimeUnit.class )
        {
            return type.cast( TimeUnit.valueOf( rawValue ) );
        }
        else if ( type.isEnum() )
        {
            // Casting 'Enum.valueOf( (Class<Enum>) type, rawValue )' to 'Enum' is redundant
            // but otherwise project build reports an error
            //noinspection RedundantCast
            return type.cast( ( Enum ) Enum.valueOf( ( Class< Enum > ) type, rawValue ) );
        }


        throw new IllegalArgumentException( "No conversion class found for type [" + type.getCanonicalName() + "]" );
    }


    public static < T > T[] upcastSplit( final String value, final String regex, final Class< T > type, T[] defaultValue )
    {
        if ( value != null )
        {
            return upcast( value.split( regex ), type );
        }
        else
        {
            return defaultValue;
        }
    }


    public static String[] upcastSplit( final String value, String[] defaultValue )
    {
        final String itemRegex = "\\s*,\\s*";
        if ( value != null )
        {
            return upcast( value.split( itemRegex ), String.class );
        }
        else
        {
            return defaultValue;
        }
    }


    /**
     * @param listText
     * @return
     */
    public static List< String > upcastList( String listText )
    {
        final String itemRegex = "\\s*,\\s*";
        return upcastList( listText, itemRegex );
    }


    /**
     * @param listText
     * @return
     */
    public static List< String > upcastList( String listText, String itemRegex )
    {
        List< String > list = new ArrayList<>();

        if ( listText == null || listText.isEmpty() )
        {
            return list;
        }

        for ( String entry : listText.trim().split( itemRegex ) )
        {
            if ( entry.isEmpty() )
            {
                continue;
            }

            list.add( entry.trim() );
        }

        return list;
    }

    /**
     * @param setText
     * @return
     */
    public static Set< String > upcastSet( String setText )
    {
        final String itemRegex = "\\s*,\\s*";
        return upcastSet( setText, itemRegex );
    }

    public static Set< String > upcastSetAdd( String setText, Set< String > set )
    {
        final Set< String > newSet = upcastSet( setText );

        newSet.addAll( set );

        return newSet;
    }


    /**
     * @param setText
     * @return
     */
    public static Set< String > upcastSet( String setText, String itemRegex )
    {
        Set< String > set = new LinkedHashSet<>();

        if ( setText == null || setText.isEmpty() )
        {
            return set;
        }

        Arrays
                .stream( setText.trim().split( itemRegex ) )
                .filter( Objects::nonNull )
                .map( COMMA::unentitise )
                .forEach( set::add );

        return set;
    }


    public static String downcastMap( Map< ? extends Object, ? extends Object > map )
    {
        return map
                .entrySet()
                .stream()
                .map( entry -> format( "%s%s%s", entry.getKey(), EQUALS.sep(), entry.getValue() ) )
                .sorted()
                .collect( Collectors.joining( COMMA.sep() ) );
    }

    public static Map< String, String > upcastMap( String mapText )
    {
        final String itemRegex = format( "\\s*%s\\s*", COMMA.sep() );
        final String keyValueRegex = format( "\\s*%s\\s*", EQUALS.sep() );

        return upcastMap( mapText, itemRegex, keyValueRegex );
    }


    /**
     * Downcasts a Properties to a string representation where every entry is on one line.
     *
     * @param p a Properties
     * @return a string representation where every entry is on one line
     */
    public static String downcastProperties( CommentedProperties p )
    {
        return p
                .entrySet()
                .stream()
                .map( entry -> format(
                        "%s=%s",
                        entry.getKey(),
                        entry.getValue().toString().contains( "=" )
                                ? Stream
                                .of( entry.getValue().toString().split( COMMA.sep() ) )
                                .collect( Collectors.joining( COMMA.sep() + " \\\n\t" ) )
                                : entry.getValue()
                ) )
                .sorted()
                .collect( Collectors.joining( "\n" ) );
    }

    public static CommentedProperties upcastProperties( String mapText )
    {
        CommentedProperties p = new CommentedProperties();
        try
        {
            p.load( new StringReader( mapText ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Failed to upcast properties text.", e );
        }

        return p;
    }


    /**
     * @param mapText
     * @return
     */
    public static Map< String, String > upcastMap( String mapText, String itemRegex, String keyValueRegex )
    {
        Map< String, String > map = new LinkedHashMap<>();

        if ( mapText == null || mapText.isEmpty() )
        {
            return map;
        }

        Arrays
                .stream( mapText.trim().split( itemRegex ) )
                .map( COMMA::unentitise )
                .forEach( entry -> {

                    String[] parts = entry.split( keyValueRegex );

                    if ( parts.length != 2 )
                    {
                        throw new RuntimeException( format( "Bad value [%s]: there must be two parts to each item.", entry ) );
                    }
                    else if ( parts[ 0 ] == null || parts[ 0 ].isEmpty() )
                    {
                        throw new RuntimeException( format( "Bad value [%s]: the first part in the item must not be empty.", entry ) );
                    }
                    else if ( parts[ 1 ] == null || parts[ 1 ].isEmpty() )
                    {
                        throw new RuntimeException( format( "Bad value [%s]: the second part in the item must not be empty.", entry ) );
                    }

                    map.put( parts[ 0 ].trim(), EQUALS.unentitise( parts[ 1 ].trim() ) );
                } );

        return map;
    }


    private static String buildSep( String sep )
    {
        return "\\s*\\" + sep + "\\s*";
    }


    public static String downcastCollection( Collection< String > l )
    {
        // sorted as item order not significant
        return l.stream()
                .map( COMMA::entitise )
                .sorted()
                .collect( Collectors.joining( COMMA.sep() ) );
    }


    public static String downcastMapSet( Map< String, Set< String > > ml )
    {
        return ml.entrySet()
                .stream()
                .map( entry -> format( "%s%s%s",
                        entry.getKey(),
                        EQUALS.sep(),
                        // NB: not sorted as item order is significant
                        entry
                                .getValue()
                                .stream()
                                .map( PIPE::entitise )
                                .collect( Collectors.joining( PIPE.sep() ) ) ) )
                .map( COMMA::entitise )
                .sorted()
                .collect( Collectors.joining( COMMA.sep() ) );
    }


    public static String downcastMapList( Map< String, List< String > > ml )
    {
        return ml.entrySet()
                .stream()
                .map( entry -> format( "%s%s%s",
                        entry.getKey(),
                        EQUALS.sep(),
                        // NB:not sorted as item order is significant
                        entry
                                .getValue()
                                .stream()
                                .map( PIPE::entitise )
                                .collect( Collectors.joining( PIPE.sep() ) ) ) )
                .map( COMMA::entitise )
                .sorted()
                .collect( Collectors.joining( COMMA.sep() ) );
    }


    public static Map< String, List< String > > upcastMapList( String text )
    {
        return upcastMapList( text, new LinkedHashMap<>() );
    }

    public static Map< String, List< String > > upcastMapList( String text, Map< String, List< String > > mapOfListOfString )
    {
        return upcastMapList( text, mapOfListOfString, COMMA.sep(), EQUALS.sep(), PIPE.sep() );
    }

    public static Map< String, List< String > > upcastMapList( String text, String listSep, String partsSep, String itemSep )
    {
        return upcastMapList( text, new LinkedHashMap<>(), listSep, partsSep, itemSep );
    }


    public static Map< String, Set< String > > upcastMapSet( String text )
    {
        return upcastMapSet( text, COMMA.sep(), EQUALS.sep(), PIPE.sep() );
    }

    public static Map< String, Set< String > > upcastMapSet( String text, Map< String, Set< String > > mapOfSetOfString )
    {
        for ( Map.Entry< String, List< String > > entry : upcastMapList( text, COMMA.sep(), EQUALS.sep(), PIPE.sep() ).entrySet() )
        {
            mapOfSetOfString.put( entry.getKey(), new LinkedHashSet<>( entry.getValue() ) );
        }
        return mapOfSetOfString;
    }

    public static Map< String, Set< String > > upcastMapSet( String text, String listSep, String partsSep, String itemSep )
    {
        Map< String, Set< String > > mapSet = new LinkedHashMap<>();
        for ( Map.Entry< String, List< String > > entry : upcastMapList( text, listSep, partsSep, itemSep ).entrySet() )
        {
            mapSet.put( entry.getKey(), new LinkedHashSet<>( entry.getValue() ) );
        }
        return mapSet;
    }

    //COMMA.sep(), EQUALS.sep(), PIPE.sep()
    public static Map< String, List< String > > upcastMapList( String text, Map< String, List< String > > mapOfSetOfString, String listSep, String partsSep, String itemSep )
    {
        final String lsep = buildSep( listSep );
        final String psep = buildSep( partsSep );
        final String isep = buildSep( itemSep );


        if ( text != null && ! text.isEmpty() )
        {
            Arrays
                    .stream( text.trim().split( lsep ) )
                    .map( COMMA::unentitise )
                    .forEach( attrMap -> {

                        String[] parts = attrMap.split( psep );

                        if ( parts.length != 2 )
                        {
                            throw new RuntimeException(
                                    format( "Bad config value: the part [%s] is required to match the regex \"%s\"; for example: \"%s\"",
                                            attrMap,
                                            partsSep,
                                            format(
                                                    "a1 %1$s a2 %2$s t1 %1$s t2 %2$s t3%3$s a3 %1$s a4 %2$s t1 %1$s t2 %1$s t3",
                                                    itemSep,
                                                    partsSep,
                                                    listSep
                                            ) ) );
                        }

                        String tags = parts[ 0 ].trim();
                        String attrs = EQUALS.unentitise( parts[ 1 ].trim() );

                        // can all share the same set
                        // need to unentitise after isep

                        List< String > tagAttrSet = Arrays
                                .stream( attrs.split( isep ) )
                                .map( PIPE::unentitise )
                                .collect( Collectors.toList() );

                        Arrays
                                .stream( tags.split( isep ) )
                                .map( PIPE::unentitise )
                                .forEach( tag -> mapOfSetOfString.put( tag.trim(), tagAttrSet ) );

                    } );
        }

        return mapOfSetOfString;
    }

}
