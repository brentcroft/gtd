package com.brentcroft.util;

/**
 * Created by Alaric on 17/07/2017.
 */
public class TimeKeySequence
{
    private long lastTimestamp = Long.MIN_VALUE;
    private long lastIndex = Long.MIN_VALUE;

    private long serial = 0;
    private long maximumIndex = 0;

    private final Type type;

    interface Joiner
    {
        String join( long visitor, long index );
    }

    enum Type
    {
        simple()
                {{
                    joiner = ( v, i ) -> v + ":" + i;
                }};

        Joiner joiner;
    }

    private TimeKeySequence( Type type )
    {
        this.type = type;
    }

    public static TimeKeySequence newSimpleTimeKeySequence()
    {
        return new TimeKeySequence( Type.simple );
    }


    public synchronized String nextValue()
    {
        long visitor = System.currentTimeMillis();

        if ( visitor == lastTimestamp )
        {
            lastIndex++;
        }
        else
        {
            maximumIndex = Math.max( maximumIndex, lastIndex );

            lastTimestamp = visitor;
            lastIndex = 0;
        }

        serial++;

        return type.joiner.join( visitor, lastIndex );
    }


    public long getSerial()
    {
        return serial;
    }

    public long getMaximumIndex()
    {
        return maximumIndex;
    }
}
