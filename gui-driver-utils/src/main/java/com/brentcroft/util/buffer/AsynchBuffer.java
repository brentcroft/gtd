package com.brentcroft.util.buffer;


import com.brentcroft.util.DateUtils;
import com.brentcroft.util.Waiter8;
import java.util.LinkedList;
import org.apache.log4j.Logger;

/**
 * Created by Alaric on 30/12/2016.
 */
public abstract class AsynchBuffer< T > implements Runnable
{
    private final static transient Logger logger = Logger.getLogger( AsynchBuffer.class );

    private long maxSize = - 1;
    private long maxEntries = - 1;

    private long delay = 0;

    private boolean fifo = true;

    private Queue< T > queue = new Queue< T >();


    public AsynchBuffer( String name )
    {
        Thread thread = new Thread( this, "AsynchBuffer_" + name );

        thread.setDaemon( true );

        thread.start();
    }

    public AsynchBuffer< T > withDelaySeconds( double delaySeconds )
    {
        return withDelay( DateUtils.secondsToMillis( delaySeconds ) );
    }

    public AsynchBuffer< T > withDelay( long delay )
    {
        this.delay = delay;
        return this;
    }

    public AsynchBuffer< T > withMaxEntries( long maxEntries )
    {
        this.maxEntries = maxEntries;
        queue.setMaxEntries( maxEntries );
        return this;
    }


    public AsynchBuffer< T > withFirstInFirstOut( boolean fifo )
    {
        this.fifo = fifo;
        return this;
    }

    public AsynchBuffer< T > withLastInFirstOut( boolean lifo )
    {
        this.fifo = ! lifo;
        return this;
    }


    @Override
    public void run()
    {
        if ( logger.isTraceEnabled() )
        {
            logger.trace( "Starting deamon run: " + Thread.currentThread().getName() );
        }

        long lastProcessed = 0;

        try
        {
            while ( true )
            {
                T queueEntry = null;

                synchronized ( queue )
                {
                    if ( queue.size() == 0 )
                    {
                        try
                        {
                            queue.wait();
                        }
                        catch ( InterruptedException e )
                        {
                            logger.debug( "Interrupted: " + e );
                        }

                        continue;
                    }
                    else
                    {
                        queueEntry = fifo
                                ? queue.removeFirst()
                                : queue.removeLast();
                    }
                }

                if ( queueEntry != null )
                {
                    lastProcessed = System.currentTimeMillis();

                    process( queueEntry );

                    long remainingDelay = delay - ( System.currentTimeMillis() - lastProcessed );

                    if ( remainingDelay > 0 )
                    {
                        Waiter8.delay( remainingDelay );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            logger.debug( "Exception raised while processing response queue.", e );
        }

        logger.info( "Finished. There were [" + queue.size() + "] items in the queue that were not sent." );
    }


    public void add( T t )
    {
        synchronized ( queue )
        {
            queue.addLast( t );
            queue.notifyAll();
        }
    }

    public int size()
    {
        return queue.size();
    }


    public int maxSize()
    {
        return queue.maxSize();
    }


    public abstract void process( T t );


    class Queue< T >
    {
        private int maxSize = 0;

        private long maxEntries = - 1;

        private LinkedList< T > linkedList;

        public Queue()
        {
            linkedList = new LinkedList< T >();
        }

        public void addLast( T m )
        {
            synchronized ( queue )
            {
                linkedList.addLast( m );

                if ( maxEntries >= 0 && linkedList.size() > maxEntries )
                {
                    linkedList.removeFirst();
                }

                maxSize = Math.max( maxSize, linkedList.size() );
            }
        }


        public T removeFirst()
        {
            synchronized ( queue )
            {
                return linkedList.removeFirst();
            }
        }


        public T removeLast()
        {
            synchronized ( queue )
            {
                return linkedList.removeLast();
            }
        }

        public int size()
        {
            return linkedList.size();
        }

        public int maxSize()
        {
            return maxSize;
        }


        public long getMaxEntries()
        {
            return maxEntries;
        }

        public void setMaxEntries( long maxEntries )
        {
            this.maxEntries = maxEntries;
        }
    }
}
