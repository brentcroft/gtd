package com.brentcroft.util;

import com.brentcroft.util.xpath.gob.Gob;
import com.brentcroft.util.xpath.ParseException;
import com.brentcroft.util.xpath.SimpleNode;
import com.brentcroft.util.xpath.XParser;
import com.brentcroft.util.xpath.XParserVisitor;
import com.brentcroft.util.xpath.gob.Selection;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import javax.xml.xpath.XPathVariableResolver;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static java.lang.String.format;


/**
 * Created by adobson on 19/05/2016.
 */
@SuppressWarnings( "rawtypes" )
public class XPathUtils implements XPathFunctionResolver, XPathVariableResolver
{
    private Map< String, XPathFunction > xPathFunctions = new HashMap< String, XPathFunction >();
    private Map< ? extends String, ? > xPathVariables = null;

    private static final XPathFactory X_PATH_FACTORY = XPathFactory.newInstance();

    private static final Map< String, XPathExpression > CACHED_XPATH_MAP = new HashMap< String, XPathExpression >();


    private static XPathUtils instance = new XPathUtils();

    static
    {
        X_PATH_FACTORY.setXPathFunctionResolver( instance );
        X_PATH_FACTORY.setXPathVariableResolver( instance );
    }


    public static void setVariables( Map< ? extends String, ? > variables )
    {
        instance.xPathVariables = variables;
    }


    public static String evaluate( String path )
    {
        Node node;

        try
        {
            node = XmlUtils.newDocument();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to create new document.", e );
        }

        return evaluate( node, path );
    }

    public static String evaluate( Node origin, String path )
    {
        try
        {
            return getCompiledPath( path )
                    .evaluate( origin );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to process xpath: " + path, e );
        }
    }

    public static Object evaluate( Node origin, String path, QName resultType )
    {
        try
        {
            return getCompiledPath( path )
                    .evaluate( origin, resultType );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to process xpath: " + path, e );
        }
    }

    public static Node getNode( Node origin, String path )
    {
        try
        {
            return ( Node ) getCompiledPath( path )
                    .evaluate( origin, XPathConstants.NODE );
        }
        catch ( XPathExpressionException e )
        {
            throw new RuntimeException( "Failed to process xpath: " + path, e );
        }
    }


    public static Selection visitPath( XParserVisitor visitor, Gob origin, String path )
    {
        SimpleNode xpathTree = null;

        try
        {
            xpathTree = new XParser(
                    new ByteArrayInputStream(
                            path.getBytes( StandardCharsets.UTF_8.name() ) ) )
                    .START();

        }
        catch ( UnsupportedEncodingException | ParseException e )
        {
            throw new RuntimeException( "Failed to process xpath: " + path, e );
        }

        xpathTree.dump( "  " );

        return visitor.visit( xpathTree, origin, null );
    }


    /**
     * Compile, cache and return an XPathExpression for the supplied path string.
     *
     * @param path
     * @return
     * @throws XPathExpressionException
     */
    public static XPathExpression getCompiledPath( String path )
    {
        if ( CACHED_XPATH_MAP.containsKey( path ) )
        {
            return CACHED_XPATH_MAP.get( path );
        }

        try
        {
            final XPathExpression newExpression = X_PATH_FACTORY.newXPath().compile( path );

            CACHED_XPATH_MAP.put( path, newExpression );

            return newExpression;
        }
        catch ( XPathExpressionException e )
        {
            throw new RuntimeException( format( "Failed to compile path [%s].", path ), e );
        }
    }


    @Override
    public XPathFunction resolveFunction( QName functionName, int arity )
    {
        // not checking prefix
        return xPathFunctions.get( functionName.getLocalPart() );
    }

    {
        xPathFunctions.put( "current-date", args -> {
            if ( args == null )
            {
                return new Date();
            }

            return DateUtils.getCurrentDate( convert( args ) );
        } );

        xPathFunctions.put( "absolute-date", args -> {
            if ( args == null )
            {
                throw new XPathFunctionException(
                        "XPathFunction: absolute-date( int... parts) requires at least one argument." );
            }

            return DateUtils.getAbsoluteDate( convert( args ) );
        } );

        xPathFunctions.put( "date-year", args -> {
            if ( args == null || args.size() != 1 )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-year( Date date ) requires one argument." );
            }

            if ( ! ( args.get( 0 ) instanceof Date ) )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-year( Date date ) requires one argument that is a Date." );
            }

            return DateUtils.dateYear( ( Date ) args.get( 0 ) );
        } );

        xPathFunctions.put( "date-month", args -> {
            if ( args == null || args.size() != 1 )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-month( Date date ) requires one argument." );
            }

            if ( ! ( args.get( 0 ) instanceof Date ) )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-month( Date date ) requires one argument that is a Date." );
            }

            return DateUtils.dateMonth( ( Date ) args.get( 0 ) );
        } );


        xPathFunctions.put( "date-day", args -> {
            if ( args == null || args.size() != 1 )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-day( Date date ) requires one argument." );
            }

            if ( ! ( args.get( 0 ) instanceof Date ) )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-day( Date date ) requires one argument that is a Date." );
            }

            return DateUtils.dateDay( ( Date ) args.get( 0 ) );
        } );


        xPathFunctions.put( "date-hour", args -> {
            if ( args == null || args.size() != 1 )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-hour( Date date ) requires one argument." );
            }

            if ( ! ( args.get( 0 ) instanceof Date ) )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-hour( Date date ) requires one argument that is a Date." );
            }

            return DateUtils.dateHour( ( Date ) args.get( 0 ) );
        } );


        xPathFunctions.put( "date-minute", args -> {
            if ( args == null || args.size() != 1 )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-minute( Date date ) requires one argument." );
            }

            if ( ! ( args.get( 0 ) instanceof Date ) )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-minute( Date date ) requires one argument that is a Date." );
            }

            return DateUtils.dateMinute( ( Date ) args.get( 0 ) );
        } );


        xPathFunctions.put( "date-second", args -> {
            if ( args == null || args.size() != 1 )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-second( Date date ) requires one argument." );
            }

            if ( ! ( args.get( 0 ) instanceof Date ) )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-second( Date date ) requires one argument that is a Date." );
            }

            return DateUtils.dateSecond( ( Date ) args.get( 0 ) );
        } );


        xPathFunctions.put( "date-millis", args -> {
            if ( args == null || args.size() != 1 )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-millis( Date date ) requires one argument." );
            }

            if ( ! ( args.get( 0 ) instanceof Date ) )
            {
                throw new XPathFunctionException(
                        "XPathFunction: date-millis( Date date ) requires one argument that is a Date." );
            }

            return DateUtils.dateMillis( ( Date ) args.get( 0 ) );
        } );
    }


    private static int[] convert( List args )
    {
        int[] v = new int[ args.size() ];
        int i = 0;
        for ( Object o : args )
        {
            v[ i++ ] = Double.valueOf( o.toString() ).intValue();
        }
        return v;
    }

    @Override
    public Object resolveVariable( QName variableName )
    {
        return xPathVariables.get( variableName.getLocalPart() );
    }


    public static class Node2XPathFunctions
    {

        public static String getSimpleXPath( Node node )
        {
            Node parent = node.getParentNode();

            if ( parent == null )
            {
                return "/";
            }
            return getSimpleXPath( parent ) + "/" + node.getNodeName();
        }

        public static String getIndexedXPath( Node node )
        {
            Node parent = node.getParentNode();

            if ( parent == null )
            {
                return "/";
            }

            int tagIndex = getIndexInParent( node );


            return getIndexedXPath( parent )
                   + "/" + node.getNodeName()
                   + "[" + tagIndex + "]";
        }


        public static int getIndexInParent( Node node )
        {
            int tagIndex = 1;

            Node s = node.getPreviousSibling();

            while ( s != null )
            {
                if ( s instanceof Element && node instanceof Element )
                {
                    Element e = ( Element ) s;
                    Element ne = ( Element ) node;

                    // do the siblings force disambiguation
                    if ( ne.getTagName().equals( e.getTagName() ) )
                    {
                        // TODO: now do more disambiguation analysis
                        tagIndex++;
                    }
                }

                // and back up
                s = s.getPreviousSibling();
            }

            return tagIndex;
        }
    }


}
