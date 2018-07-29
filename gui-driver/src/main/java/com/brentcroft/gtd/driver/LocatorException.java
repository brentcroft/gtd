package com.brentcroft.gtd.driver;

import java.io.Serializable;

/**
 * When an object cannot be found.
 * <p/>
 *
 * This exception may be returned to a remote caller.
 *
 * Created by adobson on 06/06/2016.
 */
public class LocatorException extends RuntimeException implements Serializable
{
	private static final long serialVersionUID = 8953501596012804168L;

	public LocatorException( String message )
	{
		super( message );
	}

	public LocatorException( String message, Throwable cause )
	{
		super( message, cause );
	}
}
