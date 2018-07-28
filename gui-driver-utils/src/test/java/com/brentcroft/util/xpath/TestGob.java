package com.brentcroft.util.xpath;

import com.brentcroft.util.TextUtils;
import com.brentcroft.util.xpath.gob.Attribute;
import com.brentcroft.util.xpath.gob.Gob;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import static java.lang.String.format;

public class TestGob implements Gob
{
    private final String tag;
    private List< Attribute > attributes;
    private Gob parent;
    private Set< Gob > children;

    public TestGob( String tag )
    {
        this.tag = tag;
    }

    public String toString()
    {
        return toString( "\t" );
    }

    public String toString( String indent )
    {
        String attrText = attributes == null || attributes.isEmpty()
                ? ""
                : " " + attributes
                        .stream()
                        .map( a -> format( "%s=\"%s\"", a.getName(), a.getValue() ) )
                        .collect( Collectors.joining( " " ) );

        String childrenText = children == null || children.isEmpty()
                ? ""
                : TextUtils.indentList( children, indent );

        // don't serialize root element
        return tag == null
                ? "<?xml version=\"1.0\"?>" + TextUtils.indentList( children, "" )
                : children == null || children.isEmpty()
                        ? format( "<%s%s/>", tag, attrText )
                        : format( "<%s%s>%s</%s>", tag, attrText, childrenText, tag );
    }


    public TestGob withAttributes( Attribute... attr )
    {
        if ( this.attributes == null )
        {
            this.attributes = new ArrayList<>();
        }

        if ( attributes != null )
        {
            this.attributes.addAll( Arrays.asList( attr ) );
        }

        return this;
    }


    public TestGob withParent( Gob gob )
    {
        this.parent = gob;

        return this;
    }


    public TestGob withChildren( Gob... children )
    {
        if ( this.children == null )
        {
            this.children = new LinkedHashSet<>();
        }

        if ( children != null )
        {
            this.children.addAll( Arrays.asList( children ) );
            this.children.forEach( c -> ( ( TestGob ) c ).withParent( this ) );
        }

        return this;
    }


    @Override
    public boolean hasChildren()
    {
        return children != null && ! children.isEmpty();
    }

    @Override
    public List< Gob > getChildren()
    {
        return new ArrayList<>( children );
    }

    @Override
    public Gob getParent()
    {
        return parent;
    }


    @Override
    public String getComponentTag()
    {
        return tag;
    }

    @Override
    public List< Attribute > getAttributes()
    {
        return attributes;
    }

    @Override
    public boolean hasAttribute( String name )
    {
        return attributes != null && attributes
                .stream()
                .anyMatch( attribute -> attribute.getName().equals( name ) );
    }

    @Override
    public String getAttribute( String name )
    {
        Attribute a = attributes
                .stream()
                .filter( attribute -> attribute.getName().equals( name ) )
                .findFirst()
                .get();

        return a == null
                ? null
                : a.getValue();
    }


    public static Gob fromText( String text )
    {
        TestGob gobDoc = new TestGob( null );

        DefaultHandler handler = new DefaultHandler()
        {
            Stack< Gob > stack = new Stack<>();

            public void startElement( String uri, String localName,
                                      String qName, Attributes attributes )
                    throws SAXException
            {
                TestGob gob = new TestGob( qName );

                if ( attributes != null )
                {
                    List< Gob.Attr > gobAttr = new ArrayList<>();

                    for ( int i = 0, n = attributes.getLength(); i < n; i++ )
                    {
                        gobAttr.add( new Gob.Attr( attributes.getLocalName( i ), attributes.getValue( i ) ) );
                    }

                    if ( ! gobAttr.isEmpty() )
                    {
                        gob.withAttributes( gobAttr.toArray( new Gob.Attr[ gobAttr.size() ] ) );
                    }
                }

                Gob parent = stack.isEmpty()
                        ? null
                        : stack.peek();

                // this also sets the parent on the child
                ( ( TestGob ) ( parent == null
                        ? gobDoc
                        : parent ) ).withChildren( gob );

                stack.push( gob );
            }

            public void endElement( String uri, String localName, String qName )
                    throws SAXException
            {
                stack.pop();
            }
        };

        try
        {
            SAXParserFactory.newInstance().newSAXParser().parse( new InputSource( new StringReader( text ) ), handler );
        }
        catch ( SAXException | IOException | ParserConfigurationException e )
        {
            throw new RuntimeException( e );
        }

        return gobDoc;
    }
}