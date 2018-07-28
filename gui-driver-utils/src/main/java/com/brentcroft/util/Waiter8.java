package com.brentcroft.util;

/**
 * Created by Alaric on 15/07/2017.
 */
public final class Waiter8
{
    public static final long MINIMUM_DELAY = 50;

    private long delayMillis = 100;
    private long timeoutMillis = 1000;
    private long wait = 0;

    private Until until;
    private Timeout timeout;


    public static void delay( long delay )
    {
        new Waiter8().withDelayMillis( delay ).delay();
    }

    public interface Until
    {
        boolean until();
    }

    public interface Timeout
    {
        void timeout( long millis );
    }

    public Waiter8 until( Until until)
    {
        this.until = until;
        return start();
    }

    public Waiter8 onTimeout( Timeout timeout)
    {
        this.timeout = timeout;
        return this;
    }


    public Waiter8 withDelayMillis( long delayMillis )
    {
        this.delayMillis = Math.max( MINIMUM_DELAY, delayMillis );
        return this;
    }

    public Waiter8 withTimeoutMillis( long timeoutMillis )
    {
        this.timeoutMillis = timeoutMillis;
        return this;
    }



    /**
     * private wrapper to catch and process exceptions
     *
     * @return
     */
    private boolean doUntil()
    {
        try
        {
            return until != null
                   && until.until();
        }
        catch ( Exception e )
        {
            throw new UntilException( e );
        }
    }

    private void doTimeout( long millis )
    {
        if ( timeout == null )
        {
            throw new TimeoutException( "Gave up waiting after [" + millis + "] millis." );
        }
        else
        {
            timeout.timeout( millis );
        }
    }


    private Waiter8 start()
    {
        if ( timeoutMillis < 0 )
        {
            // may be overridden to not throw an exception
            doTimeout(timeoutMillis);

            return this;
        }

        long started = System.currentTimeMillis();

        while ( !doUntil() )
        {
            wait = System.currentTimeMillis() - started;

            if ( wait >= timeoutMillis )
            {
                // may be overridden to not throw an exception
                doTimeout(wait);

                return this;
            }
            else
            {
                delay();
            }
        }

        return this;
    }

    public long getDelayMillis()
    {
        return delayMillis;
    }

    public long getTimeout()
    {
        return timeoutMillis;
    }

    public long getWait()
    {
        return wait;
    }

    public void delay()
    {
        try
        {
            Thread.sleep( delayMillis );
        }
        catch ( Exception e )
        {
            throw new TimeoutException( "Interrupted while delaying.", e );
        }
    }

    public static class TimeoutException extends RuntimeException
    {
        private static final long serialVersionUID = 630846266454358078L;

        public TimeoutException( String message )
        {
            super( message );
        }

        public TimeoutException( String message, Throwable t )
        {
            super( message, t );
        }
    }

    public static class UntilException extends RuntimeException
    {
        private static final long serialVersionUID = -7790280853324279503L;

        public UntilException( Throwable t )
        {
            super( t );
        }
    }

}
