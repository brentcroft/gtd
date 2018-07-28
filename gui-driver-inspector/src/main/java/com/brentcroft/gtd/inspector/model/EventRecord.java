package com.brentcroft.gtd.inspector.model;

import static java.lang.String.format;

/**
 * Created by Alaric on 31/12/2016.
 */
public class EventRecord
{
    private final long seq;
    private final String eventId;
    private final String param;
    private final String hash;

    private final String message;

    public EventRecord( long seq, String eventId, String param, String hash, String message )
    {
        this.seq = seq;
        this.eventId = eventId;
        this.param = param;
        this.hash = hash;
        this.message = message;
    }


    public long getSeq()
    {
        return seq;
    }

    public String getEventId()
    {
        return eventId;
    }

    public String getParam()
    {
        return param;
    }

    public String getHash()
    {
        return hash;
    }

    public String toString()
    {
        if ( message == null)
        {
            return format( "EventRecord: hash=[%s], id=[%s], seq=[%s], param=[%s]", hash, eventId, seq, param );
        }
        else
        {
            return message;
        }
    }
}
