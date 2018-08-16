package com.brentcroft.gtd.driver.client;

import static com.brentcroft.util.DateUtils.secondsToMillis;
import static java.lang.String.format;

import java.util.Map;
import java.util.Properties;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.brentcroft.gtd.driver.utils.CanonicalPath;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.Waiter8;
import com.brentcroft.util.XPathUtils;
import com.brentcroft.util.XmlUtils;

/**
 * Created by adobson on 05/05/2016.
 */
public class GuiLocalDriver extends AbstractGuiLocalDriver implements GuiDriver
{

	public Object shutdown( int status )
	{
		try
		{
			cleanup();

			return remote()
					.shutdown( status );
		}
		catch ( Exception e )
		{
			return e;
		}
	}

	/**
	 * Apply the path to the GUI serialization to obtain and return a text result.
	 *
	 * @param path
	 *            the xpath to be evaluated on the GUI serialization.
	 * @return the text result of the xpath evaluation.
	 */
	@Override
	public String getResultText( String path )
	{
		CanonicalPath canonicalPath = CanonicalPath.newCanonicalPath( path );

		String xmlText = remote().getSnapshotXmlText();

		relax();

		try
		{
			return ( String ) XPathUtils
					.getCompiledPath(
							canonicalPath
									.getXPath() )
					.evaluate(
							XmlUtils
									.parse( xmlText ),
							XPathConstants.STRING );
		}
		catch ( XPathExpressionException e )
		{
			throw new RuntimeException( format( "Failed to evaluate path expression [%s]", path ), e );
		}
	}

	/**
	 * Get a snapshot the object at the given path and then Apply the path to the
	 * GUI serialization to obtain and return a text result.
	 *
	 * @param path
	 *            the xpath to be evaluated on the GUI serialization.
	 * @return the text result of the xpath evaluation.
	 */
	public String getComponentResultText( String path, String resultPath, Map< String, Object > options )
	{
		String xmlText = remote().getSnapshotXmlText( path, options );

		relax();

		try
		{
			Document document = XmlUtils.parse( xmlText );

			XmlUtils.removeTrimmedEmptyTextNodes( document );

			Node node = document.getDocumentElement();

			if ( node == null || node.getNodeType() != Node.ELEMENT_NODE )
			{
				return null;
			}

			return ( String ) XPathUtils
					.getCompiledPath( resultPath )
					.evaluate( node, XPathConstants.STRING );
		}
		catch ( XPathExpressionException e )
		{
			throw new RuntimeException(
					format(
							"Failed to process xpath [%s] at location [%s] with options %s.",
							resultPath,
							path,
							options ),
					e );
		}
	}

	/**
	 * Apply the path to the GUI serialization to obtain and return a text result.
	 *
	 * @param path
	 *            the xpath to be evaluated on the GUI serialization.
	 * @return the text result of the xpath evaluation.
	 */
	public String getComponentResultText( String path, String resultPath )
	{
		Map< String, Object > options = DataLimit.getMaxDataLimitsOptions();

		return getComponentResultText( path, resultPath, options );
	}

	/**
	 * Apply the path to the GUI serialization to obtain and return a boolean
	 * result.
	 *
	 * @param path
	 *            the xpath to be evaluated on the GUI serialization.
	 * @return the text result of the xpath evaluation.
	 */
	public boolean getComponentResult( String path, String booleanPath )
	{
		Map< String, Object > options = DataLimit.getMaxDataLimitsOptions();

		String xmlText = remote().getSnapshotXmlText( path, options );

		relax();

		try
		{
			Node node = XmlUtils.parse( xmlText );

			return ( boolean ) XPathUtils
					.getCompiledPath( booleanPath )
					.evaluate(
							node,
							XPathConstants.BOOLEAN );
		}
		catch ( XPathExpressionException e )
		{
			throw new RuntimeException(
					format(
							"Failed to process xpath [%s] at location [%s].",
							booleanPath,
							path ),
					e );
		}
	}

	@Override
	public void delay( long millis )
	{
		Waiter8.delay( millis );
	}

	@Override
	public void delaySeconds( double seconds )
	{
		delay( secondsToMillis( seconds ) );
	}

	public Object configure( String script )
	{
		try
		{
			return remote().configure( script );
		}
		finally
		{
			relax();
		}
	}

	public void setProperties( Properties properties )
	{
		remote().setProperties( properties );
	}

	@Override
	public void notifyAWTEvents( long notificationEventMask )
	{
		remote().notifyAWTEvents( notificationEventMask );
	}

	@Override
	public void notifyFXEvents( String eventType )
	{
		remote().notifyFXEvents( eventType );
	}

	@Override
	public void notifyDOMEvents( String eventTypes )
	{
		remote().notifyDOMEvents( eventTypes );
	}

	@Override
	public void notifySnapshotEventDelay( long delay )
	{
		remote().notifySnapshotEventDelay( delay );
	}

	@Override
	public void hashCache( int level )
	{
		remote().hashCache( level );
	}

	@Override
	public void gc()
	{
		try
		{
			remote().gc();
		}
		finally
		{
			relax();
		}
	}

	@Override
	public void logNotifications( int show )
	{
		remote().logNotifications( show );
	}

	/**
	 * Return a string representation of the addressable Gui components (i.e. an XML
	 * file).
	 *
	 * @return a string representation of the addressable Gui components
	 */
	@Override
	public String getSnapshotXmlText()
	{
		try
		{
			return remote().getSnapshotXmlText();
		}
		finally
		{
			relax();
		}
	}

	@Override
	public String getSnapshotXmlText( Map< String, Object > options )
	{
		try
		{
			return remote().getSnapshotXmlText( options );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public String getSnapshotXmlText( String path, Map< String, Object > options )
	{
		try
		{
			// TODO: options can't be null, until fix bug in harness implementation
			return remote()
					.getSnapshotXmlText(
							path,
							options == null
									? DataLimit.getMaxDataLimitsOptions()
									: options );
		}
		finally
		{
			relax();
		}
	}

	/**
	 * Determines whether an element exists at a given address (i.e. path).
	 *
	 * @param path
	 *            the address to check
	 * @return true if an element exists at the given path otherwise false.
	 */
	public boolean exists( final String path )
	{
		return exists( path, defaultTimeoutSeconds );
	}

	@Override
	public boolean exists( String path, double timeout )
	{
		return exists( path, timeout, defaultPollDelaySeconds );
	}

	@Override
	public boolean exists( String path, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			return remote()
					.exists(
							path,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public boolean notExists( String path )
	{
		return notExists( path, defaultTimeoutSeconds );
	}

	@Override
	public boolean notExists( String path, double timeoutSeconds )
	{
		return notExists( path, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public boolean notExists( String path, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			return remote()
					.notExists(
							path,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	public boolean waitFor( String path, final String booleanPath )
	{
		return waitFor( path, booleanPath, defaultTimeoutSeconds );
	}

	public boolean waitFor( final String path, final String booleanPath, double timeoutSeconds )
	{
		return waitFor( path, booleanPath, timeoutSeconds, defaultPollDelaySeconds );
	}

	public boolean waitFor( final String path, final String booleanPath, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			new Waiter8()
					.withDelayMillis( secondsToMillis( pollIntervalSeconds ) )
					.withTimeoutMillis( secondsToMillis( timeoutSeconds ) )
					.until( () -> getComponentResult( path, booleanPath ) );

			return true;
		}
		finally
		{
			relax();
		}
	}

	/**
	 * Click on the component at the specified path.
	 *
	 * @param path
	 *            the address of the component to be clicked.
	 */
	@Override
	public void click( String path, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.click(
							path,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public void click( String path, double timeoutSeconds )
	{
		click( path, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void click( String path )
	{
		click( path, defaultTimeoutSeconds );
	}

	/**
	 * Double click on the component at the specified path.
	 *
	 * @param path
	 *            the address of the component to be double clicked.
	 */
	@Override
	public void robotDoubleClick( String path, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.robotDoubleClick(
							path,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}

	}

	@Override
	public void robotDoubleClick( String path, double timeoutSeconds )
	{
		robotDoubleClick( path, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void robotDoubleClickPoint( String path, int x, int y )
	{
		robotDoubleClickPoint( path, x, y, defaultTimeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void robotDoubleClickPoint( String path, int x, int y, double timeoutSeconds )
	{
		robotDoubleClickPoint( path, x, y, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void robotDoubleClick( String path )
	{
		robotDoubleClick( path, defaultTimeoutSeconds );
	}

	/**
	 * Click on the component at the specified path by invoking a Robot.
	 *
	 * @param path
	 *            the address of the component to be clicked.
	 */
	@Override
	public void robotClick( String path, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.robotClick(
							path,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public void robotClick( String path, double timeoutSeconds )
	{
		robotClick( path, timeoutSeconds, defaultPollDelaySeconds );

	}

	@Override
	public void robotClickPoint( String path, int x, int y )
	{
		robotClickPoint( path, x, y, defaultTimeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void robotClickPoint( String path, int x, int y, double timeoutSeconds )
	{
		robotClickPoint( path, x, y, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void robotClick( String path )
	{
		robotClick( path, defaultTimeoutSeconds );

	}

	@Override
	public void robotClickPoint( String path, int x, int y, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.robotClickPoint( path, x, y, timeoutSeconds, pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public void robotDoubleClickPoint( String path, int x, int y, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.robotDoubleClickPoint( path, x, y, timeoutSeconds, pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	/**
	 * Send keys to the component at the specified path by invoking a Robot.
	 *
	 * @param path
	 *            the address of the component to receive keys.
	 */
	@Override
	public void robotKeys( String path, String keys, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.robotKeys(
							path,
							keys,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public void robotKeysPoint( String path, String keys, int x, int y, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.robotKeysPoint(
							path,
							keys,
							x,
							y,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public void robotKeys( String path, String keys, double timeoutSeconds )
	{
		robotKeys( path, keys, timeoutSeconds, defaultPollDelaySeconds );

	}

	@Override
	public void robotKeysPoint( String path, String keys, int x, int y )
	{
		robotKeysPoint( path, keys, x, y, defaultTimeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void robotKeysPoint( String path, String keys, int x, int y, double timeoutSeconds )
	{
		robotKeysPoint( path, keys, x, y, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void robotKeys( String path, String keys )
	{
		robotKeys( path, keys, defaultTimeoutSeconds );
	}

	/**
	 * Executes a script on the component at the specified path.
	 * <p>
	 * The script must refer to the component as "component", e.g.
	 * "component.setComponentText('fred')"
	 *
	 * @param path
	 *            the address of the component to have a script executed.
	 */
	@Override
	public void execute( String path, String script, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.execute(
							path,
							script,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public void selectTableRow( String path, int row, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.selectTableRow(
							path,
							row,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public void selectTableRow( String path, int row, double timeoutSeconds )
	{
		selectTableRow( path, row, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void selectTableRow( String path, int row )
	{
		selectTableRow( path, row, defaultTimeoutSeconds );
	}

	@Override
	public void selectTableColumn( String path, int column, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.selectTableColumn(
							path,
							column,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public void selectTableColumn( String path, int column, double timeoutSeconds )
	{
		selectTableColumn( path, column, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void selectTableColumn( String path, int column )
	{
		selectTableColumn( path, column, defaultTimeoutSeconds );
	}

	@Override
	public void selectTableCell( String path, int row, int column, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.selectTableCell(
							path,
							row,
							column,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public void selectTableCell( String path, int row, int column, double timeoutSeconds )
	{
		selectTableCell( path, row, column, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void selectTableCell( String path, int row, int column )
	{
		selectTableCell( path, row, column, defaultTimeoutSeconds );
	}

	@Override
	public void selectTreeNode( String path, String treePath, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.selectTreeNode(
							path,
							treePath,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public void selectTreeNode( String path, String treePath, double timeoutSeconds )
	{
		selectTreeNode( path, treePath, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void selectTreeNode( String path, String treePath )
	{
		selectTreeNode( path, treePath, defaultTimeoutSeconds );
	}

	@Override
	public void setSelectedIndex( String path, int index, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.setSelectedIndex(
							path,
							index,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public void setSelectedIndex( String path, int index, double timeoutSeconds )
	{
		setSelectedIndex( path, index, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void setSelectedIndex( String path, int index )
	{
		setSelectedIndex( path, index, defaultTimeoutSeconds );
	}

	@Override
	public Integer getSelectedIndex( String path, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			return remote()
					.getSelectedIndex(
							path,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public Integer getSelectedIndex( String path, double timeoutSeconds )
	{
		return getSelectedIndex( path, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public Integer getSelectedIndex( String path )
	{
		return getSelectedIndex( path, defaultTimeoutSeconds );
	}

	@Override
	public Integer getItemCount( String path, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			return remote()
					.getItemCount(
							path,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public Integer getItemCount( String path, double timeoutSeconds )
	{
		return getItemCount( path, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public Integer getItemCount( String path )
	{
		return getItemCount( path, defaultTimeoutSeconds );
	}

	/**
	 * Obtain the text from the component at the address given by path (by calling
	 * <code>component.getComponentText()</code>).
	 *
	 * @param path
	 *            the address of the component to get text from.
	 * @return the text of the component at the given address.
	 */
	@Override
	public String getText( String path, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			return remote()
					.getText(
							path,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public String getText( String path, double timeoutSeconds )
	{
		return getText( path, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public String getText( String path )
	{
		return getText( path, defaultTimeoutSeconds );
	}

	/**
	 * Assign text to the component at the address given by path (by calling
	 * <code>component.setComponentText( String text )</code>).
	 *
	 * @param path
	 *            the address of the component to have text assigned.
	 * @param text
	 *            the new text to assign to the component.
	 */
	@Override
	public void setText( String path, String text, double timeoutSeconds, double pollIntervalSeconds )
	{
		try
		{
			remote()
					.setText(
							path,
							text,
							timeoutSeconds,
							pollIntervalSeconds );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public void setText( String path, String text, double timeoutSeconds )
	{
		setText( path, text, timeoutSeconds, defaultPollDelaySeconds );
	}

	@Override
	public void setText( String path, String text )
	{
		setText( path, text, defaultTimeoutSeconds );

	}

	@Override
	public boolean[] existsAll( double timeoutSeconds, double pollIntervalSeconds, String... paths )
	{
		try
		{
			return remote()
					.existsAll(
							timeoutSeconds,
							pollIntervalSeconds,
							paths );
		}
		finally
		{
			relax();
		}
	}

	@Override
	public boolean[] existsAll( double timeoutSeconds, String... paths )
	{
		return existsAll( timeoutSeconds, defaultPollDelaySeconds, paths );
	}

	@Override
	public boolean[] existsAll( String... paths )
	{
		return existsAll( defaultTimeoutSeconds, paths );
	}

	public boolean isConnected()
	{
		try
		{
			return "hello".equals( remote().echo( "hello" ) );
		}
		catch ( Exception e )
		{
			return false;
		}
	}

	@Override
	public Object echo( Object o )
	{
		// no relax on echo
		return remote().echo( o );
	}

	//
	// @Override
	// public GuiObject[] getGuiObjects( String... path )
	// {
	// return getGuiObjects( defaultTimeoutSeconds, path );
	// }
	//
	//
	// @Override
	// public GuiObject[] getGuiObjects( double timeout, String... path )
	// {
	// return getGuiObjects( timeout, defaultPollDelaySeconds, path );
	// }
	//
	//
	// @Override
	// public GuiObject[] getGuiObjects( double timeout, double pollInterval,
	// String... path )
	// {
	// try
	// {
	// return harness().getGuiObjects( timeout, pollInterval, path );
	// }
	// finally
	// {
	// relax();
	// }
	// }

}
