package com.brentcroft.gtd.adapter.utils;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.brentcroft.gtd.driver.utils.HashCache;

/**
 * Created by Alaric on 14/07/2017.
 */
public class HashCacheTest
{
	class ExampleCachee
	{
		String name;

		ExampleCachee( String name )
		{
			this.name = name;
		}

		@Override
		public String toString()
		{
			return name;
		}
	}

	Map< Integer, ExampleCachee > externalReferences = new HashMap<>();

	@Before
	public void setUp() throws Exception
	{
		IntStream.range( 0, 100 )
				.forEach( i -> {
					ExampleCachee ec = new ExampleCachee( format( "ExampleCachee[%s]", i ) );

					externalReferences.put( ec.hashCode(), ec );
				} );
	}

	@After
	public void tearDown() throws Exception
	{
	}

	@Test
	public void cacheObject() throws Exception
	{
		HashCache< ExampleCachee > hc = new HashCacheImpl<>();

		externalReferences
				.forEach( ( ignored, value ) -> {

					// object gets cached
					hc.cacheObject( value );

				} );

		externalReferences
				.forEach( ( key, value ) -> {

					// its accessible
					assertTrue( hc.hasCachedObject( key ) );

					// and effective
					assertEquals( externalReferences.get( key ), hc.getCachedObject( key ) );

				} );

		new ArrayList< Integer >( externalReferences.keySet() )
				.forEach( key -> {

					// dereference
					externalReferences.put( key, null );

					// confirmed
					assertTrue( externalReferences.get( key ) == null );

				} );

		// garbage collect
		System.gc();

		// garbage collect
		externalReferences
				.forEach( ( key, value ) -> {

					// not accessible
					assertFalse( hc.hasCachedObject( key ) );

					// confirmed
					assertTrue( hc.getCachedObject( key ) == null );

				} );
	}

}