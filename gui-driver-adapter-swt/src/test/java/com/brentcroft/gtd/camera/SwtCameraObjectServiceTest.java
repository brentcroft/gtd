package com.brentcroft.gtd.camera;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Properties;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.brentcroft.gtd.camera.model.SwtSnapshotGuiObject;
import com.brentcroft.gtd.camera.model.swt.TableGuiObject;
import com.brentcroft.gtd.camera.model.swt.TreeGuiObject;
import com.brentcroft.gtd.camera.model.swt.WidgetGuiObject;

public class SwtCameraObjectServiceTest implements ObjectServiceInstall
{
	CameraObjectService gos;

	@Before
	public void setUp()
	{
		initMocks( this );

		gos = new SwtCameraObjectService().install( Mockito.mock( Properties.class ) );

		//System.out.println( CODE_GENERATOR.apply( gos.getManager() ) );
	}

	@Test
	public void installsSnapshotAdapter() throws Exception
	{
		assertEquals( SwtSnapshotGuiObject.class, adapteeClass( gos, Mockito.mock( SwtSnapshot.class ) ) );
	}

	@Override
	@Test
	public void installsServiceAdapters() throws Exception
	{
		assertEquals( WidgetGuiObject.class, adapteeClass( gos, Mockito.mock( Widget.class ) ) );

		assertEquals( TableGuiObject.class, adapteeClass( gos, Mockito.mock( Table.class ) ) );
		assertEquals( TreeGuiObject.class, adapteeClass( gos, Mockito.mock( Tree.class ) ) );

	}

}