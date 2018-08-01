package com.brentcroft.gtd.camera;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

import java.awt.Component;
import java.awt.Container;
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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.brentcroft.gtd.adapter.model.DefaultGuiObject;
import com.brentcroft.gtd.adapter.model.SnapshotGuiObject;
import com.brentcroft.gtd.adapter.model.swing.AbstractButtonGuiObject;
import com.brentcroft.gtd.adapter.model.swing.ComponentGuiObject;
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

public class SwingCameraObjectServiceTest implements ObjectServiceInstall
{
	@Mock
	Properties properties;

	CameraObjectService gos;

	@Before
	public void setUp()
	{
		initMocks( this );

		gos = new CameraObjectService().install( properties );
		
		//System.out.println( CODE_GENERATOR.apply( gos.getManager() ) );
	}

	// TODO: move abstract adapter package
	// when swing stuff is packaged
	@Test
	public void installsDefaultAdapter() throws Exception
	{
		assertEquals( DefaultGuiObject.class, adapteeClass( gos, new Object() ) );
	}

	
	

	@Override
	@Test
	public void installsSnapshotAdapter() throws Exception
	{
		assertEquals( SnapshotGuiObject.class, adapteeClass(gos,  new Snapshot() ) );
	}


	@Override
	public void installsServiceAdapters() throws Exception
	{
		assertEquals( JListGuiObject.class, adapteeClass( gos, Mockito.mock( JList.class ) ) );
		assertEquals( JPanelGuiObject.class, adapteeClass( gos, Mockito.mock( JPanel.class ) ) );
		assertEquals( DefaultGuiObject.class, adapteeClass( gos, Mockito.mock( Object.class ) ) );
		assertEquals( ContainerGuiObject.class, adapteeClass( gos, Mockito.mock( Container.class ) ) );
		assertEquals( JInternalFrameGuiObject.class, adapteeClass( gos, Mockito.mock( JInternalFrame.class ) ) );
		assertEquals( JComboBoxGuiObject.class, adapteeClass( gos, Mockito.mock( JComboBox.class ) ) );
		assertEquals( SnapshotGuiObject.class, adapteeClass( gos, Mockito.mock( Snapshot.class ) ) );
		assertEquals( JComponentGuiObject.class, adapteeClass( gos, Mockito.mock( JComponent.class ) ) );
		assertEquals( AbstractButtonGuiObject.class, adapteeClass( gos, Mockito.mock( AbstractButton.class ) ) );
		assertEquals( JSpinnerGuiObject.class, adapteeClass( gos, Mockito.mock( JSpinner.class ) ) );
		assertEquals( ComponentGuiObject.class, adapteeClass( gos, Mockito.mock( Component.class ) ) );
		assertEquals( JTabbedPaneGuiObject.class, adapteeClass( gos, Mockito.mock( JTabbedPane.class ) ) );
		assertEquals( JMenuGuiObject.class, adapteeClass( gos, Mockito.mock( JMenu.class ) ) );
		assertEquals( JTableGuiObject.class, adapteeClass( gos, Mockito.mock( JTable.class ) ) );
		assertEquals( JDialogGuiObject.class, adapteeClass( gos, Mockito.mock( JDialog.class ) ) );
		assertEquals( JEditorPaneGuiObject.class, adapteeClass( gos, Mockito.mock( JEditorPane.class ) ) );
		assertEquals( JTreeGuiObject.class, adapteeClass( gos, Mockito.mock( JTree.class ) ) );
		assertEquals( JLabelGuiObject.class, adapteeClass( gos, Mockito.mock( JLabel.class ) ) );
		assertEquals( JTextComponentGuiObject.class, adapteeClass( gos, Mockito.mock( JTextComponent.class ) ) );
		assertEquals( JSliderGuiObject.class, adapteeClass( gos, Mockito.mock( JSlider.class ) ) );
		assertEquals( JFrameGuiObject.class, adapteeClass( gos, Mockito.mock( JFrame.class ) ) );
	}


}