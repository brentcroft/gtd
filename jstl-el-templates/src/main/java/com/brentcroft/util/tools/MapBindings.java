package com.brentcroft.util.tools;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.script.Bindings;


/**
 * Utility hierarchical Map<String, Object> with builder methods for chaining
 * and that implements Bindings.
 *
 *
 * @author ADobson
 *
 */
public class MapBindings extends LinkedHashMap<String, Object> implements Bindings
{
    private static final long serialVersionUID = 8422258558562588221L;

    private Map<String, Object> parent;


    public MapBindings( Map<String, Object> parent )
    {
        this.parent = parent;
    }

    public MapBindings()
    {
        this( (Map<String, Object>) null );
    }

    public Map<String, Object> getParent()
    {
        return parent;
    }


    /**
     * Copies all entries from this MapBindings into the specified target.
     * <p/>
     *
     * If there is a parent then traverses to parent first:
     * if parent is a MapBindings then does parent.copyTo( target ), otherwise just copies the parents entries.
     *
     * @param target
     */
    public void copyTo( Map<String, Object> target )
    {
        if ( target == null )
        {
            return;
        }
        else if ( parent != null  )
        {
            if ( parent instanceof MapBindings)
            {
                ( (MapBindings) parent ).copyTo( target );
            }
            else
            {
                target.putAll( parent );
            }
        }

        target.putAll( this );
    }

    @Override
    public Object get( Object a )
    {
        //noinspection SuspiciousMethodCalls
        return super.containsKey( a )
                ? super.get( a )
                : ( ( ( parent != null ) && parent.containsKey( a ) )
                    ? parent.get( a )
                    : null );
    }


    @Override
    public boolean containsKey( Object a )
    {
        return super.containsKey( a ) || ( parent != null && parent.containsKey( a ) );
    }


    /**
     * Arguments must be a sequence of name/value pairs.
     *
     * @param entries
     */
    public MapBindings( Map<String, Object> parent, Object... entries )
    {
        this( parent );

        withEntries( entries );
    }

    /**
     * Arguments must be a sequence of name/value pairs.
     *
     * @param entries
     */
    public MapBindings( Object... entries )
    {
        this( null, entries );
    }


    public MapBindings withParent( Map<String, Object> parent )
    {
        this.parent = parent;
        return this;
    }

    public MapBindings withBindings( Bindings parent )
    {
        return withParent( parent );
    }

    public MapBindings withNamespace( MapBindings parent )
    {
        return withParent( parent );
    }

    public MapBindings withMap( Map<String, ?> map )
    {
        putAll( map );
        return this;
    }


    public MapBindings withEntry( String name, Object value )
    {
        this.put( name, value );
        return this;
    }

    /**
     * Use <code>withMap(Map<String, ?> map)</code> instead.
     *
     * @param entries
     * @return
     */
    @Deprecated( )
    public MapBindings withEntries( Map<String, Object> entries )
    {
        this.putAll( entries );
        return this;
    }

    public MapBindings withEntries( Object... entries )
    {
        if ( entries != null )
        {
            if ( entries.length % 2 != 0 )
            {
                throw new IllegalArgumentException( String.format( "Must have an even number of entries: [%s]", entries.length ) );
            }

            final int step = 2;

            for ( int i = 0, n = entries.length ; i < ( n - 1 ) ; i = i + step )
            {
                withEntry( entries[ i ].toString(), entries[ i + 1 ] );
            }
        }
        return this;
    }


    public String toString()
    {
        if ( parent == null )
        {
            return super.toString();
        }
        else
        {
            return super.toString() + ( "; " + parent.toString() );
        }
    }
}
