package com.brentcroft.gtd.adapter.utils;

import java.util.Properties;

import org.junit.Test;

import com.brentcroft.gtd.camera.Camera;
import com.brentcroft.gtd.camera.CameraObjectService;
import com.brentcroft.util.XmlUtils;

public class GobVisitorTest
{
	CameraObjectService gos = new CameraObjectService();

	@Test
	public void visit()
	{
		gos.install( new Properties() );

//		GuiObject origin = gos
//				.getManager()
//				.adapt( new Snapshot(), null );

		// XPathGuiObjectVisitor visitor = new XPathGuiObjectVisitor();
		//
		// XPathUtils.visitPath(
		// visitor,
		// origin,
		// "Snapshot[ @timestamp ]/bollocks" );
		//
		// System.out.println( visitor.getSelected() );

		Camera c = new Camera();

		System.out.println( c.getReport() );

		System.out.println( XmlUtils.serialize( c.takeSnapshot() ) );
	}
}