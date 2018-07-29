package com.brentcroft.gtd.utilities;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.brentcroft.gtd.driver.utils.CanonicalPath;

/**
 * Created by Alaric on 22/12/2016.
 */
public class CanonicalPathTest
{
	@Test( expected = RuntimeException.class )
	public void toString_Null() throws Exception
	{
		String path = null;

		assertEquals(
				path,
				CanonicalPath
						.newCanonicalPath( path )
						.toString() );
	}

	@Test
	public void toString_Empty() throws Exception
	{
		String path = "";

		assertEquals(
				path,
				CanonicalPath
						.newCanonicalPath( path )
						.toString() );
	}

	@Test
	public void toString_XPathOnly() throws Exception
	{
		String path = "//alfredo[ @xyz = 'jjhg' ]";

		assertEquals(
				path,
				CanonicalPath
						.newCanonicalPath( path )
						.toString() );
	}

	@Test
	public void toString_HashOnly() throws Exception
	{
		String path = "hash=1234567;";

		assertEquals(
				path,
				CanonicalPath
						.newCanonicalPath( path )
						.toString() );
	}

	@Test
	public void toString_HashAndXPath() throws Exception
	{
		String path = "hash=1234567;//alfredo[ @xyz = 'jjhg' ]";

		assertEquals(
				path,
				CanonicalPath
						.newCanonicalPath( path )
						.toString() );
	}

	@Test
	public void hasHash() throws Exception
	{

	}

	@Test
	public void getHash() throws Exception
	{
		assertEquals(
				null,
				CanonicalPath
						.newCanonicalPath( "//alfredo[ @xyz = 'jjhg' ]" )
						.getHash() );

		assertEquals(
				Integer.valueOf( 12345 ),
				CanonicalPath
						.newCanonicalPath( "hash=12345;//alfredo[ @xyz = 'jjhg' ]" )
						.getHash() );
	}

	@Test
	public void getXPath() throws Exception
	{
		String xpOnly = "//alfredo[ @xyz = 'jjhg' ]";
		String hAndXp = "hash=12345;//alfredo[ @xyz = 'jjhg' ]";

		assertEquals(
				xpOnly,
				CanonicalPath
						.newCanonicalPath( xpOnly )
						.getXPath() );

		assertEquals(
				xpOnly,
				CanonicalPath
						.newCanonicalPath( hAndXp )
						.getXPath() );
	}

}