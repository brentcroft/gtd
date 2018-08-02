package com.brentcroft.gtd.camera;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Properties;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.brentcroft.gtd.camera.model.SwtSnapshotGuiObject;
import com.brentcroft.gtd.camera.model.swt.BrowserGuiObject;
import com.brentcroft.gtd.camera.model.swt.ButtonGuiObject;
import com.brentcroft.gtd.camera.model.swt.ComboGuiObject;
import com.brentcroft.gtd.camera.model.swt.CompositeGuiObject;
import com.brentcroft.gtd.camera.model.swt.ControlGuiObject;
import com.brentcroft.gtd.camera.model.swt.ItemGuiObject;
import com.brentcroft.gtd.camera.model.swt.LabelGuiObject;
import com.brentcroft.gtd.camera.model.swt.LinkGuiObject;
import com.brentcroft.gtd.camera.model.swt.ListGuiObject;
import com.brentcroft.gtd.camera.model.swt.MenuGuiObject;
import com.brentcroft.gtd.camera.model.swt.MenuItemGuiObject;
import com.brentcroft.gtd.camera.model.swt.ShellGuiObject;
import com.brentcroft.gtd.camera.model.swt.TabFolderGuiObject;
import com.brentcroft.gtd.camera.model.swt.TabItemGuiObject;
import com.brentcroft.gtd.camera.model.swt.TableGuiObject;
import com.brentcroft.gtd.camera.model.swt.TextGuiObject;
import com.brentcroft.gtd.camera.model.swt.ToolBarGuiObject;
import com.brentcroft.gtd.camera.model.swt.ToolItemGuiObject;
import com.brentcroft.gtd.camera.model.swt.TreeGuiObject;
import com.brentcroft.gtd.camera.model.swt.WidgetGuiObject;

public class SwtCameraObjectServiceTest implements ObjectServiceInstall
{
	@Mock
	Properties properties;

	CameraObjectService gos;

	@Before
	public void setUp()
	{
		initMocks( this );

		gos = new SwtCameraObjectService().install( properties );
		
		//System.out.println( CODE_GENERATOR.apply( gos.getManager() ) );
	}



	@Test
	public void installsSnapshotAdapter() throws Exception
	{
		assertEquals( SwtSnapshotGuiObject.class, adapteeClass(gos,  new SwtSnapshot() ) );
	}

	@Override
	public void installsServiceAdapters() throws Exception
	{
		assertEquals( BrowserGuiObject.class, adapteeClass( gos, Mockito.mock( Browser.class ) ) );
		assertEquals( ButtonGuiObject.class, adapteeClass( gos, Mockito.mock( Button.class ) ) );
		assertEquals( ComboGuiObject.class, adapteeClass( gos, Mockito.mock( Combo.class ) ) );
		assertEquals( CompositeGuiObject.class, adapteeClass( gos, Mockito.mock( Composite.class ) ) );
		assertEquals( ControlGuiObject.class, adapteeClass( gos, Mockito.mock( Control.class ) ) );
		assertEquals( ItemGuiObject.class, adapteeClass( gos, Mockito.mock( Item.class ) ) );
		assertEquals( LabelGuiObject.class, adapteeClass( gos, Mockito.mock( Label.class ) ) );
		assertEquals( LinkGuiObject.class, adapteeClass( gos, Mockito.mock( Link.class ) ) );
		assertEquals( ListGuiObject.class, adapteeClass( gos, Mockito.mock( List.class ) ) );
		assertEquals( MenuGuiObject.class, adapteeClass( gos, Mockito.mock( Menu.class ) ) );
		assertEquals( MenuItemGuiObject.class, adapteeClass( gos, Mockito.mock( MenuItem.class ) ) );
		assertEquals( ShellGuiObject.class, adapteeClass( gos, Mockito.mock( Shell.class ) ) );
		assertEquals( SwtSnapshotGuiObject.class, adapteeClass( gos, Mockito.mock( SwtSnapshot.class ) ) );
		assertEquals( TabFolderGuiObject.class, adapteeClass( gos, Mockito.mock( TabFolder.class ) ) );
		assertEquals( TabItemGuiObject.class, adapteeClass( gos, Mockito.mock( TabItem.class ) ) );
		assertEquals( TableGuiObject.class, adapteeClass( gos, Mockito.mock( Table.class ) ) );
		assertEquals( TextGuiObject.class, adapteeClass( gos, Mockito.mock( Text.class ) ) );
		assertEquals( ToolBarGuiObject.class, adapteeClass( gos, Mockito.mock( ToolBar.class ) ) );
		assertEquals( ToolItemGuiObject.class, adapteeClass( gos, Mockito.mock( ToolItem.class ) ) );
		assertEquals( TreeGuiObject.class, adapteeClass( gos, Mockito.mock( Tree.class ) ) );
		assertEquals( WidgetGuiObject.class, adapteeClass( gos, Mockito.mock( Widget.class ) ) );
	}

}