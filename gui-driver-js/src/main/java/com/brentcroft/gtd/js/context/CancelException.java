package com.brentcroft.gtd.js.context;

/**
 * Specifically to be thrown from scripts.
 * <p>
 * Created by Alaric on 04/07/2017.
 */
public class CancelException extends RuntimeException
{
    public CancelException( String m )
    {
        super( m );
    }

    public CancelException( String m, Throwable t )
    {
        super( t.getMessage() == null
                ? m
                : t.getMessage() );
    }

    public CancelException( Throwable t )
    {
        super( t.getMessage() );
    }
}
