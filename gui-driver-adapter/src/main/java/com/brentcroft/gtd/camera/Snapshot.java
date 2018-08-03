package com.brentcroft.gtd.camera;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A synthetic top-level parent for all Swing gui components.
 *
 * Created by Alaric on 14/07/2017.
 */
public class Snapshot
{
	@SuppressWarnings( "unchecked" )
	public < T extends Object > List< T > getChildren()
	{
		return new ArrayList<>( ( List< T > ) Arrays.asList( Window.getWindows() ) );
	}
}
