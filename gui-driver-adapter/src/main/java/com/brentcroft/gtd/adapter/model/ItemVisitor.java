package com.brentcroft.gtd.adapter.model;

/**
 * Created by Alaric on 14/07/2017.
 */
public interface ItemVisitor< T >
{
    boolean isDuplicate( T item );

    ItemState open( T item );

    boolean close( T item );

    enum ItemState
    {
        DUPLICATE,
        INSERT
    }
}
