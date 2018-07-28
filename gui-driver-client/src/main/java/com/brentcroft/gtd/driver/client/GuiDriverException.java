package com.brentcroft.gtd.driver.client;

public class GuiDriverException extends RuntimeException
{
    private static final long serialVersionUID = -1143407449750160362L;

    public GuiDriverException( String message )
    {
        super( message );
    }

    public GuiDriverException( String message, Throwable cause )
    {
        super( message, cause );
    }
}