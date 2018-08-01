package com.brentcroft.gtd.camera;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.W3CHTMLElementGuiObjectConsultant;
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
import com.brentcroft.gtd.adapter.model.w3c.W3cWebViewGuiObject;
import com.brentcroft.gtd.camera.CameraObjectManager.AdapterSpecification;

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
import javafx.scene.web.WebView;

/**
 * Created by Alaric on 15/07/2017.
 */
@SuppressWarnings( "restriction" )
public class FxCameraObjectService extends CameraObjectService
{

	@Override
	protected < C, H extends GuiObject< C > > void addAdapters( List< AdapterSpecification< C, H > > adapters, Properties properties )
	{
		super.addAdapters( adapters, properties );

		adapters.addAll( buildDefaultAdapters( properties ) );
		adapters.addAll( buildFxAdapters( properties ) );
		adapters.addAll( buildW3CAdapters( properties ) );
	}

	@SuppressWarnings( "unchecked" )
	private < C, H extends GuiObject< ? super C > > List< AdapterSpecification< C, H > > buildDefaultAdapters( Properties properties )
	{
		CameraObjectManager gom = getManager();
		
		List< AdapterSpecification< C, H > > adapters = new ArrayList<>();

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( FxSnapshot.class, FxSnapshotGuiObject.class ) );

		return adapters;
	}	
	
	
	@SuppressWarnings( "unchecked" )
	private < C, H extends GuiObject< ? super C > > List< AdapterSpecification< C, H > > buildFxAdapters( Properties properties )
	{
		CameraObjectManager gom = getManager();

		List< AdapterSpecification< C, H > > adapters = new ArrayList<>();

		// fx - base
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Node.class, FxNodeGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( MenuItem.class, FxMenuItemGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Tab.class, FxTabGuiObject.class ) );


		// fx - extras
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Menu.class, FxMenuGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( TabPane.class, FxTabPaneGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( MenuBar.class, FxMenuBarGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Parent.class, FxParentGuiObject.class ) );

		// bridge to W3c
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( WebView.class, W3cWebViewGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Control.class, FxControlGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( HTMLEditor.class, FxHTMLEditorGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( TreeView.class, FxTreeViewGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( ListView.class, FxListViewGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( ButtonBar.class, FxButtonBarGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( ToolBar.class, FxToolbarGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( SplitPane.class, FxSplitPaneGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Accordion.class, FxAccordionGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( ScrollPane.class, FxScrollPaneGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Labeled.class, FxLabeledGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( TitledPane.class, FxTitledPaneGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( ButtonBase.class, FxButtonBaseGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( TextInputControl.class, FxTextInputControlGuiObject.class ) );

		// FX/Swing bridge
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JFXPanel.class, JFXPanelGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( SwingNode.class, FxSwingNodeGuiObject.class ) );

		return adapters;
	}

	@SuppressWarnings( "unchecked" )
	private < C, H extends GuiObject< ? super C > > List< AdapterSpecification< C, H > > buildW3CAdapters( Properties properties )
	{
		CameraObjectManager gom = getManager();

		List< AdapterSpecification< C, H > > adapters = new ArrayList<>();

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Text.class, W3cTextGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification(
				HTMLElement.class,
				W3cHTMLElementGuiObject.class,
				new W3CHTMLElementGuiObjectConsultant< HTMLElement >( properties ) ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( HTMLIFrameElement.class, W3cHTMLIFrameElementGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( HTMLAnchorElement.class, W3cHTMLAnchorElementGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( HTMLButtonElement.class, W3cHTMLButtonElement.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( HTMLSelectElement.class, W3cHTMLSelectElementGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( HTMLInputElement.class, W3cHTMLInputElementGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( HTMLTextAreaElement.class, W3cHTMLTextAreaElementGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( HTMLTableElement.class, W3cHTMLTableGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( HTMLLabelElement.class, W3cHTMLLabelElementGuiObject.class ) );

		return adapters;
	}

}
