package com.brentcroft.gtd.utilities;

import com.brentcroft.gtd.driver.Backend;
import com.brentcroft.util.TextUtils;
import com.brentcroft.util.TriFunction;
import com.brentcroft.util.XPathUtils;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static com.brentcroft.gtd.utilities.NameUtils.maybeReverseTranslate;
import static java.lang.String.format;

public class ReferencePredicate
{
    private final static Logger logger = Logger.getLogger( ReferencePredicate.class );

    private static String txRefFormat = "${ tx[ '%s' ] }";

    interface ReferenceCache
    {
        Element getCachedLabel( String key, String value );
    }


    public static Optional< Reference > getFirst( Element element, String tag, ReferenceCache cache )
    {
        return Stream.of( NameReference.values() )
                .map( nr -> nr.getReference( element, tag, cache ) )
                .filter( Objects::nonNull )
                .findFirst();
    }


    interface Reference
    {
        String getName();

        String getXPath();

        default int getConfidence()
        {
            return 0;
        }
    }


    enum NameReference
    {
        HASHFOR( ( e, t, c ) -> {
            return findReference(
                    e,
                    // label finder (following since assume cached encountered labels)
                    "following::*[ %s ][ @for-hash='%s' ]",
                    "( @%s=//%s[ %s ]/@for-hash )",
                    t,
                    "hash",
                    c );
        } ),
        HTMLFOR( ( element, target, cache ) -> findReference(
                element,
                // label finder (following since assume cached encountered labels)
                "following::%s[ @for='%s' ]",
                "( @%s=//%s[ %s ]/@for )",
                target,
                "id",
                cache ) ),
        SIBLING( ( element, target, cache ) -> null),
        NEAREST( ( e, t, c ) -> null );



        TriFunction< Element, String, ReferenceCache, Reference > bif;

        NameReference( TriFunction< Element, String, ReferenceCache, Reference > bif )
        {
            this.bif = bif;
        }

        public Reference getReference( Element element, String tag, ReferenceCache cache )
        {
            return bif.apply( element, tag, cache );
        }
    }


    private static Reference findReference( Element element, String finder, String locator, String target, String valueAttr, ReferenceCache cache )
    {
        final String value = element.getAttribute( valueAttr );

        if ( value == null || value.isEmpty() )
        {
            return null;
        }

        // maybe already cached
        Node node = Optional
                .ofNullable( cache )
                .orElse( null )
                .getCachedLabel( valueAttr, value );

        if ( node != null )
        {
            if ( logger.isTraceEnabled() )
            {
                logger.trace( format( "Label Cache [%s] hit [%s].", valueAttr, value ) );
            }
        }
        else
        {
            node = XPathUtils.getNode( element, format( finder, target, value ) );
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

            return new Reference()
            {
                @Override
                public String getName()
                {
                    return null;
                }

                @Override
                public String getXPath()
                {
                    return format( locator, valueAttr, target, refPredicate );
                }
            };
        }

        logger.warn( "label with locus [" + finder + "] has empty text!" );

        return null;
    }


    static void maybeOverwriteNameAttribute( Element element, String newName )
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


    static String maybeSplitIntoSequence( String labelFor, String selector, boolean isExact )
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

}
