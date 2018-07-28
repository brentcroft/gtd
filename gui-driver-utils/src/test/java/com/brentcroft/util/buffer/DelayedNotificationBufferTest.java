package com.brentcroft.util.buffer;

import com.brentcroft.util.Waiter8;
import org.junit.Test;

/**
 * Created by Alaric on 23/12/2016.
 */
public class DelayedNotificationBufferTest
{
    @Test
    public void maybeNotify()
    {
        final DelayedNotificationBuffer< String > dnt = new DelayedNotificationBuffer< String >()
        {
            @Override
            public void process( String n )
            {
                System.out.println( n );
            }
        }
                .withDelayMillis( 100 );


        dnt.notify( "red" );
        dnt.notify( "green" );
        dnt.notify( "blue" );
        dnt.notify( "orange" );

        new Waiter8()                
                .withDelayMillis( 1000 )
                .withTimeoutMillis( 10 * 1000 )
                .until( () -> ! dnt.isPending() );
    }

}