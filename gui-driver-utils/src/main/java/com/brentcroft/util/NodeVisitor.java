package com.brentcroft.util;

import org.w3c.dom.Node;

public interface NodeVisitor
{
    boolean open( Node node );

    default void close( Node node )
    {

    }

    default Node visit( Node node )
    {
        if ( open( node ) )
        {
            if ( node.hasChildNodes() )
            {
                for ( Node c = node.getFirstChild(); c != null; c = c.getNextSibling() )
                {
                    visit( c );
                }
            }

            close( node );
        }

        return node;
    }
}
