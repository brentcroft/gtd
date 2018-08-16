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
import com.brentcroft.gtd.camera.CameraObjectManager.FactorySpecification;

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
	protected < C, H extends GuiObject< C > > void addAdapters( List< FactorySpecification< C, H > > adapters, Properties properties )
	{
		super.addAdapters( adapters, properties );

		adapters.addAll( buildDefaultAdapters( properties ) );
		adapters.addAll( buildFxAdapters( properties ) );
		adapters.addAll( buildW3CAdapters( properties ) );
	}

	@SuppressWarnings( "unchecked" )
	private < C, H extends GuiObject< ? super C > > List< FactorySpecification< C, H > > buildDefaultAdapters( Properties properties )
	{
		CameraObjectManager gom = getManager();
		
		List< FactorySpecification< C, H > > adapters = new ArrayList<>();

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( FxSnapshot.class, FxSnapshotGuiObject.class ) );

		return adapters;
	}	
	
	
	@SuppressWarnings( "unchecked" )
	private < C, H extends GuiObject< ? super C > > List< FactorySpecification< C, H > > buildFxAdapters( Properties properties )
	{
		CameraObjectManager gom = getManager();

		List< FactorySpecification< C, H > > adapters = new ArrayList<>();

		// fx - base
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Node.class, FxNodeGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( MenuItem.class, FxMenuItemGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Tab.class, FxTabGuiObject.class ) );


		// fx - extras
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Menu.class, FxMenuGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( TabPane.class, FxTabPaneGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( MenuBar.class, FxMenuBarGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Parent.class, FxParentGuiObject.class ) );

		// bridge to W3c
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( WebView.class, W3cWebViewGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Control.class, FxControlGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( HTMLEditor.class, FxHTMLEditorGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( TreeView.class, FxTreeViewGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( ListView.class, FxListViewGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( ButtonBar.class, FxButtonBarGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( ToolBar.class, FxToolbarGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( SplitPane.class, FxSplitPaneGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Accordion.class, FxAccordionGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( ScrollPane.class, FxScrollPaneGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Labeled.class, FxLabeledGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( TitledPane.class, FxTitledPaneGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( ButtonBase.class, FxButtonBaseGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( TextInputControl.class, FxTextInputControlGuiObject.class ) );

		// FX/Swing bridge
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JFXPanel.class, JFXPanelGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( SwingNode.class, FxSwingNodeGuiObject.class ) );

		return adapters;
	}

	@SuppressWarnings( "unchecked" )
	private < C, H extends GuiObject< ? super C > > List< FactorySpecification< C, H > > buildW3CAdapters( Properties properties )
	{
		CameraObjectManager gom = getManager();

		List< FactorySpecification< C, H > > adapters = new ArrayList<>();

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Text.class, W3cTextGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification(
				HTMLElement.class,
				W3cHTMLElementGuiObject.class,
				new W3CHTMLElementGuiObjectConsultant< HTMLElement >( properties ) ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( HTMLIFrameElement.class, W3cHTMLIFrameElementGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( HTMLAnchorElement.class, W3cHTMLAnchorElementGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( HTMLButtonElement.class, W3cHTMLButtonElement.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( HTMLSelectElement.class, W3cHTMLSelectElementGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( HTMLInputElement.class, W3cHTMLInputElementGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( HTMLTextAreaElement.class, W3cHTMLTextAreaElementGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( HTMLTableElement.class, W3cHTMLTableGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( HTMLLabelElement.class, W3cHTMLLabelElementGuiObject.class ) );

		return adapters;
	}

}
