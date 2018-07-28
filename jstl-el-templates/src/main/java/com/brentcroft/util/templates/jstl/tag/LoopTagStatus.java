package com.brentcroft.util.templates.jstl.tag;


/**
 *
 * (see http://docs.oracle.com/javaee/6/api/javax/servlet/jsp/jstl/core/
 * LoopTagStatus.html)
 *
 * @author ADobson
 *
 * @param <T>
 */
public class LoopTagStatus<T>
{
    private final Integer begin;

    private final Integer end;

    private final Integer step;

    private int index = 0;

    private T current;

    public LoopTagStatus( Integer begin, Integer end, Integer step )
    {
        this.begin = begin;
        this.end = end;
        this.step = step;

        //
        if ( begin != null )
        {
            index = begin;
        }
    }

    public LoopTagStatus<T> withCurrent( T current )
    {
        this.current = current;
        return this;
    }


    public void increment()
    {
        index++;
    }

    public void increment( Integer step )
    {
        index = index + (step == null ? 1 : step);
    }

    // The item (from the collection) for the current round of iteration
    public T getCurrent()
    {
        return current;
    }

    // The zero-based index for the current round of iteration
    public int getIndex()
    {
        return index;
    }

    public int getCount()
    {
        return index + 1;
    }

    /**
     * Flag indicating whether the current round is the first pass through the
     * iteration.
     *
     * @return true if the current iteration is the first
     */
    public boolean isFirst()
    {
        return begin == null ? index == 0 : ( index <= begin );
    }

    /**
     * Flag indicating whether the current round is the last pass through the
     * iteration (or null if we don't know).
     *
     * @return null or boolean is last pass of iteration
     */
    public Boolean isLast()
    {
        return end == null ? null : ( index >= end );
    }

    Integer getBegin()
    {
        return begin;
    }

    Integer getEnd()
    {
        return end;
    }

    int getStep()
    {
        return step;
    }

    public void setIndex( Integer begin )
    {
        index = ( begin == null ? 0 : begin );
    }
}