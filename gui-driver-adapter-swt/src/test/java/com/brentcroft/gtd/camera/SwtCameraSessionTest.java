package com.brentcroft.gtd.camera;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.brentcroft.gtd.driver.Backend;
import com.brentcroft.gtd.driver.client.GuiLauncher;
import com.brentcroft.gtd.js.driver.JSGuiLocalDriver;
import com.brentcroft.gtd.js.driver.JSGuiSession;
import com.brentcroft.gtd.swt.SwtApplication;

public class SwtCameraSessionTest
{
	JSGuiSession session = new JSGuiSession( "TestSession" )
	{
		{
			int jmxPort = 9909;

			String mBeanref = Backend.getMBeanRef( CameraController.class );
			String jmxUri = format( "service:jmx:rmi:///jndi/rmi://:%d/jmxrmi", jmxPort );

			setDriver( new JSGuiLocalDriver()
			{
				{
					setJmxRmiUrl( jmxUri );
					setMBeanRef( mBeanref );

				}
			} );

			setLauncher( new GuiLauncher( getDriver() )
			{
				{
					setApplicationMainClass( SwtApplication.class.getName() );
					setApplicationServiceClass( SwtCamera.class.getName() );

					setJavaCommand( "cmd /C java" );
					setJavaVmOptions(
							String.join(
									" ",
									"-Xmx1024m",
									"-Dsun.awt.disablegrab=true",
									format( " -Dcom.sun.management.jmxremote.port=%d", jmxPort ),
									"-Dcom.sun.management.jmxremote.authenticate=false",
									"-Dcom.sun.management.jmxremote.ssl=false"
							) );

					setJavaClassPathRoot( "../" );
					setJavaClassPath(
							String.join(
									";",
									"gui-driver-adapter-swt/target/*",
									"gui-driver-adapter-swt/src/test/resources",
									"gui-driver-cucumber/src/test/resources/lib/*"
							) );

					setAddShutdownHook( true );
				}
			} );
		}
	};

	@Before
	public void setUp()
	{
		session.start();
		
		//System.out.println( session.getDriver().getSnapshotXmlText() );
	}

	@After
	public void tearDown()
	{
		session.stop();
	}

	@Test
	public void testButtonExists()
	{
		assertTrue( session.getDriver().exists( SwtApplication.buttonXPath ) );

		System.out.println( session.getDriver().getSnapshotXmlText( SwtApplication.buttonXPath, null ) );

		assertEquals( SwtApplication.buttonName, session.getDriver().getComponentResultText( SwtApplication.buttonXPath, "@text" ) );

	}

	@Test
	public void testButtonClick()
	{
		assertTrue( session.getDriver().exists( SwtApplication.buttonXPath ) );

		session.getDriver().click( SwtApplication.buttonXPath );
		
		// because the click closed the remote app
		assertFalse( session.getDriver().isConnected() );
	}
	
	@Test
	public void testTextExists()
	{
		assertTrue( session.getDriver().exists( SwtApplication.textXPath ) );
		
		assertEquals( SwtApplication.initialText, session.getDriver().getComponentResultText( SwtApplication.textXPath, "@text" ) );
		
		session.getDriver().setText( SwtApplication.textXPath, SwtApplication.finalText );

		assertEquals( SwtApplication.finalText, session.getDriver().getComponentResultText( SwtApplication.textXPath, "@text" ) );
	}
	
	
	@Test
	public void testToolItemsExist()
	{
		for (int i : new int[] { 0, 1, 2, 3, 4 }) {
			String xpath = format("//ToolItem[@text='TI-%d']", i );
			
			assertTrue( session.getDriver().exists( xpath ) );
			
			session.getDriver().click( xpath );
		}
	}

}