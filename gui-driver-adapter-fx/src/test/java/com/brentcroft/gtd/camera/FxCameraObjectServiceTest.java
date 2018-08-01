package com.brentcroft.gtd.camera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.w3c.dom.Text;
import org.w3c.dom.html.HTMLAnchorElement;
import org.w3c.dom.html.HTMLButtonElement;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLIFrameElement;
import org.w3c.dom.html.HTMLInputElement;
import org.w3c.dom.html.HTMLLabelElement;
import org.w3c.dom.html.HTMLSelectElement;
import org.w3c.dom.html.HTMLTableElement;
import org.w3c.dom.html.HTMLTextAreaElement;

import com.brentcroft.gtd.adapter.model.FxSnapshotGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxAccordionGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxButtonBarGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxButtonBaseGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxControlGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxHTMLEditorGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxLabeledGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxListViewGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxMenuBarGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxMenuGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxMenuItemGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxNodeGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxParentGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxScrollPaneGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxSplitPaneGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxSwingNodeGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxTabGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxTabPaneGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxTextInputControlGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxTitledPaneGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxToolbarGuiObject;
import com.brentcroft.gtd.adapter.model.fx.FxTreeViewGuiObject;
import com.brentcroft.gtd.adapter.model.fx.JFXPanelGuiObject;
import com.brentcroft.gtd.adapter.model.w3c.W3cHTMLAnchorElementGuiObject;
import com.brentcroft.gtd.adapter.model.w3c.W3cHTMLButtonElement;
import com.brentcroft.gtd.adapter.model.w3c.W3cHTMLElementGuiObject;
import com.brentcroft.gtd.adapter.model.w3c.W3cHTMLIFrameElementGuiObject;
import com.brentcroft.gtd.adapter.model.w3c.W3cHTMLInputElementGuiObject;
import com.brentcroft.gtd.adapter.model.w3c.W3cHTMLLabelElementGuiObject;
import com.brentcroft.gtd.adapter.model.w3c.W3cHTMLSelectElementGuiObject;
import com.brentcroft.gtd.adapter.model.w3c.W3cHTMLTableGuiObject;
import com.brentcroft.gtd.adapter.model.w3c.W3cHTMLTextAreaElementGuiObject;
import com.brentcroft.gtd.adapter.model.w3c.W3cTextGuiObject;

import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Accordion;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Control;
import javafx.scene.control.Labeled;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeView;
import javafx.scene.web.HTMLEditor;

@SuppressWarnings( "restriction" )
public class FxCameraObjectServiceTest implements ObjectServiceInstall
{
	@Mock
	Properties properties;

	CameraObjectService gos;

	@Before
	public void setUp()
	{
		jfxInitialisationBodge();
		
		initMocks( this );

		gos = new FxCameraObjectService().install( properties );
		
		//System.out.println( CODE_GENERATOR.apply( gos.getManager() ) );
	}


	@Test
	public void installsSnapshotAdapter() throws Exception
	{
		assertEquals( FxSnapshotGuiObject.class, adapteeClass( gos, new FxSnapshot() ));
	}

	@Test
	public void installsServiceAdapters() throws Exception
	{
		installsAllFx();
		installsAllW3C();
	}
	
	private void installsAllFx()
	{
		assertEquals( FxAccordionGuiObject.class, adapteeClass( gos, Mockito.mock( Accordion.class ) ) );
		assertEquals( FxButtonBarGuiObject.class, adapteeClass( gos, Mockito.mock( ButtonBar.class ) ) );
		assertEquals( FxButtonBaseGuiObject.class, adapteeClass( gos, Mockito.mock( ButtonBase.class ) ) );
		assertEquals( FxControlGuiObject.class, adapteeClass( gos, Mockito.mock( Control.class ) ) );
		assertEquals( FxHTMLEditorGuiObject.class, adapteeClass( gos, Mockito.mock( HTMLEditor.class ) ) );
		assertEquals( FxLabeledGuiObject.class, adapteeClass( gos, Mockito.mock( Labeled.class ) ) );
		assertEquals( FxListViewGuiObject.class, adapteeClass( gos, Mockito.mock( ListView.class ) ) );
		assertEquals( FxMenuBarGuiObject.class, adapteeClass( gos, Mockito.mock( MenuBar.class ) ) );
		assertEquals( FxMenuGuiObject.class, adapteeClass( gos, Mockito.mock( Menu.class ) ) );
		assertEquals( FxMenuItemGuiObject.class, adapteeClass( gos, Mockito.mock( MenuItem.class ) ) );
		assertEquals( FxNodeGuiObject.class, adapteeClass( gos, Mockito.mock( Node.class ) ) );
		assertEquals( FxParentGuiObject.class, adapteeClass( gos, Mockito.mock( Parent.class ) ) );
		assertEquals( FxScrollPaneGuiObject.class, adapteeClass( gos, Mockito.mock( ScrollPane.class ) ) );
		assertEquals( FxSplitPaneGuiObject.class, adapteeClass( gos, Mockito.mock( SplitPane.class ) ) );
		assertEquals( FxSwingNodeGuiObject.class, adapteeClass( gos, Mockito.mock( SwingNode.class ) ) );
		assertEquals( FxTabGuiObject.class, adapteeClass( gos, Mockito.mock( Tab.class ) ) );
		assertEquals( FxTabPaneGuiObject.class, adapteeClass( gos, Mockito.mock( TabPane.class ) ) );
		assertEquals( FxTextInputControlGuiObject.class, adapteeClass( gos, Mockito.mock( TextInputControl.class ) ) );
		assertEquals( FxTitledPaneGuiObject.class, adapteeClass( gos, Mockito.mock( TitledPane.class ) ) );
		assertEquals( FxToolbarGuiObject.class, adapteeClass( gos, Mockito.mock( ToolBar.class ) ) );
		assertEquals( FxTreeViewGuiObject.class, adapteeClass( gos, Mockito.mock( TreeView.class ) ) );
		assertEquals( JFXPanelGuiObject.class, adapteeClass( gos, Mockito.mock( JFXPanel.class ) ) );
	}

	private void installsAllW3C()
	{
		assertEquals( W3cHTMLAnchorElementGuiObject.class, adapteeClass( gos, Mockito.mock( HTMLAnchorElement.class ) ) );
		assertEquals( W3cHTMLButtonElement.class, adapteeClass( gos, Mockito.mock( HTMLButtonElement.class ) ) );
		assertEquals( W3cHTMLElementGuiObject.class, adapteeClass( gos, Mockito.mock( HTMLElement.class ) ) );
		assertEquals( W3cHTMLIFrameElementGuiObject.class, adapteeClass( gos, Mockito.mock( HTMLIFrameElement.class ) ) );
		assertEquals( W3cHTMLInputElementGuiObject.class, adapteeClass( gos, Mockito.mock( HTMLInputElement.class ) ) );
		assertEquals( W3cHTMLLabelElementGuiObject.class, adapteeClass( gos, Mockito.mock( HTMLLabelElement.class ) ) );
		assertEquals( W3cHTMLSelectElementGuiObject.class, adapteeClass( gos, Mockito.mock( HTMLSelectElement.class ) ) );
		assertEquals( W3cHTMLTableGuiObject.class, adapteeClass( gos, Mockito.mock( HTMLTableElement.class ) ) );
		assertEquals( W3cHTMLTextAreaElementGuiObject.class, adapteeClass( gos, Mockito.mock( HTMLTextAreaElement.class ) ) );
		assertEquals( W3cTextGuiObject.class, adapteeClass( gos, Mockito.mock( Text.class ) ) );
		// assertEquals( W3cWebViewGuiObject.class, adapteeClass( gos, Mockito.mock(
		// WebView.class ) ) );
	}

	

	/*
	 * see: https://stackoverflow.com/questions/11273773/javafx-2-1-toolkit-not-
	 * initialized
	 */
	private void jfxInitialisationBodge()
	{
		final CountDownLatch latch = new CountDownLatch( 1 );

		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				new JFXPanel();
				latch.countDown();
			}
		} );

		try
		{
			latch.await( 10, TimeUnit.SECONDS );
		}
		catch ( InterruptedException e )
		{
			fail("Interrupted waiting for JFX to initialise.");
		}
	}

}