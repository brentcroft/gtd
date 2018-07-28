package com.brentcroft.gtd.inspector.model;

/**
 * Created by Alaric on 14/11/2016.
 */
public interface GuiEventListener
{
    /**
     * Notify as text (since DOM is not shareable!)
     *
     * @param notification
     */
    public void receive( String notification );
}
