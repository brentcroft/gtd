package com.brentcroft.gtd.camera;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.camera.model.swt.TabItemGuiObject;
import com.brentcroft.gtd.camera.model.swt.WidgetGuiObject;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.gtd.swt.SwtApplication;
import com.brentcroft.util.Waiter8;
import com.brentcroft.util.XPathUtils;
import com.brentcroft.util.XmlUtils;

public class SwtCameraTest
{
	SwtApplication app;
	SwtCamera camera;

	@Before
	public void setUp()
	{
		app = new SwtApplication();
		camera = new SwtCamera();

		Properties properties = new Properties();

		properties.setProperty( "camera.SWTWidget.widgetsToSpecialise", "*" );

		camera.setProperties( properties );

		new Thread( () -> app.run() ).start();

		new Waiter8()
				.withDelayMillis( 1 * 1000 )
				.withTimeoutMillis( 5 * 1000 )
				.until( () -> app.isStarted() );

		System.out.println( XmlUtils.serialize( camera.takeSnapshot() ) );
	}

	@After
	public void tearDown()
	{
		app.kill();

		new Waiter8()
				.withDelayMillis( 1 * 1000 )
				.withTimeoutMillis( 5 * 1000 )
				.until( () -> !app.isStarted() );
	}

	@Test
	public void testButtonExists()
	{
		GuiObject< ? > guiObject = camera.getGuiObject( 1, 5, SwtApplication.buttonXPath );

		assertNotNull( guiObject );

		// System.out.println( XmlUtils.serialize( camera.takeSnapshot(
		// guiObject.getObject(), null ) ) );

		assertEquals( SwtApplication.buttonName, guiObject.getAttribute( "text" ) );

	}

	@Test
	public void testButtonClick()
	{
		WidgetGuiObject< ? > guiObject = ( WidgetGuiObject< ? > ) camera.getGuiObject( 1, 5, SwtApplication.buttonXPath );

		assertNotNull( guiObject );

		Button button = ( Button ) guiObject.getObject();

		// System.out.println( XmlUtils.serialize( camera.takeSnapshot( button, null ) )
		// );

		final AtomicBoolean wasClicked = new AtomicBoolean( false );

		WidgetGuiObject.onDisplayThread( button, go -> {
			go.addSelectionListener( new SelectionAdapter()
			{

				@Override
				public void widgetSelected( SelectionEvent e )
				{
					System.out.println( "widgetSelected: " + e );
					wasClicked.set( true );
				}
			} );
			return null;
		} );

		// TODO:
		// since this activates shutdown on the target
		// it's problematic to investigate the SWT DOM
		// but we can still check the button was clicked
		guiObject.asClick().click();

		new Waiter8()
				.withTimeoutMillis( 100 )
				.onTimeout( timeout -> assertTrue( wasClicked.get() ) )
				.until( () -> wasClicked.get() );

		// new Waiter8()
		// .withTimeoutMillis( 100 )
		// .onTimeout( timeout -> assertTrue( wasClicked.get() ) )
		// .until( () -> camera.getController().notExists( SwtApplication.buttonXPath,
		// 5, 1 ) );
	}

	@Test
	public void testTextExists()
	{
		GuiObject< ? > guiObject = camera.getGuiObject( 1, 5, SwtApplication.textXPath );

		assertNotNull( guiObject );

		assertEquals( SwtApplication.initialText, guiObject.asText().getText() );

		System.out.println( XmlUtils.serialize( camera.takeSnapshot( guiObject.getObject(), null ) ) );

		guiObject.asText().setText( SwtApplication.finalText );

		assertEquals( SwtApplication.finalText, guiObject.asText().getText() );

		System.out.println( XmlUtils.serialize( camera.takeSnapshot( guiObject.getObject(), null ) ) );

	}

	@Test
	public void testLabelExists()
	{
		GuiObject< ? > guiObject = camera.getGuiObject( 1, 5, "//Label" );

		assertNotNull( guiObject );

		// System.out.println( XmlUtils.serialize( camera.takeSnapshot(
		// guiObject.getObject(), null ) ) );
	}

	@Test
	public void testComboExists()
	{
		Integer index = 2;

		GuiObject< ? > guiObject = camera.getGuiObject( 1, 5, "//Combo" );

		assertNotNull( guiObject );

		guiObject.asIndex().setSelectedIndex( index );

		assertEquals( index, guiObject.asIndex().getSelectedIndex() );

		// System.out.println( XmlUtils.serialize( camera.takeSnapshot(
		// guiObject.getObject(), null ) ) );

	}

	@Test
	public void testComboText()
	{
		String text = "magnolia";

		GuiObject< ? > guiObject = camera.getGuiObject( 1, 5, "//Combo" );

		assertNotNull( guiObject );

		guiObject.asText().setText( text );

		assertEquals( text, guiObject.asText().getText() );

		// System.out.println( XmlUtils.serialize( camera.takeSnapshot(
		// guiObject.getObject(), null ) ) );
	}

	@Test
	public void testListExists()
	{
		Integer index = 2;

		GuiObject< ? > guiObject = camera.getGuiObject( 1, 5, "//List" );

		assertNotNull( guiObject );

		guiObject.asIndex().setSelectedIndex( index );

		assertEquals( index, guiObject.asIndex().getSelectedIndex() );

		// System.out.println( XmlUtils.serialize( camera.takeSnapshot(
		// guiObject.getObject(), null ) ) );
	}

	@Test
	public void testTableExists()
	{
		Integer index = 2;

		GuiObject< ? > guiObject = camera.getGuiObject( 1, 5, "//Table" );

		assertNotNull( guiObject );

		guiObject.asTable().selectRow( index );

		// System.out.println( XmlUtils.serialize( camera.takeSnapshot(
		// guiObject.getObject(), null ) ) );
	}

	@Test
	public void testTreeExists()
	{
		String[] path = { "3:2:1" };

		GuiObject< ? > guiObject = camera.getGuiObject( 1, 5, "//Tree" );

		assertNotNull( guiObject );

		System.out.println( guiObject.asTree().getPath( path[ 0 ] ) );

		guiObject.asTree().selectPath( path[ 0 ] );

		// System.out.println( XmlUtils.serialize( camera.takeSnapshot(
		// guiObject.getObject(), null ) ) );
	}

	@Test
	public void testTabClick()
	{
		assertEquals( "0", getComponentResult( camera, "//TabFolder[1]", "@selected-index" ) );

		@SuppressWarnings( "unchecked" )
		TabItemGuiObject< TabItem > guiObject = ( TabItemGuiObject< TabItem > ) camera.getGuiObject( 1, 5, "//TabItem[@index=1]" );

		assertNotNull( guiObject );

		TabItem tabItem = guiObject.getObject();

		// System.out.println( XmlUtils.serialize( camera.takeSnapshot( tabItem, null )
		// ) );

		TabItemGuiObject.onDisplayThread( tabItem, go -> {
			go.addListener( -1, new Listener()
			{
				@Override
				public void handleEvent( Event event )
				{
					System.out.println( "widgetSelected: " + event );
				}
			} );
			return null;
		} );

		guiObject.asClick().click();

		// System.out.println( XmlUtils.serialize( camera.takeSnapshot( ) ) );

		assertEquals( "1", getComponentResult( camera, "//TabFolder", "@selected-index" ) );
	}

	public static String getComponentResult( Camera camera, String componentPath, String resultPath )
	{
		Map< String, Object > options = DataLimit.getMaxDataLimitsOptions();

		String xmlText = camera.getController().getSnapshotXmlText( componentPath, options );

		try
		{
			Document node = XmlUtils.parse( xmlText );

			return ( String ) XPathUtils
					.getCompiledPath( resultPath )
					.evaluate(
							node.getDocumentElement(),
							XPathConstants.STRING );
		}
		catch ( XPathExpressionException e )
		{
			throw new RuntimeException(
					format(
							"Failed to process xpath [%s] at location [%s].",
							resultPath,
							componentPath ),
					e );
		}
	}
}