package com.brentcroft.gtd.utilities;

import com.brentcroft.gtd.driver.Backend;
import com.brentcroft.util.CommentedProperties;
import com.brentcroft.util.NodeVisitor;
import com.brentcroft.util.StringUpcaster;
import com.brentcroft.util.TextUtils;
import com.brentcroft.util.XPathUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import static com.brentcroft.gtd.utilities.NameUtils.cleanName;
import static com.brentcroft.gtd.utilities.NameUtils.isEmpty;
import static com.brentcroft.gtd.utilities.NameUtils.maybeReverseTranslate;
import static com.brentcroft.gtd.utilities.NameUtils.reserveForNaming;
import static com.brentcroft.util.StringUpcaster.downcastCollection;
import static com.brentcroft.util.StringUpcaster.downcastMapList;
import static com.brentcroft.util.StringUpcaster.downcastProperties;
import static com.brentcroft.util.StringUpcaster.upcast;
import static com.brentcroft.util.StringUpcaster.upcastSplit;
import static java.lang.String.format;

/**
 * Created by Alaric on 27/04/2017.
 */
public class KeyUtils
{
    private final static Logger logger = Logger.getLogger( KeyUtils.class );


    private final NameUtils nameUtils = new NameUtils();

    public NameUtils getNameUtils()
    {
        return nameUtils;
    }

    private List< String > primaryAttributes = new ArrayList< String >();

    private boolean positionPredicateAlways = false;
    private boolean positionPredicateIfNoOther = true;
    private Set< String > attributesToTx = new HashSet< String >();
    private boolean txGenerateMissingTranslations = true;
    private boolean txAllowUntranslatedInKeys = false;
    private String txRefFormat = "${ tx[ '%s' ] }";


    /*
     *  If we don't find one in the cache then we only need to look forward
     *  in the document (since we already cached any preceding ones).
     */
    private Map< String, AtomicInteger > encounteredTags = new TreeMap<>();
    private Map< String, Element > encounteredHashFors = new HashMap<>();
    private Map< String, Element > encounteredHtmlFors = new HashMap<>();

    private boolean lookupLabels = false;

    private Set< String > nearestOrSiblingLabels = new HashSet< String >();

    String[][] hashForSiblingXPathComponents = new String[][]{
            { "preceding-sibling", "*[ %s ][ @text and not( @" + Backend.HASH_FOR_ATTRIBUTE + " ) and not( @" + Backend.HTML_FOR_ATTRIBUTE + " ) and not( @claims ) ][ 1 ]" },
            { "following-sibling", "*[ %s ][ @text and not( @" + Backend.HASH_FOR_ATTRIBUTE + " ) and not( @" + Backend.HTML_FOR_ATTRIBUTE + " ) and not( @claims ) ][ 1 ]" }
    };

    String[][] hashForNearestXPathComponents = new String[][]{
            { "../preceding-sibling", "*[ %s ][ @text and not( @for-hash ) and not( @for ) and not( @claims ) ]" },
            { "../following-sibling", "*[ %s ][ @text and not( @for-hash ) and not( @claims ) ]" },
            { "../../preceding-sibling", "*[ %s ][ @text and not( @for-hash ) and not( @claims ) ]" },
            { "../../following-sibling", "*[ %s ][ @text and not( @for-hash ) and not( @claims ) ]" }
    };


    private Map< String, List< String > > tagsAllowedHashForLabels = new LinkedHashMap<>();
    private Map< String, List< String > > tagsAllowedHtmlForLabels = new LinkedHashMap<>();

    private List< List< String > > hashForSiblingXPaths = new ArrayList<>();
    private List< List< String > > hashForNearestXPaths = new ArrayList<>();

    private Map< String, Set< String > > tagsAttributesToParseAsUrl = new LinkedHashMap<>();
    private Set< String > wildcardToParseAsUrl = new HashSet<>();

    // this is calculated and stored here only for reporting
    private String hashForLabelsPredicate;

    private Set< String > tagsAllowedNamesFromSiblingLabels = new LinkedHashSet< String >();
    private Set< String > tagsAllowedNamesFromNearestLabels = new LinkedHashSet< String >();

    //
    private Map< String, Set< String > > attributesExcludedFromKeysForTags = new LinkedHashMap<>();

    public String calculateKeyRaw( Element element )
    {
        encounterElement( element );


        StringBuilder b = new StringBuilder();

        // always the tag name
        b.append( element.getTagName() );

        if ( positionPredicateAlways )
        {
            b.append(
                    format( "[%s]",
                            XPathUtils.Node2XPathFunctions
                                    .getIndexInParent( element ) ) );
        }

        final String c = buildPredicate( element );

        if ( c != null && ! c.isEmpty() )
        {
            b
                    .append( "[" )
                    .append( c )
                    .append( "]" );
        }
        else if ( ! positionPredicateAlways && positionPredicateIfNoOther )
        {
            // if not attributes key then must use position
            b.append(
                    format( "[%s]",
                            XPathUtils.Node2XPathFunctions
                                    .getIndexInParent( element ) ) );
        }

        return b.toString();
    }

    private int encounterElement( Element element )
    {
        AtomicInteger tagCounter = null;

        if ( encounteredTags.containsKey( element.getTagName() ) )
        {
            tagCounter = encounteredTags.get( element.getTagName() );
        }
        else
        {
            tagCounter = new AtomicInteger( 0 );
            encounteredTags.put( element.getTagName(), tagCounter );
        }

        if ( element.hasAttribute( Backend.HASH_FOR_ATTRIBUTE ) )
        {
            encounteredHashFors.put( element.getAttribute( Backend.HASH_FOR_ATTRIBUTE ), element );
        }
        else if ( element.hasAttribute( Backend.HTML_FOR_ATTRIBUTE ) )
        {
            encounteredHtmlFors.put( element.getAttribute( Backend.HTML_FOR_ATTRIBUTE ), element );
        }

        return tagCounter.incrementAndGet();
    }


    private ReferencePredicate.Reference getReferencePredicate( Element element, String tag )
    {
        if ( ! lookupLabels )
        {
            return null;
        }

        if ( tagsAllowedHashForLabels != null && tagsAllowedHashForLabels.containsKey( tag ) )
        {
            ReferencePredicate.Reference rp = tagsAllowedHashForLabels
                    .get( tag )
                    .stream()
                    .map( t ->
                            ReferencePredicate
                                    .NameReference
                                    .HASHFOR
                                    .getReference(
                                            element,
                                            t,
                                            this::getCachedLabel ) )
                    .filter( Objects::nonNull )
                    .findAny()
                    .orElse( null );

            if ( rp != null )
            {
                return rp;
            }
        }

        if ( tagsAllowedHtmlForLabels != null && tagsAllowedHtmlForLabels.containsKey( tag ) )
        {
            ReferencePredicate.Reference rp = tagsAllowedHtmlForLabels
                    .get( tag )
                    .stream()
                    .map( t ->
                            ReferencePredicate
                                    .NameReference
                                    .HTMLFOR
                                    .getReference(
                                            element,
                                            t,
                                            this::getCachedLabel ) )
                    .filter( Objects::nonNull )
                    .findAny()
                    .orElse( null );

            if ( rp != null )
            {
                return rp;
            }
        }


        return null;
    }


    private String buildPredicate( Element element )
    {
        final String tag = element.getTagName();

        // any element might have a primary attribute
        // fcfs
        String[] primaryPredicate = { null };

        primaryAttributes
                .stream()
                .filter( element::hasAttribute )
                .findFirst()
                .ifPresent( ( name ) -> primaryPredicate[ 0 ] = maybeSplitIntoSequence(
                        element.getAttribute( name ),
                        "@" + name,
                        true ) );

        // if already have primary predicate then just for name calculation
        ReferencePredicate.Reference rp = getReferencePredicate( element, tag );

        if ( primaryPredicate[ 0 ] != null )
        {
            return primaryPredicate[ 0 ];
        }
        else if ( rp != null )
        {
            return rp.getXPath();
        }

        if ( tagsAllowedNamesFromSiblingLabels.contains( tag ) )
        {
            String labelPredicate = maybeUpdateNameFromSiblingLabel( element );

            if ( labelPredicate != null )
            {
                return labelPredicate;
            }
        }

        if ( tagsAllowedNamesFromNearestLabels.contains( tag ) )
        {
            String labelPredicate = maybeUpdateNameFromNearestLabel( element );

            if ( labelPredicate != null )
            {
                return labelPredicate;
            }
        }


        // fallback to examining attributes

        final Set< String > anadft = attributesExcludedFromKeysForTags.get( tag );
        final Set< String > defaultAnadft = attributesExcludedFromKeysForTags.get( "*" );


        StringBuilder c = new StringBuilder();

        final NamedNodeMap attrs = element.getAttributes();

        for ( int i = 0, n = attrs.getLength(); i < n; i++ )
        {
            final Attr sattr = ( Attr ) attrs.item( i );

            if ( sattr == null )
            {
                continue;
            }


            if ( Backend.XML_NAMESPACE_URI.equals( sattr.getNamespaceURI() ) )
            {
                continue;
            }

            final String name = sattr.getName();

            if ( name.startsWith( "xmlns:" ) || name.startsWith( Backend.XML_NAMESPACE_TAG + ":" ) )
            {
                continue;
            }

            switch ( name )
            {
                case Backend.HASH_ATTRIBUTE:
                case Backend.HASH_NEW_ATTRIBUTE:
                case Backend.KEY_ATTRIBUTE:
                case Backend.KEY_RAW_ATTRIBUTE:

                case Backend.NAME_ATTRIBUTE:
                case Backend.NAME_CLAIMS_ATTRIBUTE:
                case Backend.NAME_PREVIOUS_ATTRIBUTE:

                case Backend.ACTIONS_ATTRIBUTE:
                case Backend.DUPLICATE_ATTRIBUTE:
                case Backend.RESURRECTED_ATTRIBUTE:
                case Backend.VISITS_ATTRIBUTE:

                    continue;
            }


            if ( anadft != null && anadft.contains( name ) )
            {
                continue;
            }

            if ( defaultAnadft != null && defaultAnadft.contains( name ) )
            {
                continue;
            }


            // the attribute value
            String value = sattr.getNodeValue();

            if ( value == null || NameUtils.isEmpty( value ) )
            {
                continue;
            }


            if ( tagsAttributesToParseAsUrl != null
                 && ! tagsAttributesToParseAsUrl.isEmpty() )
            {
                final Set< String > attrSet = tagsAttributesToParseAsUrl.containsKey( tag )
                        ? tagsAttributesToParseAsUrl.get( tag )
                        : wildcardToParseAsUrl;

                if ( attrSet.contains( name ) )
                {
                    String urlPredicate = getUrlPredicate( name, value );

                    if ( urlPredicate != null && ! urlPredicate.isEmpty() )
                    {
                        if ( c.length() > 0 )
                        {
                            c.append( " and " );
                        }

                        c.append( urlPredicate );

                        continue;
                    }
                    else
                    {
                        // bad url - don't use it
                        continue;
                    }
                }
            }


            if ( attributesToTx.contains( name ) )
            {
                String tx = NameUtils.maybeReverseTranslate( value );

                if ( ! value.equals( tx ) )
                {
                    // translation reference
                    value = format( txRefFormat, tx );
                    value = format( "(@%s='%s')", name, value );

                    if ( tx.lastIndexOf( "'" ) > - 1 )
                    {
                        throw new RuntimeException(
                                format(
                                        "Attribute [%s] value [%s] translation [%s] has unescaped apostrophe in it's value.",
                                        name,
                                        value,
                                        tx )
                        );
                    }
                }
                else if ( txGenerateMissingTranslations )
                {
                    // prefix
                    tx = name + "." + NameUtils.cleanName( value );

                    TextUtils.storeName( tx, value );

                    value = format( txRefFormat, tx );
                    value = format( "(@%s='%s')", name, value );
                }
                else if ( ! txAllowUntranslatedInKeys )
                {
                    // if not translated, and should be,
                    // then can't contribute to key
                    continue;
                }
                else
                {
                    value = maybeSplitIntoSequence( value, "@" + name, true );
                }
            }
            else
            {
                value = maybeSplitIntoSequence( value, "@" + name, true );
            }


            if ( c.length() > 0 )
            {
                c.append( " and " );
            }

            c.append( value );
        }


        return c.toString();
    }


    /**
     * TODO: may recurse for entire length of input
     *
     * @param attrValue
     * @return
     * @throws URISyntaxException
     */
    public URI getURI( String attrValue, int depth ) throws URISyntaxException
    {
        try
        {
            return new URI( attrValue );
        }
        catch ( URISyntaxException e )
        {
            int index = e.getIndex();

            if ( logger.isTraceEnabled() )
            {
                logger.trace( format( "Failed to parse value [%s] at index [%s] as url (at depth %s): %s", attrValue, index, depth, e ) );
            }

            // left size + right size + 1 == att length
            int leftSize = index;
            int rightSize = attrValue.length() - index - 1;


            // if can't do substring
            if ( depth > 0 )
            {
                if ( rightSize > 0 )
                {
                    return getURI( attrValue.substring( index + 1 ), depth - 1 );
                }
                else if ( leftSize > 0 )
                {
                    return getURI( attrValue.substring( 0, index ), depth - 1 );
                }
            }
            throw e;
        }
    }


    public String getUrlPredicate( String attrKey, String attrValue )
    {
        // any other obvious cases?
        if ( attrValue.startsWith( "javascript:" ) )
        {
            return null;
        }

        // we'll exclude anything after a semi colon (e.g. session keys)
        final String REGEX = "\\s*;\\s*";
        final int MAX_DEPTH = 20;

        try
        {
            URI url = getURI( attrValue, MAX_DEPTH );

            return Stream
                    .of( url.getPath(), url.getQuery(), url.getFragment() )
                    .filter( Objects::nonNull )
                    .filter( s -> ! s.isEmpty() )
                    .map( s -> s.split( REGEX )[ 0 ] )
                    .map( s -> format( "contains( @%s, '%s' )", attrKey, s ) )
                    .collect( Collectors.joining( " and " ) );
        }
        catch ( URISyntaxException e )
        {
            logger.warn( format( "Failed to parse attribute [%s] value [%s] as url: %s", attrKey, attrValue, e ) );

            return null;
        }
    }


    private void maybeOverwriteNameAttribute( Element element, String newName )
    {
        String oldName = element.hasAttribute( Backend.NAME_ATTRIBUTE )
                ? element.getAttribute( Backend.NAME_ATTRIBUTE )
                : null;

        if ( ! newName.equals( oldName ) )
        {
            // indicates that name was changed
            //element.setAttribute( NAME_PREVIOUS_ATTRIBUTE, oldName == null ? "" : oldName );

            element.setAttribute( Backend.NAME_ATTRIBUTE, newName );

        }
    }


    private String maybeSplitIntoSequence( String labelFor, String selector, boolean isExact )
    {
        if ( labelFor.lastIndexOf( "'" ) < 0 )
        {
            return format(
                    isExact ? "( %s='%s' )" : "contains( %s, '%s' )",
                    selector,
                    labelFor );
        }

        StringBuilder b = new StringBuilder( "( " );
        boolean isFirst = true;
        for ( String s : labelFor.split( "'" ) )
        {
            if ( isFirst )
            {
                isFirst = false;
                b.append( format( isExact ? "starts-with( %s, '%s' )" : "contains( %s, '%s' )", selector, s ) );
            }
            else
            {
                b.append( " and " );
                b.append( format( "contains( %s, '%s' )", selector, s ) );
            }
        }
        return b
                .append( " )" )
                .toString();
    }


    private Element getCachedLabel( String keyAttr, String keyAttrValue )
    {
        Element label = null;

        switch ( keyAttr )
        {
            case "hash":
                if ( encounteredHashFors.containsKey( keyAttrValue ) )
                {
                    label = encounteredHashFors.remove( keyAttrValue );
                }
                break;
            case "id":
                if ( encounteredHtmlFors.containsKey( keyAttrValue ) )
                {
                    label = encounteredHtmlFors.remove( keyAttrValue );
                }
                break;
        }

        return label;
    }


    public String maybeUpdateNameFromLabel( Element element, String finder, String locator, String target, String keyAttr )
    {
        final String keyAttrValue = element.getAttribute( keyAttr );

        if ( keyAttrValue == null || keyAttrValue.isEmpty() )
        {
            return null;
        }

        // maybe already cached
        Node node = getCachedLabel( keyAttr, keyAttrValue );

        if ( node != null )
        {
            if ( logger.isTraceEnabled() )
            {
                logger.trace( format( "Label Cache [%s] hit [%s].", keyAttr, keyAttrValue ) );
            }
        }
        else if ( lookupLabels )
        {
            node = XPathUtils.getNode( element, format( finder, target, keyAttrValue ) );
        }


        if ( node == null || node.getNodeType() != Node.ELEMENT_NODE )
        {
            return null;
        }

        String labelFor = ( ( Element ) node ).getAttribute( Backend.TEXT_ATTRIBUTE );

        boolean isTextAttribute = true;

        if ( NameUtils.isEmpty( labelFor ) )
        {
            isTextAttribute = false;

            // for HTML: expect there to be a text element with text content
            labelFor = node.getTextContent();
        }


        if ( ! NameUtils.isEmpty( labelFor ) )
        {
            String calculatedName = NameUtils.cleanName( labelFor );

            maybeOverwriteNameAttribute( element, calculatedName );

            String refValue = labelFor.trim();

            boolean exactEquals = ( refValue.equals( labelFor ) );

            String selector = isTextAttribute
                    ? "@" + Backend.TEXT_ATTRIBUTE
                    : "text/text()";

            String refPredicate = TextUtils.isTranslated( refValue )
                    ? format(
                    exactEquals
                            ? "( %s='%s' )"
                            : "contains( %s, '%s' )",
                    selector,
                    format(
                            txRefFormat,
                            maybeReverseTranslate( refValue ) ) )
                    : maybeSplitIntoSequence( refValue, selector, exactEquals );

            return format( locator, keyAttr, target, refPredicate );
        }

        logger.warn( "label with locus [" + finder + "] has empty text!" );

        return null;
    }


    public String maybeUpdateNameFromSiblingLabel( Element element )
    {
        for ( List< String > path : hashForSiblingXPaths )
        {
            String axis = path.get( 0 );
            String locator = path.get( 1 );

            Element label = ( Element ) XPathUtils.getNode( element, axis + "::" + locator );

            if ( label != null )
            {
                if ( reserveForNaming( label ) )
                {
                    String labelFor = label.getAttribute( "text" );

                    if ( ! isEmpty( labelFor ) )
                    {
                        String calculatedName = cleanName( labelFor );

                        maybeOverwriteNameAttribute( element, calculatedName );

                        labelFor = TextUtils.isTranslated( labelFor )
                                ? format(
                                "( @%s='%s' )",
                                Backend.TEXT_ATTRIBUTE,
                                format(
                                        txRefFormat,
                                        maybeReverseTranslate( labelFor ) ) )
                                : maybeSplitIntoSequence( labelFor, "@" + Backend.TEXT_ATTRIBUTE, true );


                        return format( "%s::%s[ %s ]", axis, label.getTagName(), labelFor );
                    }
                }
            }
        }

        return null;
    }


    public String maybeUpdateNameFromNearestLabel( Element element )
    {
        for ( List< String > path : hashForNearestXPaths )
        {
            String axis = path.get( 0 );
            String locator = path.get( 1 );

            Element label = ( Element ) XPathUtils.getNode( element, axis + "::" + locator );

            if ( label != null )
            {
                if ( reserveForNaming( label ) )
                {
                    String labelFor = label.getAttribute( Backend.TEXT_ATTRIBUTE );

                    if ( ! isEmpty( labelFor ) )
                    {
                        String calculatedName = cleanName( labelFor );

                        maybeOverwriteNameAttribute( element, calculatedName );

                        labelFor = TextUtils.isTranslated( labelFor )
                                ? format(
                                "( @%s='%s' )",
                                Backend.TEXT_ATTRIBUTE,
                                format(
                                        txRefFormat,
                                        maybeReverseTranslate( labelFor ) ) )
                                : maybeSplitIntoSequence( labelFor, "@" + Backend.TEXT_ATTRIBUTE, true );

                        return format( "%s::%s[ %s ]", axis, label.getTagName(), labelFor );
                    }
                }
            }
        }
        return null;
    }

    public KeyUtils withPositionPredicateInKeys( boolean c )
    {
        positionPredicateAlways = c;
        return this;
    }


    public KeyUtils withAttributesExcluded( String tagAttributes )
    {
        StringUpcaster.upcastMapSet( tagAttributes, attributesExcludedFromKeysForTags );
        return this;
    }


    public KeyUtils withNearestOrSiblingLabels( String... labels )
    {
        if ( labels != null )
        {
            this.nearestOrSiblingLabels.addAll( Arrays.asList( labels ) );

            hashForLabelsPredicate = nearestOrSiblingLabels
                    .stream()
                    .collect(
                            Collectors
                                    .joining(
                                            "' ) or ( name() = '",
                                            "( name() = '",
                                            "' )" ) );


            for ( String[] h : hashForSiblingXPathComponents )
            {
                String axis = h[ 0 ];
                String locator = h[ 1 ];

                List< String > p = new ArrayList<>();
                p.add( axis );
                p.add( format( locator, hashForLabelsPredicate ) );

                hashForSiblingXPaths.add( p );
            }

            for ( String[] h : hashForNearestXPathComponents )
            {
                String axis = h[ 0 ];
                String locator = h[ 1 ];

                List< String > p = new ArrayList<>();
                p.add( axis );
                p.add( format( locator, hashForLabelsPredicate ) );

                hashForNearestXPaths.add( p );
            }
        }
        return this;
    }


    public KeyUtils withTagsAllowedHashForLabels( String tagAttributes )
    {
        if ( tagAttributes != null && ! tagAttributes.trim().isEmpty() )
        {
            StringUpcaster.upcastMapList( tagAttributes.trim(), tagsAllowedHashForLabels );
        }
        return this;
    }

    public KeyUtils withTagsAttributesToParseAsUrl( String tagAttributes )
    {
        if ( tagAttributes != null && ! tagAttributes.trim().isEmpty() )
        {
            StringUpcaster.upcastMapSet( tagAttributes.trim(), tagsAttributesToParseAsUrl );
        }

        if ( tagsAttributesToParseAsUrl.containsKey( "*" ) )
        {
            wildcardToParseAsUrl.addAll( tagsAttributesToParseAsUrl.get( "*" ) );
        }

        return this;
    }


    public KeyUtils withTagsAllowedHtmlForLabels( String tagAttributes )
    {
        if ( tagAttributes != null && ! tagAttributes.trim().isEmpty() )
        {
            StringUpcaster.upcastMapList( tagAttributes.trim(), tagsAllowedHtmlForLabels );
        }
        return this;
    }

    public KeyUtils withPrimaryAttributes( String... tags )
    {
        if ( tags != null )
        {
            this.primaryAttributes.addAll( Arrays.asList( tags ) );
        }
        return this;
    }


    public KeyUtils withTagsAllowedNamesFromSiblingLabels( String... tags )
    {
        if ( tags != null )
        {
            this.tagsAllowedNamesFromSiblingLabels.addAll( Arrays.asList( tags ) );
        }
        return this;
    }

    public KeyUtils withTagsAllowedNamesFromNearestLabels( String... tags )
    {
        if ( tags != null )
        {
            this.tagsAllowedNamesFromNearestLabels.addAll( Arrays.asList( tags ) );
        }
        return this;
    }

    public KeyUtils withAttributesToTranslate( String... attributesToTx )
    {
        if ( attributesToTx != null )
        {
            this.attributesToTx.addAll( Arrays.asList( attributesToTx ) );
        }
        return this;
    }

    public KeyUtils withTxReferenceFormat( String format )
    {
        txRefFormat = format;
        return this;
    }

    public KeyUtils withTxGenerateMissingTranslations( boolean c )
    {
        txGenerateMissingTranslations = c;
        return this;
    }

    public KeyUtils withTxAllowUntranslated( boolean c )
    {
        txAllowUntranslatedInKeys = c;
        return this;
    }


    public String toString()
    {
        CommentedProperties p = new CommentedProperties();

        export( p );

        return downcastProperties( p );
    }

    public String getCacheDetails()
    {
        StringBuilder b = new StringBuilder( "\n" );

        if ( encounteredTags.isEmpty() )
        {
            b.append( format( "encounteredTags=(empty)\n" ) );
        }
        else
        {
            b.append( format( "encounteredTags=%s\n%s", this.encounteredTags.size(), TextUtils.indentMap( encounteredTags ) ) );
        }


        if ( encounteredHashFors.isEmpty() )
        {
            b.append( format( "unusedHashFors=(empty)\n" ) );
        }
        else
        {
            b.append( format( "unusedHashFors=%s\n%s", this.encounteredHashFors.size(), TextUtils.indentMap( encounteredHashFors ) ) );
        }

        if ( encounteredHtmlFors.isEmpty() )
        {
            b.append( format( "unusedHtmlFors=(empty)\n" ) );
        }
        else
        {
            b.append( format( "unusedHtmlFors=%s\n%s", this.encounteredHtmlFors.size(), TextUtils.indentMap( encounteredHtmlFors ) ) );
        }

        return b.toString();
    }


    public KeyUtils preCache( Node node )
    {
        encounteredHashFors.clear();
        encounteredHtmlFors.clear();

        ( ( NodeVisitor ) n -> {
            if ( n instanceof Document )
            {
                return true;
            }
            else if ( n instanceof Element )
            {
                Element element = ( Element ) n;
                if ( element.hasAttribute( Backend.HASH_FOR_ATTRIBUTE ) )
                {
                    encounteredHashFors.put( element.getAttribute( Backend.HASH_FOR_ATTRIBUTE ), element );
                }
                else if ( element.hasAttribute( Backend.HTML_FOR_ATTRIBUTE ) )
                {
                    encounteredHtmlFors.put( element.getAttribute( Backend.HTML_FOR_ATTRIBUTE ), element );
                }
                return true;
            }
            else
            {
                return false;
            }
        } ).visit( node );

        return this;
    }


    public KeyUtils configure( Properties p )
    {
        Arrays
                .asList( Property.values() )
                .stream()
                .forEach( property -> {
                    property.read( this, p );
                } );

        getNameUtils().configure( p );

        return this;
    }

    public KeyUtils export( Properties p )
    {
        Arrays
                .asList( Property.values() )
                .stream()
                .forEach( property -> {
                    property.write( this, p );
                } );

        getNameUtils().export( p );

        return this;
    }

    public enum Property
    {
        KEY_XPATH_POSITION_PREDICATE_ALWAYS( "positionPredicateAlways" )
                {
                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.positionPredicateAlways = upcast( p.getProperty( attribute ), false );
                    }

                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        p.setProperty( attribute, "" + ku.positionPredicateAlways );
                    }
                },

        KEY_XPATH_POSITION_PREDICATE_IF_NO_OTHER( "positionPredicateIfNoOther" )
                {
                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.positionPredicateIfNoOther = upcast( p.getProperty( attribute ), true );
                    }

                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        p.setProperty( attribute, "" + ku.positionPredicateIfNoOther );
                    }
                },


        KEY_XPATH_PRIMARY_ATTRIBUTES( "primaryAttributes" )
                {
                    final String[] defaultValue = { "guid", "id" };

                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.primaryAttributes.clear();
                        ku.primaryAttributes.addAll( Arrays.asList( upcastSplit( p.getProperty( attribute ), defaultValue ) ) );
                    }

                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        p.setProperty(
                                attribute,
                                downcastCollection( ku.primaryAttributes ) );
                    }
                },

        KEY_XPATH_ATTRIBUTES_TO_TRANSLATE( "tx.attributesToInclude" )
                {
                    final String[] defaultValue = {};

                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.attributesToTx.clear();
                        ku.attributesToTx.addAll( Arrays.asList( upcastSplit( p.getProperty( attribute ), defaultValue ) ) );
                    }

                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        p.setProperty(
                                attribute,
                                downcastCollection( ku.attributesToTx ) );
                    }
                },


        KEY_XPATH_ALLOW_UNTRANSLATED_ATTRIBUTES( "tx.allowUntranslated" )
                {
                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.txAllowUntranslatedInKeys = upcast( p.getProperty( attribute ), true );
                    }

                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        p.setProperty(
                                attribute,
                                "" + ku.txAllowUntranslatedInKeys );
                    }
                },


        KEY_XPATH_GENERATE_MISSING_TRANSLATIONS( "tx.generateMissingEntries" )
                {
                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.txGenerateMissingTranslations = upcast( p.getProperty( attribute ), false );
                    }

                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        p.setProperty(
                                attribute,
                                "" + ku.txGenerateMissingTranslations );
                    }
                },

        KEY_XPATH_TX_REFERENCE_FORMAT( "tx.refFormat" )
                {
                    final String defaultValue = "${ tx[ '%s' ] }";

                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.txRefFormat = upcast( p.getProperty( attribute ), defaultValue );
                    }

                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        if ( ku.txRefFormat != null )
                        {
                            p.setProperty(
                                    attribute,
                                    ku.txRefFormat );
                        }
                    }
                },


        KEY_XPATH_TAG_ATTRIBUTE_URLS( "tagAttributeUrls" )
                {
                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.tagsAttributesToParseAsUrl.clear();
                        ku.withTagsAttributesToParseAsUrl( p.getProperty( attribute ) );
                    }

                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        if ( ku.tagsAttributesToParseAsUrl != null )
                        {
                            p.setProperty(
                                    attribute,
                                    StringUpcaster.downcastMapSet( ku.tagsAttributesToParseAsUrl ) );
                        }
                    }
                },


        KEY_XPATH_EXCLUDE_ATTRIBUTES( "exclude" )
                {
                    String defaultValue = "* = a:actions | disabled | duplicate | \n" +
                                          "icon | focus | enabled | visible | \n" +
                                          "selected | selected-index | size | \n" +
                                          "for-hash | for";

                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.attributesExcludedFromKeysForTags.clear();
                        ku.withAttributesExcluded( p.getProperty( attribute, defaultValue ) );
                    }


                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        if ( ku.attributesExcludedFromKeysForTags != null )
                        {
                            p.setProperty(
                                    attribute,
                                    StringUpcaster.downcastMapSet( ku.attributesExcludedFromKeysForTags ) );
                        }
                    }
                },


        KEY_XPATH_LOOKUP_LABELS( "lookupLabels" )
                {
                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.lookupLabels = upcast( p.getProperty( attribute ), ku.lookupLabels );
                    }

                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        p.setProperty( attribute, "" + ku.lookupLabels );
                    }
                },

        KEY_XPATH_TAGS_ALLOWED_HASH_FOR_LABELS( "tagsAllowedHashForLabels" )
                {
                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.tagsAllowedHashForLabels.clear();
                        ku.withTagsAllowedHashForLabels( p.getProperty( attribute ) );
                    }


                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        if ( ku.tagsAllowedHashForLabels != null )
                        {
                            p.setProperty(
                                    attribute,
                                    downcastMapList( ku.tagsAllowedHashForLabels ) );
                        }
                    }
                },


        KEY_XPATH_TAGS_ALLOWED_HTML_FOR_LABELS( "tagsAllowedHtmlForLabels" )
                {
                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.tagsAllowedHtmlForLabels.clear();
                        ku.withTagsAllowedHtmlForLabels( p.getProperty( attribute ) );
                    }

                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        if ( ku.tagsAllowedHtmlForLabels != null )
                        {
                            p.setProperty(
                                    attribute,
                                    downcastMapList( ku.tagsAllowedHtmlForLabels ) );
                        }
                    }
                },


        KEY_XPATH_NEAREST_OR_SIBLING_LABELS( "nearestOrSiblingLabels" )
                {
                    String[] defaultValue = {};

                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.nearestOrSiblingLabels.clear();
                        ku.withNearestOrSiblingLabels( upcastSplit( p.getProperty( attribute ), defaultValue ) );
                    }


                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        if ( ku.nearestOrSiblingLabels != null )
                        {
                            p.setProperty(
                                    attribute,
                                    downcastCollection( ku.nearestOrSiblingLabels ) );
                        }
                    }
                },


        KEY_XPATH_TAGS_ALLOWED_NAMES_FROM_SIBLING_LABELS( "tagsAllowedNamesFromSiblingLabels" )
                {
                    String[] defaultValue = {};

                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.tagsAllowedNamesFromSiblingLabels.clear();
                        ku.withTagsAllowedNamesFromSiblingLabels( upcastSplit( p.getProperty( attribute ), defaultValue ) );
                    }


                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        if ( ku.tagsAllowedNamesFromSiblingLabels != null )
                        {
                            p.setProperty(
                                    attribute,
                                    downcastCollection( ku.tagsAllowedNamesFromSiblingLabels ) );
                        }
                    }
                },


        KEY_XPATH_TAGS_ALLOWED_NAMES_FROM_NEAREST_LABELS( "tagsAllowedNamesFromNearestLabels" )
                {
                    String[] defaultValue = {};

                    @Override
                    void read( KeyUtils ku, Properties p )
                    {
                        ku.tagsAllowedNamesFromNearestLabels.clear();
                        ku.withTagsAllowedNamesFromNearestLabels( upcastSplit( p.getProperty( attribute ), defaultValue ) );
                    }

                    @Override
                    void write( KeyUtils ku, Properties p )
                    {
                        if ( ku.tagsAllowedNamesFromNearestLabels != null )
                        {
                            p.setProperty(
                                    attribute,
                                    downcastCollection( ku.tagsAllowedNamesFromNearestLabels ) );
                        }
                    }
                },;


        final static String ATTRIBUTE_PREFIX = "modeller.xpath";
        final String attribute;

        Property( String attribute )
        {
            this.attribute = ATTRIBUTE_PREFIX + "." + attribute;
        }


        abstract void read( KeyUtils ku, Properties p );

        abstract void write( KeyUtils ku, Properties p );
    }


    public static void main( String[] args )
    {
        System.out.println(
                Arrays
                        .asList( Property.values() )
                        .stream()
                        .map( p -> format( "%s=%s", p.attribute, p.name() ) )
                        .collect( Collectors.joining( "\n" ) )
        );
    }

}
