package com.brentcroft.util.xpath.gob;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;

public class Selection
{
    public static final Selection TRUE = new Selection().withBoolean( true );
    public static final Selection FALSE = new Selection().withBoolean( false );

    private Axis axis = Axis.CHILD;
    private Set< Gob > gobs;

    private String text;
    private Number number;
    private Boolean booleanValue;


    public Axis getAxis()
    {
        return axis;
    }

    public List< Gob > getGobs()
    {
        return gobs == null
                ? null
                : new ArrayList<>( gobs );
    }

    public String getText()
    {
        return text;
    }

    public Number getNumber()
    {
        return number;
    }

    public Boolean getBoolean()
    {
        return booleanValue;
    }


    public boolean isGobs()
    {
        return gobs != null;
    }

    public boolean isText()
    {
        return text != null;
    }

    public boolean isBoolean()
    {
        return booleanValue != null;
    }

    public boolean isNumber()
    {
        return number != null;
    }


    public boolean isEmpty()
    {
        return ! isGobs() && ! isText() && ! isNumber() && ! isBoolean();
    }


    public Integer indexForGob( Gob gob )
    {
        if ( ! isGobs() )
        {
            return null;
        }

        int index = 1;
        for ( Gob c : getGobs() )
        {
            if ( c == gob )
            {
                return index;
            }
            index++;
        }
        return null;
    }

    public String toString()
    {
        final String payload = isGobs()
                ? format( "%s", getGobs() )
                : isText()
                        ? format( "%s", getText() )
                        : isNumber()
                                ? format( "%s", getNumber() )
                                : format( "%s, %s, %s", gobs, text, number );
        return format( "axis=[%s]: %s", axis, payload );
    }

    public Selection withAxis( Axis axis )
    {
        this.axis = axis;
        return this;
    }


    public Selection withResult( Selection result )
    {
        if ( result != null && ! result.isEmpty() )
        {
            withGobs( result.getGobs() );
            withText( result.getText() );
            withNumber( result.getNumber() );
            withBoolean( result.toBoolean() );
        }
        return this;
    }


    public Selection withBoolean( boolean b )
    {
        this.booleanValue = b;
        return this;
    }


    public Selection withGobs( List< ? extends Gob > newGobs )
    {
        if ( newGobs == null || newGobs.isEmpty() )
        {
        }
        else if ( getGobs() == null )
        {
            this.gobs = new LinkedHashSet<>( newGobs );
        }
        else
        {
            this.gobs.addAll( newGobs );
        }
        return this;
    }

    public Selection withGob( Gob newGob )
    {
        if ( newGob == null )
        {
        }
        else if ( getGobs() == null )
        {
            this.gobs = new LinkedHashSet<>();
            this.gobs.add( newGob );
        }
        else
        {
            this.gobs.add( newGob );
        }
        return this;
    }

    public Selection withText( String text )
    {
        this.text = text;
        return this;
    }

    public Selection withNumber( Number number )
    {
        this.number = number;
        return this;
    }


    public Number toNumber()
    {
        Number n = number != null
                ? number
                : text != null
                        ? Double.valueOf( text )
                        : null;

        if ( n == null )
        {
            throw new IllegalArgumentException( "Cannot coerce gobs to number: " + gobs );
        }

        return n;
    }

    public String toText()
    {
        return text != null
                ? text
                : number != null
                        ? number.toString()
                        : gobs != null
                                ? gobs.toString()
                                : "";
    }

    public boolean toBoolean()
    {
        return ( isBoolean() && booleanValue )
               || ( isGobs() && ! getGobs().isEmpty() )
               || ( isText() && ! getText().isEmpty() )
               || isNumber();
    }


    public void clear()
    {
        this.gobs = null;
        this.text = null;
        this.number = null;
        this.booleanValue = null;
        this.axis = Axis.CHILD;
    }


    public boolean equals( Selection result )
    {
        if ( isText() )
        {
            return ( result.isText() && getText().equals( result.getText() ) );
        }
        else if ( isNumber() )
        {
            return result.isNumber() && getNumber().equals( result.getNumber() );
        }
        else if ( isGobs() )
        {
            return result.isGobs() && getGobs().equals( result.getGobs() );
        }

        return false;
    }


    public Selection addResult( Selection result )
    {
        if ( result != null && ! result.isEmpty() )
        {
            if ( ! isNumber() )
            {
                clear();
                withNumber( result.getNumber() );
            }
            else
            {
                Number number1 = getNumber();
                Number number2 = result.getNumber();

                clear();

                if ( number1 instanceof Integer )
                {
                    withNumber( number1.intValue() + number2.intValue() );
                }
                else
                {
                    withNumber( number1.doubleValue() + number2.doubleValue() );
                }
            }
        }
        return this;
    }

    public Selection subtractResult( Selection result )
    {
        if ( result != null && ! result.isEmpty() )
        {
            if ( ! isNumber() )
            {
                clear();
                withNumber( result.getNumber() );
            }
            else
            {
                Number number1 = getNumber();
                Number number2 = result.getNumber();

                clear();

                if ( number1 instanceof Integer )
                {
                    withNumber( number1.intValue() - number2.intValue() );
                }
                else
                {
                    withNumber( number1.doubleValue() - number2.doubleValue() );
                }
            }
        }
        return this;
    }


    public void multiplyResult( Selection result )
    {
        if ( result != null && ! result.isEmpty() )
        {
            if ( ! isNumber() )
            {
                clear();
                withNumber( result.getNumber() );
            }
            else
            {
                Number number1 = getNumber();
                Number number2 = result.getNumber();

                clear();

                if ( number1 instanceof Integer )
                {
                    withNumber( number1.intValue() * number2.intValue() );
                }
                else
                {
                    withNumber( number1.doubleValue() * number2.doubleValue() );
                }
            }
        }
    }

    public void divideResult( Selection result )
    {
        if ( result != null && ! result.isEmpty() )
        {
            if ( ! isNumber() )
            {
                clear();
                withNumber( result.getNumber() );
            }
            else
            {
                Number number1 = getNumber();
                Number number2 = result.getNumber();

                clear();

                if ( number1 instanceof Integer )
                {
                    withNumber( number1.intValue() / number2.intValue() );
                }
                else
                {
                    withNumber( number1.doubleValue() / number2.doubleValue() );
                }
            }
        }
    }

    public void modResult( Selection result )
    {
        if ( result != null && ! result.isEmpty() )
        {
            if ( ! isNumber() )
            {
                clear();
                withNumber( result.getNumber() );
            }
            else
            {
                Number number1 = getNumber();
                Number number2 = result.getNumber();

                clear();

                if ( number1 instanceof Integer )
                {
                    withNumber( number1.intValue() % number2.intValue() );
                }
                else
                {
                    withNumber( number1.doubleValue() % number2.doubleValue() );
                }
            }
        }
    }
}
