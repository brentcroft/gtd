package com.brentcroft.util.xpath.gob;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;

public interface Gob
{
    String getComponentTag();

    List< Attribute > getAttributes();

    boolean hasAttribute( String name );

    String getAttribute( String name );

    boolean hasChildren();

    List< ? extends Gob > getChildren();

    Gob getParent();


    class NSpace implements Namespace
    {
        final static List< Namespace > NAMESPACES = new ArrayList<>();

        private final String uri;
        private final String prefix;

        public NSpace( String uri, String prefix )
        {
            this.uri = uri;
            this.prefix = prefix;
        }

        @Override
        public String getUri()
        {
            return uri;
        }

        @Override
        public String getPrefix()
        {
            return prefix;
        }
    }


    class Attr implements Attribute
    {
        private final Namespace namespace;
        private final String name;
        private final String value;

        public Attr( String name, String value )
        {
            this.namespace = null;
            this.name = name;
            this.value = value;
        }

        public Attr( Namespace namespace, String name, String value )
        {
            this.namespace = namespace;
            this.name = name;
            this.value = value;
        }

        @Override
        public Namespace getNameSpace()
        {
            return namespace;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public String getValue()
        {
            return value;
        }


        public String toString()
        {
            return format( " %s%s=\"%s\"",
                    getNameSpace() == null
                            ? ""
                            : getNameSpace().getPrefix() + ":",
                    getName(),
                    getValue() );
        }
    }

    Selection RESULT_CHILD = new Selection();
    Selection RESULT_ATTRIBUTE = new Selection().withAxis( Axis.ATTRIBUTE );


    default Namespace namespaceForUriAndPrefix( String uri, String prefix )
    {
        for ( Namespace n : NSpace.NAMESPACES )
        {
            if ( n.getUri().compareTo( uri ) == 0 && n.getPrefix().compareTo( prefix ) == 0 )
            {
                return n;
            }
        }

        Namespace n = new NSpace( uri, prefix );

        NSpace.NAMESPACES.add( n );

        return n;
    }

    default Attribute attributeForNameAndValue( String name, String value )
    {
        return new Attr( null, name, value );
    }


    default Attribute attributeForNSNameAndValue( Namespace ns, String name, String value )
    {
        return new Attr( ns, name, value );
    }

    default Attribute attributeForNSNameAndValue( String uri, String prefix, String name, String value )
    {
        return new Attr( namespaceForUriAndPrefix( uri, prefix ), name, value );
    }

}
