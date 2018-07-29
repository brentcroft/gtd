package com.brentcroft.gtd.driver.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Alaric on 22/12/2016.
 */
public class CanonicalPath
{
	private static final Pattern PATH_HASH_PREFIX = Pattern.compile( "^(?:hash=(\\d+);)?(.+)?$" );

	private final Integer hash;
	private final String xPath;

	private CanonicalPath( String hashText, String xPath )
	{
		this.hash = (hashText == null)
				? null
				: Integer.valueOf( hashText );
		this.xPath = (xPath == null)
				? ""
				: xPath;
		;
	}

	@Override
	public String toString()
	{
		return (hash == null ? "" : "hash=" + hash + ";") + xPath;
	}

	public boolean hasHash()
	{
		return hash != null;
	}

	public Integer getHash()
	{
		return hash;
	}

	public String getXPath()
	{
		return xPath;
	}

	public static CanonicalPath newCanonicalPath( String path )
	{
		if ( path == null )
		{
			throw new RuntimeException( "path cannot be null!" );
		}

		Matcher m = PATH_HASH_PREFIX.matcher( path );

		if ( m.matches() )
		{
			return new CanonicalPath(
					m.group( 1 ),
					m.group( 2 )
			);
		}
		else
		{
			return new CanonicalPath(
					null,
					path
			);
		}
	}
}
