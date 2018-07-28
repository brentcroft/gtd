package com.brentcroft.gtd.events;

import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.util.function.Consumer;
import org.apache.log4j.Logger;

import static java.lang.String.format;

/**
 * Created by Alaric on 25/05/2017.
 */
public class AWTEventUtils extends AbstractEventUtils< AWTEvent, AWTEventListener >
{
    private final static Logger logger = Logger.getLogger( AWTEventUtils.class );

    public boolean canIgnore( AWTEvent event )
    {
        return false;
    }

    public String getParams( AWTEvent awtEvent )
    {
        //return replaceAnyNonPrintingCharacters( awtEvent.paramString() );
        return awtEvent.paramString();
    }


    public AWTEventListener getHandler( final Consumer< AWTEvent > consumer )
    {
        return awtEvent ->
        {
            try
            {
                consumer.accept( awtEvent );
            }
            catch ( Exception ex )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.warn( format( "Ignored AWTEvent (exception): [%s]; %s", awtEvent.getID(), ex ) );
                }
            }
        };
    }
}
