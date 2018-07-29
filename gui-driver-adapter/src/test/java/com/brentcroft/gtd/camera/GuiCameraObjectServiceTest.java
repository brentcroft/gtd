package com.brentcroft.gtd.camera;

import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.w3c.dom.html.HTMLDivElement;

import com.brentcroft.gtd.adapter.model.GuiObject;

public class GuiCameraObjectServiceTest
{
	@Mock
	HTMLDivElement htmlDivElement;

	@Before
	public void setUp()
	{
		initMocks( this );
	}

	@Test
	public void install() throws Exception
	{
		CameraObjectService gos = new CameraObjectService();

		gos.install( new Properties() );

		System.out.println( gos.getManager() );

		GuiObject< ? > go = gos.getManager().adapt( htmlDivElement, null );

		assertNotNull( go );

	}

}