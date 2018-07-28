package com.brentcroft.gtd.events;

import java.util.function.Consumer;

/**
 * Created by Alaric on 26/05/2017.
 */
public abstract class AbstractEventUtils< E, H >
{
    public abstract boolean canIgnore( E event );

    public abstract String getParams( E event );

    public abstract H getHandler( Consumer< E > processor );



    protected String removeAnyNonPrintingCharacters( String text )
    {
        return text == null
                ? null
                : text.replaceAll( "[\u0000-\u0019]*", "" );
    }

    protected String replaceAnyNonPrintingCharacters( String text)
    {
        return replaceAnyNonPrintingCharacters( text, "\ufffd" );
    }

    protected String replaceAnyNonPrintingCharacters( String text, String replacement )
    {
        return text == null
                ? null
                : text.replaceAll( "[\u0000-\u0019]*", replacement );
    }
}
