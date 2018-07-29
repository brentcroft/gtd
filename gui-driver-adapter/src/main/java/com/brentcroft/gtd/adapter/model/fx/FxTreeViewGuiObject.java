package com.brentcroft.gtd.adapter.model.fx;

import static java.lang.String.format;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxTreeViewGuiObject< S, T extends TreeView< S > > extends FxControlGuiObject< T > implements GuiObject.Tree
{
	public FxTreeViewGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public boolean hasChildren()
	{
		return false;
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		return null;
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		addTreeAction( element, options );
	}

	@Override
	public void selectPath( String path )
	{
		FXUtils.maybeInvokeNowOnFXThread( () -> selectTreeNode( getObject(), path ) );
	}

	public static < S > void selectTreeNode( final TreeView< S > tree, String treePathText )
	{
		String[] nodePathKeys = treePathText.split( "\\s*:\\s*" );

		int[] nodePath = new int[ nodePathKeys.length ];

		for ( int i = 0, n = nodePath.length; i < n; i++ )
		{
			// and convert to zero based
			nodePath[ i ] = Integer.valueOf( nodePathKeys[ i ] ) - 1;
		}

		final TreeItem< S > item = getTreeItem( tree.getRoot(), nodePath );

		if ( item != null )
		{
			FXUtils.maybeInvokeNowOnFXThread( () -> tree.getSelectionModel().select( item ) );
		}
	}

	@SuppressWarnings( "unchecked" )
	public static < S > TreeItem< S > getTreeItem( TreeItem< S > item, int[] nodePath )
	{
		Object[] treeNodes = new Object[ nodePath.length ];

		treeNodes[ 0 ] = item;

		for ( int i = 1, n = nodePath.length; i < n; i++ )
		{
			int numChildren = item.getChildren().size();

			if ( nodePath[ i ] >= numChildren )
			{
				throw new RuntimeException(
						format( "Bad Tree Node path [%s], node was null at depth [%s].",
								intArrayToString( nodePath ), i ) );
			}

			Object child = item.getChildren().get( i );

			if ( child == null )
			{
				throw new RuntimeException(
						format( "Bad Tree Node path [%s], node was null at depth [%s].",
								intArrayToString( nodePath ), i ) );
			}

			treeNodes[ i ] = child;
		}

		return ( TreeItem< S > ) treeNodes[ treeNodes.length - 1 ];
	}

	public static String intArrayToString( int[] p )
	{
		StringBuilder b = new StringBuilder();
		for ( int i : p )
		{
			if ( b.length() > 0 )
			{
				b.append( ":" );
			}
			b.append( p[ i ] );
		}
		return b.toString();
	}

	@Override
	public String getPath( String path )
	{
		String[] nodePathKeys = path.split( "\\s*:\\s*" );

		int[] nodePath = new int[ nodePathKeys.length ];

		for ( int i = 0, n = nodePath.length; i < n; i++ )
		{
			// and convert to zero based
			nodePath[ i ] = Integer.valueOf( nodePathKeys[ i ] ) - 1;
		}

		final TreeItem item = getTreeItem( getObject().getRoot(), nodePath );

		return "" + item.getValue();
	}
}
