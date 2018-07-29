package com.brentcroft.gtd.adapter.utils;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.scene.input.KeyCode;

/**
 * Created by Alaric on 28/12/2016.
 */
public class KeyEventParser
{
	private static Map< String, Integer > lookup = new LinkedHashMap<>();

	// stash the available names
	static
	{
		for ( int i = 0; i < 255; i++ )
		{
			String keyText = KeyEvent.getKeyText( i );

			if ( (keyText == null) || keyText.startsWith( "Unknown keyCode" ) )
			{
				continue;
			}

			lookup.put( keyText, i );
		}
	}

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder( "{" );

		for ( Map.Entry< String, Integer > entry : lookup.entrySet() )
		{
			b
					.append( "\n \"" )
					.append( entry.getKey() )
					.append( "\"=" )
					.append( entry.getValue() );
		}

		return b
				.append( "\n}" )
				.toString();
	}

	public List< List< Integer > > parse( String keys )
	{
		List< List< Integer > > keyCodesSequences = new ArrayList< List< Integer > >();

		int currentPos = 0;
		int nextPos = getNextTokenPosition( currentPos, keys );

		while ( (nextPos > -1) && (nextPos <= keys.length()) )
		{
			List< Integer > keyCodes = new ArrayList< Integer >();

			if ( (keys.charAt( currentPos ) == '{')
					&& (keys.charAt( nextPos ) == '}') )
			{
				String tokenSequence = keys
						.substring( currentPos + 1, nextPos )
						.trim();

				if ( (tokenSequence != null) && !tokenSequence.isEmpty() )
				{

					String[] tokens = tokenSequence
							.split( "\\+|\\-" );

					for ( String token : tokens )
					{
						Integer keyValue = lookup.get( token );

						if ( keyValue != null )
						{
							keyCodes.add( keyValue );
						}
						else
						{
							throw new RuntimeException( "Unexpected token: " + token );
						}
					}
				}

				keyCodesSequences.add( keyCodes );

				currentPos = nextPos + 1;

			}
			else
			{
				char c = keys.charAt( currentPos );

				if ( Character.isUpperCase( c ) )
				{
					keyCodes.add( KeyEvent.VK_SHIFT );
					keyCodes.add( ( int ) c );
				}
				else
				{
					keyCodes.add( ( int ) Character.toUpperCase( c ) );
				}

				keyCodesSequences.add( keyCodes );

				currentPos = nextPos;
			}

			nextPos = getNextTokenPosition( currentPos, keys );
		}

		return keyCodesSequences;
	}

	public List< List< KeyCode > > parseTokens( String keys )
	{
		List< List< KeyCode > > keyCodesSequences = new ArrayList<>();

		int currentPos = 0;
		int nextPos = getNextTokenPosition( currentPos, keys );

		while ( (nextPos > -1) && (nextPos <= keys.length()) )
		{
			List< KeyCode > keyTokens = new ArrayList<>();

			if ( (keys.charAt( currentPos ) == '{')
					&& (keys.charAt( nextPos ) == '}') )
			{
				String tokenSequence = keys
						.substring( currentPos + 1, nextPos )
						.trim();

				if ( (tokenSequence != null) && !tokenSequence.isEmpty() )
				{

					String[] tokens = tokenSequence.split( "\\+|\\-" );

					// key code names are all case sensitive and mixed case
					for ( String token : tokens )
					{
						keyTokens.add( KeyCode.getKeyCode( token ) );
					}
				}

				keyCodesSequences.add( keyTokens );

				currentPos = nextPos + 1;

			}
			else
			{
				String key = keys.substring( currentPos, currentPos + 1 );

				if ( Character.isUpperCase( key.charAt( 0 ) ) )
				{
					keyTokens.add( KeyCode.SHIFT );
				}

				keyTokens.add( KeyCode.getKeyCode( key.toUpperCase() ) );

				keyCodesSequences.add( keyTokens );

				currentPos = nextPos;
			}

			nextPos = getNextTokenPosition( currentPos, keys );
		}

		return keyCodesSequences;
	}

	private int getNextTokenPosition( int currentPos, String keys )
	{
		int p = keys.indexOf( '{', currentPos );

		if ( p == currentPos )
		{
			int q = keys.indexOf( '}', p + 1 );

			// allowing empty braces
			if ( q > p )
			{
				return q;
			}
		}

		return currentPos + 1;
	}

	public static int[][] toArray( List< List< Integer > > codeSequences )
	{
		int[][] kcSeq = new int[ codeSequences.size() ][];

		int index = 0;
		for ( List< Integer > keyCodes : codeSequences )
		{
			int[] kc = new int[ keyCodes.size() ];
			int ri = 0;
			for ( int keyCode : keyCodes )
			{
				kc[ ri++ ] = keyCode;
			}
			kcSeq[ index++ ] = kc;
		}

		return kcSeq;
	}

	public static char[][] toCharArray( List< List< Integer > > codeSequences )
	{
		char[][] kcSeq = new char[ codeSequences.size() ][];

		int index = 0;
		for ( List< Integer > keyCodes : codeSequences )
		{
			char[] kc = new char[ keyCodes.size() ];
			int ri = 0;
			for ( int keyCode : keyCodes )
			{
				kc[ ri++ ] = ( char ) keyCode;
			}
			kcSeq[ index++ ] = kc;
		}

		return kcSeq;
	}

}
