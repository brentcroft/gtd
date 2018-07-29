package com.brentcroft.gtd.driver;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.brentcroft.util.DateUtils;
import com.brentcroft.util.Waiter8;
import com.brentcroft.util.XPathUtils;
import com.brentcroft.util.xpath.gob.Gob;

public interface GuiObjectLocator< T >
{
    /**
     * Takes a snapshot of the current GUI object tree by constructing and returning
     * a corresponding XML document.
     * <p/>
     * Identifies top level GUI objects and traverses over all descendant GUI
     * objects, extracting KEY_MODEL properties in elements in an XML document,
     * preserving the same hierarchical structure.<br/>
     * <p>
     * An element is created in the document for each GUI object visited and
     * KEY_MODEL properties of the GUI object are copied to element attributes.<br/>
     * <p>
     * <p>
     * Each GUI object is assigned as UserData to it's respective element. This
     * allows the snapshot document to be queried by x-path to access GUI
     * objects.<br/>
     *
     * @return a document providing an addressable space of GUI objects.
     */
    Document takeSnapshot();

    /**
     * Takes a snapshot of the current GUI object tree by constructing and returning
     * a corresponding XML document.
     * <p/>
     * Identifies top level GUI objects and traverses over all descendant GUI
     * objects, extracting KEY_MODEL properties in elements in an XML document,
     * preserving the same hierarchical structure.<br/>
     * <p>
     * An element is created in the document for each GUI object visited and
     * KEY_MODEL properties of the GUI object are copied to element attributes.<br/>
     * <p>
     * <p>
     * Each GUI object is assigned as UserData to it's respective element. This
     * allows the snapshot document to be queried by x-path to access GUI
     * objects.<br/>
     *
     * @return a document providing an addressable space of GUI objects.
     */
    Document takeSnapshot( Map< String, Object > options );

    void takeSnapshot( Object origin, Node node, Map< String, Object > options );

    Gob getGob( Object item );

    /**
     * Obtain the object located at the given path.
     *
     * @param path
     *            the path to obtain the component.
     * @return the obtained object or null if none was found.
     */
    @SuppressWarnings( "unchecked" )
	default T getObjectAtPath( String path )
    {
        // Gob gob = getGob();
        //
        // Selection s = Gobber
        // .newGobber( path )
        // .execute( gob );
        //
        // if ( s.isEmpty() )
        // {
        // return null;
        // }
        //
        // List< Gob > gobs = s.getGobs();
        //
        // if ( gobs == null || gobs.isEmpty() )
        // {
        // return null;
        // }
        //
        // if ( gobs.size() > 1 )
        // {
        // throw new RuntimeException( format( "Too many [%s] results for path: %s",
        // gobs.size(), path ) );
        // }
        //
        // return ( T ) gobs.get( 0 );

        final Node node = getNodeAtPath( path );

        return node == null
                ? null
				: ( T ) node.getUserData( Backend.GUI_OBJECT_KEY );
    }

    /**
     * Obtain the objects located at the given paths.
     *
     * @param paths
     *            the set of paths to obtain the objects.
     * @return the found objects or null if none were found.
     */
    @SuppressWarnings( "unchecked" )
	default T[] getObjectsAtPaths( String... paths )
    {
        if ( paths == null )
        {
            return null;
        }

        final Node[] nodes = getNodesAtPaths( paths );

        if ( nodes == null )
        {
            return null;
        }

        return Stream
                .of( getNodesAtPaths( paths ) )
                .map( node -> (T) node == null ? null : node.getUserData( Backend.GUI_OBJECT_KEY ) )
                .collect( Collectors.toList() )
                .toArray( (T[]) new Object[ nodes.length ] );
    }

    /**
     * Obtain the component registered at the given path.
     *
     * @param path
     *            the path to obtain the component.
     * @return the obtained component or null if none was found.
     */
    default T getGuiObject( double pollDelay, double timeout, String path )
    {
        final AtomicReference< T > ar = new AtomicReference<>();

        new Waiter8()
                .onTimeout( millis -> {
                    throw new LocatorException(
                            format( "Object does not exist at path [%s] after [%d] millis.",
                                    path,
                                    millis ) );
                } )
                .withDelayMillis( DateUtils.secondsToMillis( pollDelay ) )
                .withTimeoutMillis( DateUtils.secondsToMillis( timeout ) )
                .until( () -> {
                    ar.set( getObjectAtPath( path ) );
                    return ar.get() != null;
                } );

        return ar.get();
    }

    default T[] getGuiObjects( double pollInterval, double timeout, final String... paths )
    {
        final AtomicReference< T[] > ar = new AtomicReference<>();

        new Waiter8()
                .onTimeout( ( millis ) -> {
                    throw new LocatorException(
                            format( "Gave up waiting for objects after [%s] millis.",
                                    millis ) );
                } )
                .withDelayMillis( DateUtils.secondsToMillis( pollInterval ) )
                .withTimeoutMillis( DateUtils.secondsToMillis( timeout ) )
                .until( () -> {
                    ar.set( getObjectsAtPaths( paths ) );

                    return ar.get() != null;
                } );

        return ar.get();
    }

    default Node getNodeAtPath( String path )
    {
        try
        {
            return (Node) XPathUtils
                    .getCompiledPath( path )
                    .evaluate(
                            takeSnapshot(),
                            XPathConstants.NODE );
        }
        catch ( XPathExpressionException e )
        {
            throw new LocatorException( format( "Failed processing x-path expression: [%s]",
                    path ), e );
        }
    }

    default Node[] getNodesAtPaths( String... paths )
    {
        Document snapshot = takeSnapshot();

        Node[] nodes = new Node[ paths.length ];

        int[] index = { 0 };

        Arrays
                .asList( paths )
                .stream()
                .forEachOrdered( path -> {
                    try
                    {
                        nodes[ index[ 0 ]++ ] = (Node) XPathUtils
                                .getCompiledPath( path )
                                .evaluate(
                                        snapshot,
                                        XPathConstants.NODE );
                    }
                    catch ( XPathExpressionException e )
                    {
                        throw new LocatorException(
                                format( "Failed processing x-path expression: index=[%s], [%s]", index[ 0 ], path ),
                                e );
                    }
                } );

        return nodes;
    }
}