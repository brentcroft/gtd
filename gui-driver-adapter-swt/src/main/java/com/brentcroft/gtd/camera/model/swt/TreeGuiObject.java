package com.brentcroft.gtd.camera.model.swt;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.swt.widgets.TreeItem;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class TreeGuiObject< T extends org.eclipse.swt.widgets.Tree > extends CompositeGuiObject< T >
		implements GuiObject.Tree
{
	public TreeGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		appendTreeModel(
				element,
				options,
				onDisplayThread( getObject(), go -> go.getItems() ),
				item -> onDisplayThread( ( TreeItem ) item, go -> go.getItems() ),
				item -> onDisplayThread( ( TreeItem ) item, go -> go.getText() )
		);

		addTreeAction( element, options );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public List< AttrSpec< T > > loadAttrSpec()
	{
		if ( attrSpec == null )
		{
			attrSpec = super.loadAttrSpec();
			attrSpec.addAll( Arrays.asList( ( AttrSpec< T >[] ) Attr.values() ) );
		}

		return attrSpec;
	}

	static enum Attr implements AttrSpec< org.eclipse.swt.widgets.Tree >
	{
		SIZE( "size", go -> go.getItemCount() ),
		// SELECTED_INDEX( "selected-index", go -> go.get )

		;

		final String n;
		final Function< org.eclipse.swt.widgets.Tree, Object > f;

		Attr( String name, Function< org.eclipse.swt.widgets.Tree, Object > f )
		{
			this.n = name;
			this.f = f;
		}

		@Override
		public String getName()
		{
			return n;
		}

		@Override
		public String getAttribute( org.eclipse.swt.widgets.Tree go )
		{
			return onSwtDisplayThreadAsText( go, f );
		}
	}

	@Override
	public String getPath( String path )
	{
		String[] nodePathKeys = path.split( "\\s*:\\s*" );
		int[] nodePath = new int[ nodePathKeys.length ];

		for ( int i = 0, n = nodePath.length; i < n; i++ )
		{
			// and convert to zero based
			nodePath[ i ] = Integer.valueOf( nodePathKeys[ i ] );
		}

		TreeItem[] treeItems = onDisplayThread( getObject(), t -> t.getItems() );
		TreeItem treeItem = null;

		for ( int p : nodePath )
		{
			if ( treeItems.length < p )
			{
				throw new RuntimeException( format( "No TreeItem at [%s]", p ) );
			}

			treeItem = treeItems[ p ];

			if ( treeItem == null )
			{
				throw new RuntimeException( format( "Null TreeItem at [%s]", p ) );
			}

			treeItems = onDisplayThread( treeItem, t -> t.getItems() );
		}

		return onDisplayThread( treeItem, t -> t.getText() );
	}

	@Override
	public void selectPath( String path )
	{
		String[] nodePathKeys = path.split( "\\s*:\\s*" );
		int[] nodePath = new int[ nodePathKeys.length ];

		for ( int i = 0, n = nodePath.length; i < n; i++ )
		{
			// and convert to zero based
			nodePath[ i ] = Integer.valueOf( nodePathKeys[ i ] ) - 1;
		}

		TreeItem[] treeItems = onDisplayThread( getObject(), t -> t.getItems() );

		for ( int p : nodePath )
		{
			if ( treeItems.length < (p + 1) )
			{
				throw new RuntimeException( format( "No TreeItem at [%s]", p ) );
			}

			TreeItem treeItem = treeItems[ p ];

			if ( treeItem == null )
			{
				throw new RuntimeException( format( "Null TreeItem at [%s]", p ) );
			}

			onDisplayThread( getObject(), t -> {
				getObject().setSelection( treeItem );
				return null;
			} );

			treeItems = onDisplayThread( treeItem, t -> t.getItems() );
		}
	}

}
