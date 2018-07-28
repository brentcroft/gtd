package com.brentcroft.gtd.camera;

import com.brentcroft.gtd.adapter.model.DefaultGuiObject;
import com.brentcroft.gtd.adapter.model.SnapshotGuiObject;
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
import com.brentcroft.gtd.adapter.model.swing.AbstractButtonGuiObject;
import com.brentcroft.gtd.adapter.model.swing.ComponentGuiObject;
import com.brentcroft.gtd.adapter.model.swing.ComponentGuiObjectConsultant;
import com.brentcroft.gtd.adapter.model.swing.ContainerGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JComboBoxGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JComponentGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JDialogGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JEditorPaneGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JFXPanelGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JFrameGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JInternalFrameGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JLabelGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JListGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JMenuGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JPanelGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JSliderGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JSpinnerGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JTabbedPaneGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JTableGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JTextComponentGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JTreeGuiObject;
import com.brentcroft.gtd.adapter.model.w3c.W3CHTMLElementGuiObjectConsultant;
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

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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
import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.text.JTextComponent;
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

/**
 * Created by Alaric on 15/07/2017.
 */
public class CameraObjectService 
{
	private CameraObjectManager gom = new CameraObjectManager();

	public CameraObjectManager getManager() {
		return gom;
	}

	protected void addAdapters(List<AdapterSpecification> adapters, Properties properties) {
		adapters.addAll(buildDefaultAdapters(properties));
		adapters.addAll(buildSwingAdapters(properties));
		adapters.addAll(buildFxAdapters(properties));
		adapters.addAll(buildW3CAdapters(properties));
	}

	public void install(Properties properties) {
		gom.clear();

		List<AdapterSpecification> adapters = new ArrayList<>();

		addAdapters(adapters, properties);

		gom.install(adapters);

		gom.configure(properties);
	}

	private List<CameraObjectManager.AdapterSpecification> buildDefaultAdapters(Properties properties) {
		List<CameraObjectManager.AdapterSpecification> adapters = new ArrayList<>();

		adapters.add(gom.newAdapterSpecification(Object.class, DefaultGuiObject.class));
		adapters.add(gom.newAdapterSpecification(Snapshot.class, SnapshotGuiObject.class));

		return adapters;
	}

	private List<CameraObjectManager.AdapterSpecification> buildSwingAdapters(Properties properties) {
		List<CameraObjectManager.AdapterSpecification> adapters = new ArrayList<>();

		adapters.add(gom.newAdapterSpecification(AbstractButton.class, AbstractButtonGuiObject.class));
		adapters.add(gom.newAdapterSpecification(Component.class, ComponentGuiObject.class,
				new ComponentGuiObjectConsultant(properties)));
		adapters.add(gom.newAdapterSpecification(Container.class, ContainerGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JComboBox.class, JComboBoxGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JComponent.class, JComponentGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JDialog.class, JDialogGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JEditorPane.class, JEditorPaneGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JFrame.class, JFrameGuiObject.class));

		// bridge to FX
		adapters.add(gom.newAdapterSpecification(JFXPanel.class, JFXPanelGuiObject.class));

		adapters.add(gom.newAdapterSpecification(JInternalFrame.class, JInternalFrameGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JLabel.class, JLabelGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JList.class, JListGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JMenu.class, JMenuGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JPanel.class, JPanelGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JSlider.class, JSliderGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JSpinner.class, JSpinnerGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JTabbedPane.class, JTabbedPaneGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JTable.class, JTableGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JTextComponent.class, JTextComponentGuiObject.class));
		adapters.add(gom.newAdapterSpecification(JTree.class, JTreeGuiObject.class));

		return adapters;
	}

	private List<CameraObjectManager.AdapterSpecification> buildFxAdapters(Properties properties) {
		List<CameraObjectManager.AdapterSpecification> adapters = new ArrayList<>();

		// fx - base
		adapters.add(gom.newAdapterSpecification(Node.class, FxNodeGuiObject.class));
		adapters.add(gom.newAdapterSpecification(MenuItem.class, FxMenuItemGuiObject.class));
		adapters.add(gom.newAdapterSpecification(Tab.class, FxTabGuiObject.class));

		// bridge to swing
		adapters.add(gom.newAdapterSpecification(SwingNode.class, FxSwingNodeGuiObject.class));

		// fx - extras
		adapters.add(gom.newAdapterSpecification(Menu.class, FxMenuGuiObject.class));
		adapters.add(gom.newAdapterSpecification(TabPane.class, FxTabPaneGuiObject.class));
		adapters.add(gom.newAdapterSpecification(MenuBar.class, FxMenuBarGuiObject.class));

		adapters.add(gom.newAdapterSpecification(Parent.class, FxParentGuiObject.class));

		// bridge to W3c
		adapters.add(gom.newAdapterSpecification(WebView.class, W3cWebViewGuiObject.class));

		adapters.add(gom.newAdapterSpecification(Control.class, FxControlGuiObject.class));

		adapters.add(gom.newAdapterSpecification(HTMLEditor.class, FxHTMLEditorGuiObject.class));
		adapters.add(gom.newAdapterSpecification(TreeView.class, FxTreeViewGuiObject.class));
		adapters.add(gom.newAdapterSpecification(ListView.class, FxListViewGuiObject.class));
		adapters.add(gom.newAdapterSpecification(ButtonBar.class, FxButtonBarGuiObject.class));
		adapters.add(gom.newAdapterSpecification(ToolBar.class, FxToolbarGuiObject.class));
		adapters.add(gom.newAdapterSpecification(SplitPane.class, FxSplitPaneGuiObject.class));
		adapters.add(gom.newAdapterSpecification(Accordion.class, FxAccordionGuiObject.class));
		adapters.add(gom.newAdapterSpecification(ScrollPane.class, FxScrollPaneGuiObject.class));

		adapters.add(gom.newAdapterSpecification(Labeled.class, FxLabeledGuiObject.class));
		adapters.add(gom.newAdapterSpecification(TitledPane.class, FxTitledPaneGuiObject.class));
		adapters.add(gom.newAdapterSpecification(ButtonBase.class, FxButtonBaseGuiObject.class));

		adapters.add(gom.newAdapterSpecification(TextInputControl.class, FxTextInputControlGuiObject.class));

		return adapters;
	}

	private List<CameraObjectManager.AdapterSpecification> buildW3CAdapters(Properties properties) {
		List<CameraObjectManager.AdapterSpecification> adapters = new ArrayList<>();

		adapters.add(gom.newAdapterSpecification(Text.class, W3cTextGuiObject.class));

		adapters.add(gom.newAdapterSpecification(HTMLElement.class, W3cHTMLElementGuiObject.class,
				new W3CHTMLElementGuiObjectConsultant(properties)));

		adapters.add(gom.newAdapterSpecification(HTMLIFrameElement.class, W3cHTMLIFrameElementGuiObject.class));

		adapters.add(gom.newAdapterSpecification(HTMLAnchorElement.class, W3cHTMLAnchorElementGuiObject.class));

		adapters.add(gom.newAdapterSpecification(HTMLButtonElement.class, W3cHTMLButtonElement.class));

		adapters.add(gom.newAdapterSpecification(HTMLSelectElement.class, W3cHTMLSelectElementGuiObject.class));
		adapters.add(gom.newAdapterSpecification(HTMLInputElement.class, W3cHTMLInputElementGuiObject.class));
		adapters.add(gom.newAdapterSpecification(HTMLTextAreaElement.class, W3cHTMLTextAreaElementGuiObject.class));
		adapters.add(gom.newAdapterSpecification(HTMLTableElement.class, W3cHTMLTableGuiObject.class));

		adapters.add(gom.newAdapterSpecification(HTMLLabelElement.class, W3cHTMLLabelElementGuiObject.class));

		return adapters;
	}

}
