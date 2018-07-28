package com.brentcroft.gtd.adapter.utils;

import com.brentcroft.gtd.driver.utils.HashCache;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import org.apache.log4j.Logger;

import static java.lang.String.format;

/**
 * Created by Alaric on 23/12/2016.
 */
public class HashCacheImpl< T > implements HashCache< T >
{
    private static final Logger logger = Logger.getLogger( HashCacheImpl.class );

    private HashMap< Integer, WeakReference< T > > cache = new HashMap<>();

    private boolean enabled = true;

    private long hits = 0;
    private long throughput = 0;
    

    @Override
    public int getCacheSize()
    {
        return cache == null
                ? - 1
                : cache.size();
    }


    @Override
    public T getCachedObject( Integer hash )
    {
        if ( ! cache.containsKey( hash ) )
        {
            return null;
        }

        // another thread may have just removed the entry
        WeakReference< T > wr = cache.get( hash );

        if ( wr == null )
        {
            return null;
        }

        // walk past the weak reference
        T cachee = wr.get();

        if ( cachee != null )
        {
            hits++;
            return cachee;
        }
        else
        {
            cache.remove( hash );
        }

        return null;
    }


    @Override
    public void cacheObject( T cachee )
    {
        if ( enabled
             && cachee != null
             && ! cache.containsKey( cachee.hashCode() ) )
        {
            // put weak reference so don't stop GC of cachees
            cache.put(
                    cachee.hashCode(),
                    new WeakReference< T >( cachee ) );

            throughput++;

            // every X hits check cache size 
            // and if bigger than Y then call gc 
            // else log a report
            if ( ( throughput % 1000 == 0 ) )
            {
                long cacheSize = getCacheSize();

                if ( cacheSize > 10000 )
                {
                    gc();
                }
                else if ( logger.isDebugEnabled() )
                {
                    logger.debug( getReport() );
                }
            }
        }
    }


    @Override
    public boolean isEnabled()
    {
        return enabled;
    }

    @Override
    public void setEnabled( boolean enabled )
    {
        this.enabled = enabled;

        if ( ! enabled )
        {
            cache.clear();

            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Disabled and cleared cache." );
            }
        }
        else
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Enabled cache." );
            }
        }
    }


    @Override
    public String getReport()
    {
        return format( "Status: cache-size=[%s], hits=[%s], throughput=[%s].",
                getCacheSize(),
                hits,
                throughput );
    }

    private void removeDeadCachees()
    {
        cache.values().removeIf( ( value ) -> value.get() == null );
    }


    @Override
    public void gc()
    {
        int oldCacheSize = getCacheSize();

        removeDeadCachees();

        int cleanCacheSize = getCacheSize();

        System.gc();

        int newCacheSize = getCacheSize();

        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "GC: hits=[%s], throughput=[%s], new-cache-size=[%s], old-cache-size=[%s], dead=[%s], collected=[%s].",
                    hits,
                    throughput,
                    newCacheSize,
                    oldCacheSize,
                    oldCacheSize - cleanCacheSize,
                    cleanCacheSize - newCacheSize
            ) );
        }
    }
}
