package com.brentcroft.gtd.utilities;

import com.brentcroft.util.XmlUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import static com.brentcroft.gtd.utilities.NameUtils.isEmpty;
import static java.lang.String.format;


public class XmlAccumulator
{
    private final static transient Logger logger = Logger.getLogger( XmlAccumulator.class );

    private Long serial = null;

    private boolean countHits = true;

    private Set< String > updateableAttributes = new HashSet<>();

    public XmlAccumulator()
    {
        updateableAttributes.add( "hash" );
        updateableAttributes.add( "seq" );
        updateableAttributes.add( "duration" );
        updateableAttributes.add( "timestamp" );
    }


    public XmlAccumulator withSerial( long serial )
    {
        this.serial = serial;
        return this;
    }

    public XmlAccumulator withCountHits( boolean countHits )
    {
        this.countHits = countHits;
        return this;
    }

    public boolean merge( Document base, Element... additions )
    {
        if ( additions == null || additions.length < 1 )
        {
            throw new RuntimeException( "additions cannot be null or empty!" );
        }


        if ( base.getDocumentElement() == null )
        {
            // shallow copy the first addition
            base.appendChild( base.importNode( additions[ 0 ], false ) );
        }

        return merge( base.getDocumentElement(), additions );
    }


    /**
     * Merges one or more addition elements to the base element.
     * <p/>
     * Returns <code>true</code> if the base element is modified otherwise <code>false</code>.
     * <p/>
     * Any unid attributes are updated from the additions, to the base element and its descendants.
     *
     * @param base      (B) The element to which the additions are merged
     * @param additions (A) The elements which are merged or added to the base
     * @return <code>true</code> if the base is modified, otherwise <code>false</code>.
     */
    public boolean merge( Element base, Element... additions )
    {
        if ( additions == null || additions.length < 1 )
        {
            throw new RuntimeException( "additions cannot be null or empty!" );
        }
        else if ( base == null )
        {
            throw new RuntimeException( "base cannot be null!" );
        }

        boolean notChanged = true;


        for ( Element addition : additions )
        {
            final NodeList bcl = base.getChildNodes();


            for ( Node addNode = addition.getFirstChild(); addNode != null; addNode = addNode.getNextSibling() )
            {
                if ( ! ( addNode instanceof Element ) )
                {
                    continue;
                }

                final Element addElement = ( Element ) addNode;

                String key = addElement.getAttribute( "a:xpath" );
                String name = addElement.getAttribute( "a:name" );

                if ( isEmpty( key ) || isEmpty( name ) )
                {
                    throw new RuntimeException( format( "Addition element must have values for both attributes @x:name and @x:path%n%s", XmlUtils.serialize( addElement ) ) );
                }


                Element mbe = getElementWithKey( bcl, key, name );

                if ( mbe == null )
                {
                    mbe = copyElementToParent( base, addElement );

                    // because not merging
                    // want to set acc data on copied descendants
                    if ( countHits && serial != null )
                    {
                        setDescendantAccData( mbe, serial );
                    }
                }
                else
                {
                    notChanged = merge( mbe, addElement );

                    maybeUpdateAttributes( mbe, addElement );
                }


                AccData accData = getAccData( mbe, true );

                if ( countHits && serial != null )
                {
                    accData.hit( serial );
                }
            }
        }

        return notChanged;
    }


    private long groupCounter = 0;
    private Map< Set< Long >, String > groups = new HashMap<>();


    public static String longToLetters( long value )
    {
        StringBuffer result = new StringBuffer();

        do
        {
            result.append( ( char ) ( 'A' + value % 26 ) );
            value /= 26;
        }
        while ( -- value >= 0 );

        return result.toString();
    }

    public void expandGroups( Element documentElement )
    {
        // identify groups
        expandAccData(
                documentElement,
                ( e, a ) -> {
                    Set< Long > hits = a.hits();

                    if ( groups.containsKey( hits ) )
                    {
                        a.group( groups.get( hits ) );
                    }
                    else
                    {
                        String key = longToLetters( groupCounter++ );

                        groups.put( hits, key );

                        a.group( key );
                    }
                }
        );

        expandAccData(
                documentElement,
                ( e, a ) -> {
                    Set< Long > hits = a.hits();
                    int size = hits.size();

                    // find and add groups I belong to
                    groups
                            .entrySet()
                            .stream()
                            // assume already have group with same size
                            .filter( entry -> entry.getKey().size() > size )
                            //
                            .filter( entry -> entry.getKey().containsAll( hits ) )
                            .forEach( entry -> a.group( entry.getValue() ) );
                }
        );
    }


    public enum ElementAccDataBiC implements BiConsumer< Element, AccData >
    {
        ALL( ( e, a ) -> {

            // only if no groups then write hits
            if ( a.groups() == null || a.groups().isEmpty() )
            {
                Optional
                        .ofNullable( a.hits() )
                        .filter( hits -> ! hits.isEmpty() )
                        .ifPresent( hits -> {
                            e.setAttribute(
                                    "a:hits",
                                    hits
                                            .stream()
                                            .sorted()
                                            .map( Object::toString )
                                            .collect( Collectors.joining( "," ) ) );
                        } );
            }

            Optional
                    .ofNullable( a.groups() )
                    .filter( groups -> ! groups.isEmpty() )
                    .ifPresent( groups -> {
                        e.setAttribute(
                                "a:groups",
                                groups
                                        .stream()
                                        .sorted()
                                        .collect( Collectors.joining( "," ) ) );
                    } );

        } );

        BiConsumer< Element, AccData > bic;

        ElementAccDataBiC( BiConsumer< Element, AccData > bic )
        {
            this.bic = bic;
        }

        @Override
        public void accept( Element element, AccData accData )
        {
            bic.accept( element, accData );
        }
    }


    /**
     * Finds AccData objects attached to the supplied element (and it's descendants)
     * and expands the AccData to attribute values.
     *
     * @param element
     */
    public void expandAccData( Node element, BiConsumer< Element, AccData > elementAccDataBiConsumer )
    {
        if ( ! countHits )
        {
            return;
        }

        Optional
                .ofNullable( getAccData( element ) )
                .ifPresent( accData -> elementAccDataBiConsumer.accept( ( Element ) element, accData ) );

        for ( Node child = element.getFirstChild(); child != null; child = child.getNextSibling() )
        {
            if ( Node.ELEMENT_NODE == child.getNodeType() )
            {
                expandAccData( child, elementAccDataBiConsumer );
            }
        }
    }


    /**
     * On what basis should attributes be updated???
     *
     * @param mbe
     * @param addElement
     */
    private void maybeUpdateAttributes( Element mbe, Element addElement )
    {
        NamedNodeMap attributes = addElement.getAttributes();
        for ( int j = 0, m = attributes.getLength(); j < m; j++ )
        {
            Attr attr = ( Attr ) attributes.item( j );

            String attName = attr.getName();
            String attValue = attr.getValue();

            if ( ! mbe.hasAttribute( attName ) )
            {
                // adding new attribute
                mbe.setAttribute( attName, attValue );
            }
            else
            {
                String value = mbe.getAttribute( attName );

                if ( value == null )
                {
                    // replace old null value (maybe with another null)
                    mbe.setAttribute( attName, attValue );
                }
                else if ( ! value.equals( attValue ) )
                {
                    // replace old value with a new value
                    if ( isUpdatableAttribute( attName ) )
                    {
                        mbe.setAttribute( attName, attValue );
                    }

                    // warn of different values
                    logger.warn( format( "On match: attribute [%s] has different value: existing=[%s], new=[%s]",
                            attName,
                            value,
                            attValue ) );
                }
            }
        }
    }

    private boolean isUpdatableAttribute( String attName )
    {
        return updateableAttributes.contains( attName );
    }


    private Element getElementWithKey( NodeList list, String targetKey, String targetName )
    {
        if ( targetKey == null )
        {
            return null;
        }

        for ( int j = 0, m = list.getLength(); j < m; j++ )
        {
            final Node node = list.item( j );

            if ( ! ( node instanceof Element ) )
            {
                continue;
            }

            Element element = ( Element ) node;

            String key = element.getAttribute( "a:xpath" );
            String name = element.getAttribute( "a:name" );

            if ( targetKey.equals( key ) && targetName.equals( name ) )
            {
                // exact match on key and name
                return element;
            }
        }

        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "Name [%s] and Key [%s] not found.", targetName, targetKey ) );
        }

        return null;
    }


    private Element copyElementToParent( Element target, Element element )
    {
        Element newChild = ( Element ) target
                .getOwnerDocument()
                .importNode( element, true );
        target.appendChild( newChild );

        // the child will
        return newChild;
    }

    /**
     * Set the serial as a hit on every descendant element.
     *
     * @param target the element who's descendants are to be hit
     * @param serial the value with which to hit the descendants
     */
    private void setDescendantAccData( Node target, long serial )
    {
        for ( Node n = target.getFirstChild(); n != null; n = n.getNextSibling() )
        {
            if ( n.getNodeType() == Node.ELEMENT_NODE )
            {
                Element child = ( Element ) n;
                Optional
                        .ofNullable( getAccData( child, true ) )
                        .ifPresent( a -> {
                            a.hit( serial );
                            setDescendantAccData( child, serial );
                        } );
            }
        }
    }


    public static final String accKey = "a:acc";


    private AccData getAccData( Node element )
    {
        return getAccData( element, false );
    }

    private AccData getAccData( Node element, boolean createNew )
    {
        AccData userData = ( AccData ) element.getUserData( accKey );

        if ( userData == null && createNew )
        {
            userData = new AccData();
            element.setUserData( accKey, userData, null );
        }

        return userData;
    }

    public class AccData
    {
        private Set< Long > hits = new HashSet<>();
        private Set< String > groups = new HashSet<>();
        private String group;

        public void hit( long serial )
        {
            hits.add( serial );
        }

        public Set< Long > hits()
        {
            return hits;
        }

        public void group( String key )
        {
            if ( groups.isEmpty() )
            {
                group = key;
            }

            groups.add( key );
        }

        public String group()
        {
            return group;
        }

        public Set< String > groups()
        {
            return groups;
        }
    }

}