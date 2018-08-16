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
import com.brentcroft.gtd.camera.CameraObjectManager.FactorySpecification;


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

	protected < C, H extends GuiObject< C > > void addAdapters( List< FactorySpecification< C, H > > adapters, Properties properties )
	{
		adapters.addAll( buildDefaultAdapters( properties ) );
		adapters.addAll( buildSwingAdapters( properties ) );
	}

	public < C, H extends GuiObject< C > > CameraObjectService install( Properties properties )
	{
		gom.clear();

		List< FactorySpecification< C, H > > adapters = new ArrayList<>();

		addAdapters( adapters, properties );

		gom.install( adapters );

		gom.configure( properties );
		
		return this;
	}

	@SuppressWarnings( "unchecked" )
	private < C, H extends GuiObject< ? super C > > List< FactorySpecification< C, H > > buildDefaultAdapters( Properties properties )
	{
		List< FactorySpecification< C, H > > adapters = new ArrayList<>();

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Object.class, DefaultGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Snapshot.class, SnapshotGuiObject.class ) );

		return adapters;
	}

	@SuppressWarnings( { "unchecked" } )
	private < C, H extends GuiObject< ? super C > > List< FactorySpecification< C, H > > buildSwingAdapters( Properties properties )
	{
		List< FactorySpecification< C, H > > adapters = new ArrayList<>();

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( AbstractButton.class, AbstractButtonGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Component.class, ComponentGuiObject.class,
				new ComponentGuiObjectConsultant< Component >( properties ) ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( Container.class, ContainerGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JComboBox.class, JComboBoxGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JComponent.class, JComponentGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JDialog.class, JDialogGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JEditorPane.class, JEditorPaneGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JFrame.class, JFrameGuiObject.class ) );

		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JInternalFrame.class, JInternalFrameGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JLabel.class, JLabelGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JList.class, JListGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JMenu.class, JMenuGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JPanel.class, JPanelGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JSlider.class, JSliderGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JSpinner.class, JSpinnerGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JTabbedPane.class, JTabbedPaneGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JTable.class, JTableGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JTextComponent.class, JTextComponentGuiObject.class ) );
		adapters.add( ( FactorySpecification< C, H > ) gom.newFactorySpecification( JTree.class, JTreeGuiObject.class ) );

		return adapters;
	}


}
