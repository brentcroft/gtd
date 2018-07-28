package com.brentcroft.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathConstants;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import static java.lang.String.format;

/**
 * Created by Alaric on 11/07/2017.
 */
public class XmlUtils
{
    private final static transient Logger logger = Logger.getLogger( XmlUtils.class );

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();

    private final static Pattern dtdPattern = Pattern.compile( "<!DOCTYPE [^>]*>\\s*" );

    private final static Pattern xmlDeclPattern = Pattern.compile( "<\\?xml [^>]*\\?>\\s*" );

    private static final Map< Integer, String > entities = new LinkedHashMap< Integer, String >( 4 );
    private static final Map< Integer, String > zentities = new LinkedHashMap< Integer, String >( 4 );

    static
    {
        zentities.put( ( int ) '\'', "apos" );
        zentities.put( ( int ) '"', "quot" );
        zentities.put( ( int ) '<', "lt" );
        zentities.put( ( int ) '>', "gt" );


        entities.putAll( zentities );
        entities.put( ( int ) '&', "amp" );


        DOCUMENT_BUILDER_FACTORY.setNamespaceAware( true );
        DOCUMENT_BUILDER_FACTORY.setExpandEntityReferences( false );
    }


    public static String removeAnyDtd( String xmlText )
    {
        return dtdPattern.matcher( xmlText ).replaceFirst( "" );
    }

    public static String removeAnyXmlDecl( String xmlText )
    {
        return xmlDeclPattern.matcher( xmlText ).replaceFirst( "" );
    }


    public static String escapeForXmlAttribute( String text )
    {
        if ( text == null )
        {
            return null;
        }

        StringBuilder b = new StringBuilder();
        escapeForXmlAttribute( b, text, entities );
        return b.toString();
    }

    public static String zescapeForXmlAttribute( String text )
    {
        if ( text == null )
        {
            return null;
        }

        StringBuilder b = new StringBuilder();
        escapeForXmlAttribute( b, text, entities );
        return b.toString();
    }

    private static void escapeForXmlAttribute( StringBuilder b, String str, Map< Integer, String > entities )
    {
        int len = str.length();
        for ( int i = 0; i < len; i++ )
        {
            char c = str.charAt( i );

            String entityName = entities.get( ( int ) c );

            if ( entityName != null )
            {
                b.append( '&' );
                b.append( entityName );
                b.append( ';' );
            }
            else if ( c > 0x7F || c < 0x20 )
            {
                String c10 = Integer.toString( c, 10 );

                b.append( "&#" );

                for ( int j = Math.max( 4 - c10.length(), 0 ), m = 0; j > m; j-- )
                {
                    b.append( "0" );
                }

                b.append( c10 );
                b.append( ';' );
            }
            else
            {
                b.append( c );
            }
        }
    }


    public static Document getDocument( Node node )
    {
        return node instanceof Document
                ? ( Document ) node
                : node.getOwnerDocument();
    }

    public static boolean hasChildElements( Node node )
    {
        for ( Node child = node.getFirstChild(); child != null; child = child.getNextSibling() )
        {
            if ( Node.ELEMENT_NODE == child.getNodeType() )
            {
                return true;
            }
        }

        return false;
    }

    /**
     * This provides a suitable String for an XML element tag.<br/>
     * <p>
     * It takes the class name and replaces any dollar signs with underscores.<br/>
     *
     * @param guiClass the class to calculate an identified for
     * @return the class name and replaces any dollar signs with underscores
     */
    public static String getClassIdentifier( Class< ? > guiClass )
    {
        if ( guiClass == null )
        {
            return "(null)";
        }
        final String candidateTag = guiClass.getName().replaceAll( "\\$", "-" );
        return candidateTag.substring( candidateTag.lastIndexOf( '.' ) + 1 );
    }


    public static Document newDocument()
    {
        try
        {
            return DOCUMENT_BUILDER_FACTORY
                    .newDocumentBuilder()
                    .newDocument();
        }
        catch ( ParserConfigurationException e )
        {
            throw new RuntimeException( e );
        }
    }

    public static Document newDocument( Reader reader )
    {
        try ( BufferedReader br = reader instanceof BufferedReader
                ? ( BufferedReader ) reader
                : new BufferedReader( reader ) )
        {
            final Document document = DOCUMENT_BUILDER_FACTORY
                    .newDocumentBuilder()
                    .parse( new InputSource( br ) );

            document.normalize();

            return document;
        }
        catch ( Exception e )
        {
            throw e instanceof RuntimeException ? ( RuntimeException ) e : new RuntimeException( e );
        }
    }

    public static Document newDocument( File file )
    {
        try
        {
            Document document = newDocument( new FileReader( file ) );

            document.setDocumentURI( file.toURI().toString() );

            return document;
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Error parsing file [" + e + "]: " + file, e );
        }
    }


    public static Document copyNodeToNewDocument( Node node )
    {
        try
        {
            final TransformerFactory tfactory = TransformerFactory.newInstance();
            final Transformer tx = tfactory.newTransformer();

            final DOMSource source = new DOMSource( node );
            final DOMResult result = new DOMResult();

            tx.transform( source, result );

            return ( Document ) result.getNode();
        }
        catch ( Exception e )
        {
            throw ( e instanceof RuntimeException ) ? ( RuntimeException ) e : new RuntimeException( e );
        }
    }


    public static Document parse( String text )
    {
        try
        {
            return DOCUMENT_BUILDER_FACTORY
                    .newDocumentBuilder()
                    .parse( new InputSource( new StringReader( text ) ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( format( "Failed to parse text to document [%s].", text ), e );
        }
    }

    public static Document parse( InputSource is ) throws ParserConfigurationException, IOException, SAXException
    {
        return DOCUMENT_BUILDER_FACTORY
                .newDocumentBuilder()
                .parse( is );
    }


    public static String serialize( Node doc )
    {
        return serialize( doc, true, true );
    }

    public static String serialize( Node doc, boolean indent, boolean xmlDecl )
    {
        try
        {
            DOMSource domSource = new DOMSource( doc );
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult( writer );

            Transformer transformer = TRANSFORMER_FACTORY.newTransformer();

            transformer.setOutputProperty( OutputKeys.METHOD, "xml" );

            if ( indent )
            {
                transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
                transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );
            }

            if ( ! xmlDecl )
            {
                transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
            }

            transformer.transform( domSource, result );

            writer.flush();

            return writer.toString();
        }
        catch ( Exception e )
        {
            logger.warn( "Failed serialization by transform, doing simple serialization: " + e );

            StringWriter writer = new StringWriter();

            simpleSerializeNode( doc, writer );

            writer.flush();

            return writer.toString();
        }
    }


    public static void simpleSerializeNode( Node node, StringWriter writer )
    {
        switch ( node.getNodeType() )
        {
            case Node.DOCUMENT_NODE:
                if ( node.hasChildNodes() )
                {
                    Node childNode = node.getFirstChild();

                    while ( childNode != null )
                    {
                        simpleSerializeNode( childNode, writer );

                        childNode = childNode.getNextSibling();
                    }
                }
                break;

            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                writer.append( Entitizer.escape( node.getNodeValue() ) );
                break;


            case Node.ELEMENT_NODE:
                writer.append( "\n<" ).append( node.getNodeName() );

                final NamedNodeMap attributes = node.getAttributes();

                for ( int i = 0, n = attributes.getLength(); i < n; i++ )
                {
                    final Node attribute = attributes.item( i );

                    String prefix = attribute.getPrefix();

                    writer
                            .append( " " )
                            .append( ( prefix == null ? "" : prefix + ":" ) + attribute.getLocalName() )
                            .append( "=\"" )
                            .append( Entitizer.escape( attribute.getNodeValue() ) )
                            .append( "\"" );
                }


                if ( node.hasChildNodes() )
                {
                    writer.append( ">" );

                    Node childNode = node.getFirstChild();

                    while ( childNode != null )
                    {
                        simpleSerializeNode( childNode, writer );

                        childNode = childNode.getNextSibling();
                    }

                    writer.append( "</" ).append( node.getNodeName() ).append( ">" );
                }
                else
                {
                    writer.append( "/>" );
                }
                break;
        }
    }

    /**
     * Traverse node tree checking for any text (or CDATA) nodes that when trimmed have zero length and then removing
     * them.
     * <p/>
     * <p>
     * NB: It makes sense to normalize the node before calling this method.
     *
     * @param node the node to have its empty text nodes removed
     * @return the same node as was passed in
     */
    public static Node removeTrimmedEmptyTextNodes( Node node )
    {
        if ( ! node.hasChildNodes() )
        {
            return node;
        }


        Node childNode = node.getFirstChild();

        while ( childNode != null )
        {
            Node currentNode = childNode;

            // capture now before any changes
            childNode = childNode.getNextSibling();

            switch ( currentNode.getNodeType() )
            {
                case Node.TEXT_NODE:
                case Node.CDATA_SECTION_NODE:
                    if ( currentNode.getTextContent().trim().length() == 0 )
                    {
                        // lets get rid of it
                        node.removeChild( currentNode );
                    }
                    break;

                case Node.DOCUMENT_NODE:
                case Node.DOCUMENT_FRAGMENT_NODE:
                case Node.ELEMENT_NODE:
                    removeTrimmedEmptyTextNodes( currentNode );
                    break;
            }
        }

        return node;

    }


    /**
     * Used to generate unique values for a given attribute for sibling elements.<p/>
     * <p>
     * Checks if the candidateValue for the attribute is already in use by a previous sibling element and if so,
     * calculates the next disambiguating suffix and returns the candidateValue with the suffix appended,
     * otherwise just returns the candidateValue.<p/>
     *
     * @param element        the element for who's attribute the candidateValue is proposed
     * @param namespace
     * @param attr           the attribute for whom the candidateValue is proposed
     * @param candidateValue the candidateValue proposed for the attribute
     * @param suffixFormat   the format of the suffix
     * @return
     */
    public static String disambiguateAttr( Element element, String namespace, String attr, String candidateValue, String suffixFormat )
    {
        // check if any sibling is using this name;
        Node ps = element.getPreviousSibling();

        while ( ps != null )
        {
            if ( ps.getNodeType() == Node.ELEMENT_NODE )
            {
                Element pse = ( Element ) ps;


                String psValue = pse.getAttributeNS( namespace, attr );

                if ( psValue.startsWith( candidateValue ) )
                {
                    String suffix = psValue.substring( candidateValue.length() ).trim();

                    if ( suffix.isEmpty() || Character.isDigit( suffix.charAt( 0 ) ) )
                    {
                        try
                        {
                            int count = suffix.isEmpty()
                                    ? 0
                                    : Integer.parseInt(
                                            suffix.startsWith( "[" ) || suffix.startsWith( "(" )
                                                    ? suffix.substring( 1, suffix.length() - 1 ).trim()
                                                    : suffix.trim() );

                            return candidateValue + format( suffixFormat, count + 1 );
                        }
                        catch ( NumberFormatException ignored )
                        {
                        }
                    }
                }
            }

            ps = ps.getPreviousSibling();
        }

        return candidateValue;
    }

    public static void maybeAppendElementAttribute( Element element, String attribute, String value )
    {
        maybeAppendElementAttribute( null, element, null, attribute, value );
    }

    public static void maybeAppendElementAttribute( Element element, String namespace, String attribute, String value )
    {
        maybeAppendElementAttribute( null, element, namespace, attribute, value );
    }

    public static void maybeAppendElementAttribute( Map< String, Object > options, Element element, String namespace, String attribute,
                                                    String value )
    {
        //
        if ( options == null || ! options.containsKey( attribute ) )
        {
            if ( value != null && ! value.isEmpty() )
            {
                String escapedValue = value.toString();//entityEscapement.escapeForXmlAttribute( value.toString() );

                String existingValue = null;

                if ( element.hasAttributeNS( namespace, attribute ) )
                {
                    existingValue = element.getAttributeNS( namespace, attribute );
                }

                if ( existingValue == null || ! existingValue.contains( escapedValue ) )
                {
                    String newValue = ( existingValue != null
                            ? ( existingValue + ", " )
                            : "" ) + escapedValue;

                    //element.setAttributeNS( namespace, attribute, newValue );
                    Attr attr = getDocument( element ).createAttributeNS( namespace, attribute );

                    attr.setValue( newValue );

                    element.setAttributeNodeNS( attr );
                }
            }
        }
    }

    public static void maybeSetElementAttribute( Element element, String attribute, Object value )
    {
        maybeSetElementAttribute( null, element, null, attribute, value );
    }

    public static void maybeSetElementAttribute( Element element, String namespace, String attribute, Object value )
    {
        maybeSetElementAttribute( null, element, namespace, attribute, value );
    }

    public static void maybeSetElementAttribute( Map< String, Object > options, Element element, String namespace, String attribute,
                                                 Object value )
    {
        // options contains attribute name forbids
        // empty value forbids
        if ( ( options != null && options.containsKey( attribute ) )
             || ( value == null || value.toString().isEmpty() ) )
        {
            return;
        }

        final String escapedValue = value.toString();

        // same value forbids
        if ( element.hasAttributeNS( namespace, attribute ) && escapedValue.equals( element.getAttributeNS( namespace, attribute ) ) )
        {
            return;
        }

        element.setAttributeNS( namespace, attribute, value.toString() );
    }


    public static Templates newTemplates( File file ) throws Exception
    {
        return TRANSFORMER_FACTORY.newTemplates( new StreamSource( new FileReader( file ), file.toURI().toURL().toExternalForm() ) );
    }

    public static Templates newTemplates( Reader reader ) throws Exception
    {
        return TRANSFORMER_FACTORY.newTemplates( new StreamSource( reader ) );
    }

    public static Templates newTemplates( Reader reader, String uri ) throws Exception
    {
        return TRANSFORMER_FACTORY.newTemplates( new StreamSource( reader, uri ) );
    }


    public static Node transform( Templates templates, Node node, Map< String, Object > parameters )
    {
        return transform( templates, null, node, parameters );
    }

    public static Node transform( Templates templates, URIResolver uriResolver, Node node, Map< String, Object > parameters )
    {
        try
        {
            final Transformer transformer = templates.newTransformer();

            Optional
                    .ofNullable( parameters )
                    .ifPresent( p -> p.forEach( transformer::setParameter ) );

            Optional
                    .ofNullable( uriResolver )
                    .ifPresent( transformer::setURIResolver );

            final DOMSource domSource = new DOMSource();
            domSource.setNode( node );

            final DOMResult domResult = new DOMResult();
            domResult.setNode( DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().newDocument() );

            // transform the input xml using dom source and dom result
            transformer.transform( domSource, domResult );

            return domResult.getNode();
        }
        catch ( Exception e )
        {
            throw e instanceof RuntimeException
                    ? ( RuntimeException ) e
                    : new RuntimeException( e );
        }
    }


    public static String transformToText( Templates templates, Node node, Map< String, Object > parameters )
    {
        try
        {
            final Transformer transformer = templates.newTransformer();

            Optional
                    .ofNullable( parameters )
                    .ifPresent( p -> p.forEach( transformer::setParameter ) );

            final DOMSource domSource = new DOMSource();
            domSource.setNode( node );

            StringWriter sw = new StringWriter();

            // transform the input xml using dom source and stream result
            transformer.transform( domSource, new StreamResult( sw ) );

            return sw.toString();
        }
        catch ( Exception e )
        {
            throw e instanceof RuntimeException
                    ? ( RuntimeException ) e
                    : new RuntimeException( e );
        }
    }


    static class Entitizer
    {
        private static final Map< Integer, String > entities = new LinkedHashMap< Integer, String >( 4 );

        static
        {
//            entities.put( ( int ) '\'', "apos" );
            entities.put( ( int ) '"', "quot" );
            entities.put( ( int ) '<', "lt" );
            entities.put( ( int ) '>', "gt" );
            entities.put( ( int ) '&', "amp" );
        }

        public static String escape( String str )
        {
            StringBuilder b = new StringBuilder();

            str
                    .chars()
                    .forEach( c -> appendEscapedChar( b, ( char ) c ) );

            return b.toString();
        }


        private static void appendEscapedChar( StringBuilder b, char c )
        {
            String entityName = entities.get( ( int ) c );

            if ( entityName != null )
            {
                b.append( '&' );
                b.append( entityName );
                b.append( ';' );
            }
            else if ( c > 0x7F || c < 0x20 )
            {
                String c10 = Integer.toString( c, 10 );

                b.append( "&#" );

                for ( int j = Math.max( 4 - c10.length(), 0 ), m = 0; j > m; j-- )
                {
                    b.append( "0" );
                }

                b.append( c10 );
                b.append( ';' );
            }
            else
            {
                b.append( c );
            }
        }
    }


    public static Document filterByPath( Document document, String path )
    {
        try
        {
            Node locus = ( Node ) XPathUtils
                    .getCompiledPath( path )
                    .evaluate(
                            document,
                            XPathConstants.NODE );


            Document d = newDocument();


            if ( locus == null )
            {
                // copy the document element only
                d.appendChild( d.createComment( "empty filter result" ) );

                return d;
            }

            LinkedList< Node > parents = new LinkedList<>();

            Node locusParent = locus.getParentNode();

            while ( locusParent != null )
            {
                parents.add( locusParent );
                locusParent = locusParent.getParentNode();
            }

            Node de = null;
            Node l = null;

            // but not including document node
            for ( int i = parents.size() - 1; i >= 0; i-- )
            {
                if ( parents.get( i ).getNodeType() == Node.DOCUMENT_NODE )
                {
                    continue;
                }

                l = d.importNode( parents.get( i ), false );

                de = ( de == null )
                        ? d.appendChild( l )
                        : de.appendChild( l );
            }

            // deep copy
            l = d.importNode( locus, true );

            de = ( de == null )
                    ? d.appendChild( l )
                    : de.appendChild( l );


            return d;
        }
        catch ( Exception e )
        {
            throw e instanceof RuntimeException
                    ? ( RuntimeException ) e
                    : new RuntimeException( e );
        }
    }

    public interface ElementFilter
    {
        boolean accept( Element element );
    }


    public static boolean erode( Element element, ElementFilter elementFilter )
    {
        boolean retainChild = false;

        if ( element.hasChildNodes() )
        {
            for ( Node child = element.getLastChild(); child != null; )
            {
                Node thisChild = child;
                child = child.getPreviousSibling();

                if ( thisChild.getNodeType() == Node.ELEMENT_NODE )
                {
                    boolean remaining = erode( ( Element ) thisChild, elementFilter );

                    if ( ! remaining )
                    {
                        element.removeChild( thisChild );
                    }
                    else if ( ! retainChild )
                    {
                        retainChild = true;
                    }
                }
            }
        }

        return retainChild || elementFilter.accept( element );
    }


    /**
     * Adds the declaration (attribute) <code>xmlns:{prefix}="{uri}"</code> to the given node's document's document element,
     * if it doesn't exists there already.
     *
     * @param node   the node who's document's document element is to have the declaration (attribute)
     * @param prefix the prefix to be used
     * @param uri    the namespace uri
     * @return the given node
     */
    public static Node addXmlnsPrefixNamespaceDeclaration( Node node, String prefix, String uri )
    {
        Element documentElement = getDocument( node ).getDocumentElement();

        if ( documentElement == null )
        {
            throw new IllegalArgumentException( "The provided Node's document does not have a document element." );
        }

        final String XMLNS = "http://www.w3.org/2000/xmlns/";
        final String at = "xmlns:" + prefix;

        if ( ! documentElement.hasAttributeNS( XMLNS, at ) )
        {
            documentElement.setAttributeNS( XMLNS, at, uri );
        }

        return node;
    }


    public static DocumentFragment evaluateAsFragment( String path, Node context )
    {
        final NodeList list = ( NodeList ) XPathUtils.evaluate( context, path, XPathConstants.NODESET );

        final DocumentFragment fragment = getDocument( context ).createDocumentFragment();

        for ( int i = 0, n = list.getLength(); i < n; i++ )
        {
            fragment.appendChild( list.item( i ) );
        }

        return fragment;
    }



    public static Node sort( Node node, String sortSelect )
    {
        StringBuilder b = new StringBuilder()
                .append( "<xsl:stylesheet version=\"1.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">" )
                .append( "\n<xsl:output method=\"xml\" version=\"1.0\" encoding=\"UTF-8\" indent=\"yes\"/>" )
                .append( "\n<xsl:template match=\"*\">" )
                .append( "\n<xsl:copy>" )
                .append( "\n<xsl:copy-of select=\"@*\"/>" )
                .append( "\n<xsl:apply-templates>" )
                .append( "\n<xsl:sort select=\"" ).append( sortSelect ).append( "\"/>" )
                .append( "\n</xsl:apply-templates>" )
                .append( "\n</xsl:copy>" )
                .append( "\n</xsl:template>" )
                .append( "\n</xsl:stylesheet>" );


        try
        {
            Templates templates = newTemplates( new StringReader( b.toString() ) );

            final Transformer transformer = templates.newTransformer();

            final DOMSource domSource = new DOMSource();
            domSource.setNode( node );

            final DOMResult domResult = new DOMResult();
            domResult.setNode( newDocument() );

            // transform the input xml using dom source and dom result
            transformer.transform( domSource, domResult );

            return domResult.getNode();

        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to sort node using select: [" + sortSelect + "]", e );
        }
    }
}
