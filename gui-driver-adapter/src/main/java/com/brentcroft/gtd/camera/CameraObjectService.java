package com.brentcroft.gtd.camera;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

import com.brentcroft.gtd.adapter.model.DefaultGuiObject;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.SnapshotGuiObject;
import com.brentcroft.gtd.adapter.model.swing.AbstractButtonGuiObject;
import com.brentcroft.gtd.adapter.model.swing.ComponentGuiObject;
import com.brentcroft.gtd.adapter.model.swing.ComponentGuiObjectConsultant;
import com.brentcroft.gtd.adapter.model.swing.ContainerGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JComboBoxGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JComponentGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JDialogGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JEditorPaneGuiObject;
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
import com.brentcroft.gtd.camera.CameraObjectManager.AdapterSpecification;


/**
 * Created by Alaric on 15/07/2017.
 */
public class CameraObjectService
{
	private CameraObjectManager gom = new CameraObjectManager();

	public CameraObjectManager getManager()
	{
		return gom;
	}

	protected < C, H extends GuiObject< ? super C > > void addAdapters( List< AdapterSpecification< C, H > > adapters, Properties properties )
	{
		adapters.addAll( buildDefaultAdapters( properties ) );
		adapters.addAll( buildSwingAdapters( properties ) );
	}

	public < C, H extends GuiObject< ? super C > > CameraObjectService install( Properties properties )
	{
		gom.clear();

		List< AdapterSpecification< C, H > > adapters = new ArrayList<>();

		addAdapters( adapters, properties );

		gom.install( adapters );

		gom.configure( properties );
		
		return this;
	}

	@SuppressWarnings( "unchecked" )
	private < C, H extends GuiObject< ? super C > > List< AdapterSpecification< C, H > > buildDefaultAdapters( Properties properties )
	{
		List< AdapterSpecification< C, H > > adapters = new ArrayList<>();

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Object.class, DefaultGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Snapshot.class, SnapshotGuiObject.class ) );

		return adapters;
	}

	@SuppressWarnings( { "unchecked" } )
	private < C, H extends GuiObject< ? super C > > List< AdapterSpecification< C, H > > buildSwingAdapters( Properties properties )
	{
		List< AdapterSpecification< C, H > > adapters = new ArrayList<>();

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( AbstractButton.class, AbstractButtonGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Component.class, ComponentGuiObject.class,
				new ComponentGuiObjectConsultant< Component >( properties ) ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( Container.class, ContainerGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JComboBox.class, JComboBoxGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JComponent.class, JComponentGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JDialog.class, JDialogGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JEditorPane.class, JEditorPaneGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JFrame.class, JFrameGuiObject.class ) );

		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JInternalFrame.class, JInternalFrameGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JLabel.class, JLabelGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JList.class, JListGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JMenu.class, JMenuGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JPanel.class, JPanelGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JSlider.class, JSliderGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JSpinner.class, JSpinnerGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JTabbedPane.class, JTabbedPaneGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JTable.class, JTableGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JTextComponent.class, JTextComponentGuiObject.class ) );
		adapters.add( ( AdapterSpecification< C, H > ) gom.newAdapterSpecification( JTree.class, JTreeGuiObject.class ) );

		return adapters;
	}


}
