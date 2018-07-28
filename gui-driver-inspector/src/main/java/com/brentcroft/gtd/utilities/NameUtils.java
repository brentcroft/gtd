package com.brentcroft.gtd.utilities;

import com.brentcroft.util.StringUpcaster;
import com.brentcroft.util.TextUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.w3c.dom.Element;

import static com.brentcroft.gtd.driver.Backend.NAME_ATTRIBUTE;
import static com.brentcroft.gtd.driver.Backend.NAME_CLAIMS_ATTRIBUTE;
import static com.brentcroft.gtd.driver.Backend.STAR;
import static com.brentcroft.util.StringUpcaster.downcastMap;
import static com.brentcroft.util.StringUpcaster.downcastMapList;
import static java.lang.String.format;

/**
 * Created by Alaric on 28/10/2016.
 */
public class NameUtils
{
    public final static String NAME_EXCLUDED_REGEX_TOKENS = " \\!\"#%\\&'\\(\\)\\*\\+\\,\\-\\.\\/\\:\\;\\<\\=\\>\\?@\\[\\\\\\]\\^\\_`\\{\\|\\}\\~";

    public final static String NAME_EXCLUDED_LOW_BYTES = "[\\u0000-\\u001F]";
    public final static String NAME_EXCLUDED_HIGH_BYTES = "[\\u00EF-\\uFFFF]";


    enum EXTRACTOR_REGEX
    {
        URL( "(([^:/?#]+):)?(//([^/?#]*))?([^?#]*/(?=\\w))*(?<%1$s>[^?#/]*)[/]*(\\?([^#]*))?(#(.*))?" );

        String regexRaw;

        EXTRACTOR_REGEX( String regexRaw )
        {
            this.regexRaw = regexRaw;
        }

        public Pattern buildPattern( String attrName )
        {
            return Pattern.compile( format( regexRaw, attrName ) );
        }

        public static Pattern getPattern( String attrName, String regex )
        {
            return Stream
                    .of( values() )
                    .filter( v -> v.name().equalsIgnoreCase( regex ) )
                    .findFirst()
                    .map( extractor_regex -> extractor_regex.buildPattern( attrName ) )
                    .orElseGet( () -> Pattern.compile( regex ) );
        }
    }


    private final Map< String, String > tagMappings = new LinkedHashMap<>();
    private Map< String, List< ? extends Namer > > includeTagAttributes = new LinkedHashMap<>();


    public NameUtils withTagMappings( String tagMappingText )
    {
        if ( tagMappingText != null && ! tagMappingText.isEmpty() )
        {
            tagMappings.putAll( StringUpcaster.upcastMap( tagMappingText ) );
        }
        return this;
    }

    public NameUtils withTagAttributes( String tagAttributes )
    {
        Map< String, List< String > > info = new LinkedHashMap<>();
        StringUpcaster.upcastMapList( tagAttributes, info );

        info
                .entrySet()
                .forEach( entry -> {

                    List< Namer > namers = new ArrayList<>();

                    entry
                            .getValue()
                            .forEach( attrName -> {

                                String[] parts = attrName.split( "#" );

                                if ( parts.length == 1 )
                                {
                                    // simple namer
                                    namers.add( new Namer( parts[ 0 ] ) );
                                }
                                else// regex namer
                                {
                                    // check for reference

                                    Function< String, String >[] functions = IntStream
                                            .range( 1, parts.length )
                                            .mapToObj( i -> EXTRACTOR_REGEX.getPattern( parts[ 0 ], parts[ i ] ) )
                                            .map( p -> {
                                                Function< String, String > x = ( n ) -> {
                                                    Matcher m = p.matcher( n );
                                                    return m.matches()
                                                            ? m.group( parts[ 0 ] )
                                                            : null;
                                                };
                                                return x;
                                            } )
                                            .toArray( Function[]::new );

                                    namers.add(
                                            new Namer(
                                                    parts[ 0 ],
                                                    String.join( "#", parts ),
                                                    functions ) );
                                }
                            } );

                    includeTagAttributes.put( entry.getKey(), namers );
                } );

        return this;
    }


    public static String getNameExcludedRegexTokens()
    {
        return "[" + NAME_EXCLUDED_REGEX_TOKENS
               + NAME_EXCLUDED_LOW_BYTES
               + NAME_EXCLUDED_HIGH_BYTES + "]+";
    }

    public static String cleanName( String name )
    {
        if ( name == null || name.trim().length() < 1 )
        {
            throw new RuntimeException( "Name cannot be null or empty." );
        }

        String candidate = TextUtils.removeListDuplicates(
                TextUtils.replaceAll(
                        name,
                        getNameExcludedRegexTokens(),
                        "_" ),
                "_",
                "_" )
                .trim();

        if ( candidate == null || candidate.length() < 1 )
        {
            throw new RuntimeException( "Unable to create valid name from: " + name );
        }


        // not allowed to begin with a digit
        if ( Character.isDigit( candidate.charAt( 0 ) ) )
        {
            return "d_" + candidate;
        }

        return candidate;
    }


    public static boolean isValid( String name )
    {
        try
        {
            cleanName( name );
            return true;
        }
        catch ( Exception e )
        {
            return false;
        }
    }


    class Namer
    {
        String attrName;
        String info;
        Function< String, String >[] extractors;

        Namer( String attrName, String info, Function< String, String >... extractors )
        {
            this.attrName = attrName;
            this.info = info;
            this.extractors = ( extractors == null || extractors.length == 0 || extractors[ 0 ] == null )
                    ? null
                    : extractors;
        }

        Namer( String attrName )
        {
            this( attrName, null, ( Function< String, String > ) null );
        }

        public String getAttrName()
        {
            return attrName;
        }

        public String extract( Element element )
        {
            String value = element.getAttribute( attrName );
            return ( extractors == null || extractors.length == 0 || extractors[ 0 ] == null )
                    ? value
                    : Stream
                            .of( extractors )
                            .filter( Objects::nonNull )
                            .map( extractor -> extractor.apply( value ) )
                            .filter( Objects::nonNull )
                            .findFirst()
                            .orElse( null );
        }

        public String toString()
        {
            return ( info == null ? attrName : info );
        }
    }


    public String buildName( Element e )
    {
        // detect a name that was previously calculated; e.g. by label lookup
        if ( e.hasAttribute( NAME_ATTRIBUTE ) )
        {
            return e.getAttribute( NAME_ATTRIBUTE );
        }


        final String tag = e.getTagName();

        final String namingTag = includeTagAttributes.containsKey( tag )
                ? tag
                : includeTagAttributes.containsKey( STAR )
                        ? STAR
                        : null;

        // naming tag attributes are most preferred
        if ( namingTag != null && includeTagAttributes.containsKey( namingTag ) )
        {
            for ( final Namer namer : includeTagAttributes.get( namingTag ) )
            {
                if ( "text()".equals( namer.getAttrName() ) )
                {
                    final String textContent = e.getTextContent();

                    if ( ! isEmpty( textContent ) && isValid( textContent ) )
                    {
                        return textContent;
                    }
                }
                else
                {
                    final String attrValue = namer.extract( e );

                    if ( ! isEmpty( attrValue ) && isValid( attrValue ) )
                    {
                        return attrValue;
                    }
                }
            }
        }

        // fixed tag mappings
        if ( tagMappings.containsKey( tag ) )
        {
            return tagMappings.get( tag );
        }

        // assume tag can always be converted to a valid name
        return tag;
    }

    /**
     * TODO: write to Element UserData
     *
     * @param element
     * @return
     */
    public static boolean reserveForNaming( Element element )
    {
        int claims = element.hasAttribute( NAME_CLAIMS_ATTRIBUTE )
                ? Integer.valueOf( element.getAttribute( NAME_CLAIMS_ATTRIBUTE ) )
                : 0;

        claims++;

        element.setAttribute( NAME_CLAIMS_ATTRIBUTE, "" + claims );

        return claims == 1;
    }


    public static boolean unreserveForNaming( Element element )
    {
        if ( ! element.hasAttribute( NAME_CLAIMS_ATTRIBUTE ) )
        {
            return true;
        }

        int claims = Integer.valueOf( element.getAttribute( NAME_CLAIMS_ATTRIBUTE ) );

        claims--;

        if ( claims == 0 )
        {
            element.removeAttribute( NAME_CLAIMS_ATTRIBUTE );
        }
        else
        {
            element.setAttribute( NAME_CLAIMS_ATTRIBUTE, "" + claims );
        }

        return claims == 0;
    }


    public static String maybeReverseTranslate( String text )
    {
        String tx = TextUtils.reverseTranslate( text );
        return tx == null || tx.isEmpty() ? text : tx;
    }


    public static String getKeyHashCode( String key )
    {
        if ( key == null )
        {
            return null;
        }

        final int hashCode = key.hashCode();

        return "#" + ( hashCode < 0 ? "" : "+" ) + hashCode;
    }


    public static boolean isEmpty( Object o )
    {
        return o == null || o.toString().trim().isEmpty();
    }

    public String toString()
    {
        StringBuilder b = new StringBuilder();

        b.append( format( "tagMap=%s%n", TextUtils.indentMap( this.tagMappings ) ) );
        b.append( format( "include=%s%n", TextUtils.indentMap( this.includeTagAttributes ) ) );

        return b.toString();
    }

    public NameUtils configure( Properties p )
    {
        Arrays
                .asList( Property.values() )
                .stream()
                .forEach( property -> {
                    property.read( this, p );
                } );

        return this;
    }

    public NameUtils export( Properties p )
    {
        Arrays
                .asList( Property.values() )
                .stream()
                .forEach( property -> {
                    property.write( this, p );
                } );

        return this;
    }

    public enum Property
    {
        KEY_NAME_TAG_MAP( "tagMap" )
                {
                    @Override
                    void read( NameUtils nu, Properties p )
                    {
                        nu.tagMappings.clear();
                        nu.withTagMappings( p.getProperty( attribute ) );
                    }

                    @Override
                    void write( NameUtils nu, Properties p )
                    {
                        p.setProperty( attribute, downcastMap( nu.tagMappings ) );
                    }
                },

        KEY_NAME_INCLUDE( "include" )
                {
                    @Override
                    void read( NameUtils nu, Properties p )
                    {
                        nu.includeTagAttributes.clear();
                        nu.withTagAttributes( p.getProperty( attribute ) );
                    }

                    @Override
                    void write( NameUtils nu, Properties p )
                    {
                        Map< String, List< String > > info = new LinkedHashMap<>();

                        nu
                                .includeTagAttributes
                                .forEach( ( key, value ) -> {
                                    List< String > namerText = new ArrayList<>();
                                    value.forEach( attrName -> namerText.add( attrName.toString() ) );
                                    info.put( key, namerText );
                                } );

                        p.setProperty( attribute, downcastMapList( info ) );
                    }
                },;

        final static String ATTRIBUTE_PREFIX = "modeller.naming";
        final String attribute;

        Property( String attribute )
        {
            this.attribute = ATTRIBUTE_PREFIX + "." + attribute;
        }

        abstract void read( NameUtils nu, Properties p );

        abstract void write( NameUtils nu, Properties p );
    }
}
