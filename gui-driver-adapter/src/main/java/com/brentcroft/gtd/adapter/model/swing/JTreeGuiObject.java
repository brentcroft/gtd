package com.brentcroft.gtd.adapter.model.swing;

import static com.brentcroft.gtd.adapter.model.DefaultGuiObject.Converter.maybeConvertValue;
import static com.brentcroft.gtd.adapter.model.DefaultGuiObject.Converter.maybeGetValueType;
import static com.brentcroft.gtd.adapter.model.swing.JComboBoxGuiObject.MODEL_TAG;
import static com.brentcroft.util.XmlUtils.maybeSetElementAttribute;
import static java.lang.String.format;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.RobotUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JTreeGuiObject< T extends JTree > extends JComponentGuiObject< T > implements GuiObject.Tree
{
	public JTreeGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		// the last action in the list is the prime action
		addTreeAction( element, options );

		buildTreeModel( getObject(), element, options );
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

		// TODO: using robot click
		// not on EDT - maybe

		TreePath treePath = getTreePath( nodePath );

		JTree tree = getObject();

		tree.setSelectionPath( treePath );

		tree.scrollPathToVisible( treePath );

		Rectangle rect = tree.getPathBounds( treePath );

		if ( rect == null )
		{
			throw new RuntimeException(
					format( "JTree path bounds [%s] produced an empty Rectangle for JTree [%s].", path, tree ) );
		}

		Point point = new Point( rect.x + (rect.x / 2), rect.y + (rect.height / 2) );

		SwingUtilities.convertPointToScreen( point, tree );

		RobotUtils.awtRobotClickOnPoint( new int[] { point.x, point.y } );
	}

	protected String getNodePath( TreePath treePath )
	{
		return Stream.of( treePath.getPath() )
				.map( p -> ( TreeNode ) p )
				// one-based indexes
				.map( p -> "" + (1 + (p.getParent() == null ? 0 : p.getParent().getIndex( p ))) )
				.collect( Collectors.joining( ":" ) );
	}

	public TreePath getTreePath( int[] nodePath )
	{
		TreeModel model = getObject().getModel();

		Object[] treeNodes = new Object[ nodePath.length ];

		// i.e. nodePath[ 0 ] == 1
		Object node = model.getRoot();

		treeNodes[ 0 ] = node;

		for ( int i = 1, n = nodePath.length; i < n; i++ )
		{
			int numChildren = model.getChildCount( node );

			if ( nodePath[ i ] >= numChildren )
			{
				throw new RuntimeException(
						format( "Bad Tree Node path [%s], node was null at depth [%s].",
								intArrayToString( nodePath ), i ) );
			}

			// next node
			node = model.getChild( node, nodePath[ i ] );

			if ( node == null )
			{
				throw new RuntimeException(
						format( "Bad Tree Node path [%s], node was null at depth [%s].",
								intArrayToString( nodePath ), i ) );
			}

			treeNodes[ i ] = node;
		}

		return new TreePath( treeNodes );
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

	private void buildTreeModel( JTree tree, Element element, Map< String, Object > options )
	{
		Element modelElement = element.getOwnerDocument().createElement( MODEL_TAG );

		element.appendChild( modelElement );

		modelElement.setAttribute( "type", "tree" );

		TreeModel model = tree.getModel();

		if ( model == null )
		{
			return;
		}

		if ( tree.getSelectionPath() != null )
		{
			modelElement.setAttribute( "selected-path", getNodePath( tree.getSelectionPath() ) );
			modelElement.setAttribute( "selected-text", "" + tree.getSelectionPath().getLastPathComponent() );
		}

		if ( !GuiObject.isShallow( options ) )
		{
			Object root = model.getRoot();

			if ( root != null )
			{
				int maxDepth = DataLimit.MAX_TREE_DEPTH
						.getMin( Integer.MAX_VALUE, options );

				String nodePath = "1";
				parseTreeNode( model, root, modelElement, nodePath, maxDepth );
			}
		}
	}

	/**
	 * Recurses over all tree nodes, writing elements for each node as:
	 * <n path="1:2:5" text="fred"/>
	 *
	 * @param model
	 * @param treeNode
	 * @param parentElement
	 * @param nodePath
	 */
	protected void parseTreeNode( TreeModel model, Object treeNode, Element parentElement, String nodePath, int maxDepth )
	{
		if ( maxDepth < 1 )
		{
			return;
		}

		Element nodeElement = parentElement.getOwnerDocument().createElement( "n" );

		parentElement.appendChild( nodeElement );

		Object value = treeNode;

		maybeSetElementAttribute( nodeElement, "text", format( "[%s]%s", maybeGetValueType( value ), maybeConvertValue( value ) ) );

		maybeSetElementAttribute( nodeElement, "path", nodePath );

		if ( !model.isLeaf( treeNode ) && (maxDepth > 1) )
		{
			for ( int i = 0, n = model.getChildCount( treeNode ); i < n; i++ )
			{
				Object childNode = model.getChild( treeNode, i );

				parseTreeNode( model, childNode, nodeElement, nodePath + ":" + (i + 1), maxDepth - 1 );
			}
		}
	}

	@Override
	public String getPath( String path )
	{
		// TODO Auto-generated method stub
		return null;
	}
}
