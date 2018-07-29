package com.brentcroft.util.buffer;

import com.brentcroft.util.Waiter8;
import org.junit.Test;

import static java.lang.String.format;

/**
 * Created by Alaric on 30/12/2016.
 */
public class AsynchBufferTest
{

    @Test
    public void superFastProcess()
    {
        // requires a timeout fudge because it must take longer than zero millis
        final long timeoutFudge = 10;

        final long delayMillis = 0;
        final long numEntries = 100000;

        final long[] processed = { 0 };

        final AsynchBuffer< String > ab = new AsynchBuffer< String >( "Super Fast" ) {
            @Override
            public void process( String s )
            {
                processed[ 0 ]++;
            }
        }
                .withDelay( delayMillis );

        runTheQueue( ab, delayMillis, numEntries, timeoutFudge );
    }

    @Test
    public void slowProcess()
    {
        final long delayMillis = 100;
        final long numEntries = 50;

        final long[] processed = { 0 };

        final AsynchBuffer< String > ab = new AsynchBuffer< String >( "Slow" ) {
            @Override
            public void process( String s )
            {
                processed[ 0 ]++;
            }
        }
                .withDelay( delayMillis );

        // requires a timeout fudge proportional to the numer of items
        final long timeoutFudge = numEntries * 10;

        runTheQueue( ab, delayMillis, numEntries, timeoutFudge );
    }

    @Test
    public void superSlowProcess()
    {
        final long delayMillis = 1000;
        final long numEntries = 5;

        final long[] processed = { 0 };

        final AsynchBuffer< String > ab = new AsynchBuffer< String >( "Super Slow" ) {
            @Override
            public void process( String s )
            {
                processed[ 0 ]++;
            }
        }
                .withDelay( delayMillis );

        runTheQueue( ab, delayMillis, numEntries );
    }

    @Test
    public void onlyMaxEntriesOneDelay100()
    {
        final long delayMillis = 1000;
        final long numEntries = 100;
        final int maxEntries = 1;

        final long[] processed = { 0 };

        final AsynchBuffer< String > ab = new AsynchBuffer< String >( "Max Entries 1 Slow" ) {
            @Override
            public void process( String s )
            {
                // minimal processing
                processed[ 0 ]++;
            }
        }
                .withMaxEntries( maxEntries )
                .withDelay( delayMillis );

        runTheQueue(
                ab,
                delayMillis,
                numEntries );
    }

    @Test
    public void onlyMaxEntriesOneFast()
    {
        final long delayMillis = 10;
        final long numEntries = 10000;
        final int maxEntries = 1;

        final long[] processed = { 0 };

        final AsynchBuffer< String > ab = new AsynchBuffer< String >( "Max Entries 1 Fast" ) {
            @Override
            public void process( String s )
            {
                // minimal processing
                processed[ 0 ]++;
            }
        }
                .withMaxEntries( maxEntries )
                .withDelay( delayMillis );

        runTheQueue(
                ab,
                delayMillis,
                numEntries );
    }

    private void addEntriesToQueue( final AsynchBuffer< String > ab, final long numEntries )
    {
        for ( int i = 0; i < numEntries; i++ )
        {
            ab.add( "" + i );
        }
    }

    private long waitForQueueToEmpty( final AsynchBuffer< String > ab, final long timeout )
    {
        System.out.println( format( "Waiting for queue to empty: timeout=[%s]", timeout ) );

        return new Waiter8()
                .withDelayMillis( 10 )
                .withTimeoutMillis( timeout )
                .onTimeout( millis -> {
                    throw new Waiter8.TimeoutException(
                            "Timed out after [" + millis + "] millis waiting for queue to empty: queue-size=["
                                    + ab.size() + "]\n max-queue-size=[" + ab.maxSize() + "]." );
                } )
                .until( () -> ab.size() == 0 )
                .getWait();
    }

    private void runTheQueue( final AsynchBuffer< String > ab, final long delayMillis, final long numEntries )
    {
        runTheQueue( ab, delayMillis, numEntries, 0 );
    }

    private void runTheQueue( final AsynchBuffer< String > ab, final long delayMillis, final long numEntries,
            final long timeoutFudge )
    {
        long started = System.currentTimeMillis();

        System.out.println( format( "Adding [%s] entries to queue: delay=[%s]", numEntries, delayMillis ) );

        addEntriesToQueue( ab, numEntries );

        long filled = System.currentTimeMillis();

        long wait = waitForQueueToEmpty( ab, ( delayMillis * numEntries ) + timeoutFudge );

        long finished = System.currentTimeMillis();
        long duration = finished - started;

        System.out.println( "Queue emptied:" +
                "\n duration=[" + duration + "]" +
                "\n throughput=[" + numEntries + "]" +
                "\n max-queue-size=[" + ab.maxSize() + "]" +
                "\n fill=[" + ( filled - started ) + "]" +
                "\n wait=[" + wait + "]." );
    }
}