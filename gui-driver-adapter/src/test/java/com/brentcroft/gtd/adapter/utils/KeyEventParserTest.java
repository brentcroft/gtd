package com.brentcroft.gtd.adapter.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.awt.event.KeyEvent;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javafx.scene.input.KeyCode;

/**
 * Created by Alaric on 28/12/2016.
 */
public class KeyEventParserTest
{

	static KeyEventParser keyEventParser = new KeyEventParser();

	@BeforeClass
	public static void setUp()
	{
		// System.out.println( keyEventParser );
	}

	private void validateKeys( String keys, char[][] expected )
	{
		List< List< Integer > > actual = keyEventParser.parse( keys );

		System.out.println( "KeyEventParser: " + keys + " -> " + actual );

		assertArrayEquals( expected, KeyEventParser.toCharArray( actual ) );
	}

	@Test
	public void parse_hello() throws Exception
	{
		String keys = "hello";

		char[][] expected = {
				{ 'H' },
				{ 'E' },
				{ 'L' },
				{ 'L' },
				{ 'O' }
		};

		validateKeys( keys, expected );
	}

	@Test
	public void parse_HELLO() throws Exception
	{
		String keys = "HELLO";

		char[][] expected = {
				{ KeyEvent.VK_SHIFT, 'H' },
				{ KeyEvent.VK_SHIFT, 'E' },
				{ KeyEvent.VK_SHIFT, 'L' },
				{ KeyEvent.VK_SHIFT, 'L' },
				{ KeyEvent.VK_SHIFT, 'O' }
		};

		validateKeys( keys, expected );
	}

	@Test
	public void parse_Alphabet() throws Exception
	{
		String keys = "abcdABCD";

		char[][] expected = {
				{ 'A' },
				{ 'B' },
				{ 'C' },
				{ 'D' },
				{ KeyEvent.VK_SHIFT, 'A' },
				{ KeyEvent.VK_SHIFT, 'B' },
				{ KeyEvent.VK_SHIFT, 'C' },
				{ KeyEvent.VK_SHIFT, 'D' }
		};

		validateKeys( keys, expected );
	}

	@Test
	public void parse_LowerCaseSymbols() throws Exception
	{
		String LOWER_CASE_SYMBOLS = "1234567890[]-=;'#,./\\";

		char[][] expected = new char[ LOWER_CASE_SYMBOLS.length() ][];

		for ( int i = 0, n = LOWER_CASE_SYMBOLS.length(); i < n; i++ )
		{
			expected[ i ] = new char[] { LOWER_CASE_SYMBOLS.charAt( i ) };
		}

		validateKeys( LOWER_CASE_SYMBOLS, expected );
	}

	@Test
	@Ignore( "Exclamation mark is not seen as upper case." )
	public void parse_UpperCaseSymbols() throws Exception
	{
		String keys = "!\"£$%^&*()_+\\{\\}:@~<>?|";

		char[][] expected = {
				{ KeyEvent.VK_SHIFT, '1' },
				{ KeyEvent.VK_SHIFT, '2' },
				{ KeyEvent.VK_SHIFT, '3' },
				{ KeyEvent.VK_SHIFT, '4' },
				{ KeyEvent.VK_SHIFT, '5' },
				{ KeyEvent.VK_SHIFT, '6' },
				{ KeyEvent.VK_SHIFT, '7' },
				{ KeyEvent.VK_SHIFT, '8' },
				{ KeyEvent.VK_SHIFT, '9' },
				{ KeyEvent.VK_SHIFT, '0' },
				{ KeyEvent.VK_SHIFT, '-' },
				{ KeyEvent.VK_SHIFT, '=' },

				// depends on handling escaped curly brackets
				{ KeyEvent.VK_SHIFT, '[' },
				{ KeyEvent.VK_SHIFT, ']' },

				{ KeyEvent.VK_SHIFT, ';' },
				{ KeyEvent.VK_SHIFT, '\'' },
				{ KeyEvent.VK_SHIFT, '#' },
				{ KeyEvent.VK_SHIFT, ',' },
				{ KeyEvent.VK_SHIFT, '.' },
				{ KeyEvent.VK_SHIFT, '/' },
				{ KeyEvent.VK_SHIFT, '\\' }
		};

		validateKeys( keys, expected );
	}

	@Test
	public void parse_EscapeCurlyBrackets() throws Exception
	{
		String keys = "a{}c";

		char[][] expected = {
				{ 'A' },
				{},
				{ 'C' }
		};

		validateKeys( keys, expected );
	}

	@Test
	public void parse_Alt_F4() throws Exception
	{
		String keys = "abc{Alt+F4}abc{Alt+F4}";

		char[][] expected = {
				{ 'A' },
				{ 'B' },
				{ 'C' },
				{ KeyEvent.VK_ALT, KeyEvent.VK_F4 },
				{ 'A' },
				{ 'B' },
				{ 'C' },
				{ KeyEvent.VK_ALT, KeyEvent.VK_F4 }
		};

		validateKeys( keys, expected );
	}

	@Test
	public void parse_Alt_2_F4() throws Exception
	{
		String keys = "abc{Alt-F4}abc{Alt-F4}";

		char[][] expected = {
				{ 'A' },
				{ 'B' },
				{ 'C' },
				{ KeyEvent.VK_ALT, KeyEvent.VK_F4 },
				{ 'A' },
				{ 'B' },
				{ 'C' },
				{ KeyEvent.VK_ALT, KeyEvent.VK_F4 }
		};

		validateKeys( keys, expected );
	}

	@Test
	public void parse_Ctrl_Alt_F4() throws Exception
	{
		String keys = "abc{Ctrl+Alt-F4}abc{Ctrl-Alt+F4}";

		char[][] expected = {
				{ 'A' },
				{ 'B' },
				{ 'C' },
				{ KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_F4 },
				{ 'A' },
				{ 'B' },
				{ 'C' },
				{ KeyEvent.VK_CONTROL, KeyEvent.VK_ALT, KeyEvent.VK_F4 }
		};

		validateKeys( keys, expected );
	}

	@Test
	public void parse_Ctrl_Shift_1() throws Exception
	{
		String keys = "a9c{Ctrl-Shift-1}a7c{Ctrl+Shift+1}";

		char[][] expected = {
				{ 'A' },
				{ '9' },
				{ 'C' },
				{ KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_1 },
				{ 'A' },
				{ '7' },
				{ 'C' },
				{ KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_1 }
		};

		validateKeys( keys, expected );
	}

	@Test
	public void parse_fdgsfgsafdgadfgafdgadfg() throws Exception
	{
		String keys = "fdgsfgsafdgadfgafdgadfg";

		List< List< Integer > > actual = keyEventParser.parse( keys );

		assertTrue( actual != null );
	}

	@Test
	public void parseTokens_fdgsfgsafdgadfgafdgadfg() throws Exception
	{
		String keys = "fdgsfgsafdgadfgafdgadfg";

		List< List< KeyCode > > actual = keyEventParser.parseTokens( keys );

		assertTrue( actual != null );

		System.out.println( actual );
	}

	@Test
	public void parseTokens_Alt_F4() throws Exception
	{
		String keys = "{Alt-F4}";

		List< List< KeyCode > > actual = keyEventParser.parseTokens( keys );

		assertTrue( actual != null );

		System.out.println( actual );
	}
}