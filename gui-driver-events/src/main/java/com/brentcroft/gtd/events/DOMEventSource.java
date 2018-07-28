package com.brentcroft.gtd.events;


import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.w3c.dom.Document;


/**
 * DOMEventSource delivers notifications of new documents, once loaded, to consumers.
 */
public class DOMEventSource
{
    private static final List< Consumer< Document > > consumers = new ArrayList<>();

    public static void notifyNewDocument( Document document )
    {
        consumers.forEach( consumer -> consumer.accept( document ) );
    }

    public static void addConsumer( Consumer< Document > consumer )
    {
        if ( ! consumers.contains( consumer ) )
        {
            consumers.add( consumer );
        }
    }

    public static void removeConsumer( Consumer< Document > consumer )
    {
        if ( consumers.contains( consumer ) )
        {
            consumers.remove( consumer );
        }
    }
}
