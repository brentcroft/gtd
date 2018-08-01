package com.brentcroft.gtd.camera;

import java.util.function.Function;
import java.util.stream.Collectors;



public interface ObjectServiceInstall
{
	/**
	 * Test binding of service parent object (e.g. Snapshot, FXSnapshot and SwtSnapshot) 
	 * with correct adapter (e.g. SnapshotGuiObject, FXSnapshotGuiObject and SwtSnapshotGuiObject).
	 * 
	 * @throws Exception
	 */
	void installsSnapshotAdapter() throws Exception;
	
	/**
	 * Test bindings of all other service adapters.
	 * 
	 * @throws Exception
	 */
	void installsServiceAdapters() throws Exception;
	
	
	@SuppressWarnings( "unchecked" )
	default < T > Class< T > adapteeClass( CameraObjectService gos, T t )
	{
		return ( Class< T > ) gos.getManager().adapt( t, null ).getClass();
	}
	
	
	Function< CameraObjectManager, String > CODE_GENERATOR = com -> com
			.getAdapterMap()
			.entrySet()
			.stream()
			.map( entry -> String.join( "",
					"assertEquals( ",
					entry.getValue().getSimpleName(),
					".class, adapteeClass( gos, Mockito.mock( ",
					entry.getKey().getSimpleName(),
					".class ) ) );"
			) )
			.collect( Collectors.joining( "\n" ) );

}