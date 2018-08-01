package com.brentcroft.gtd.camera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import com.brentcroft.gtd.adapter.model.DefaultGuiObject;
import com.brentcroft.gtd.adapter.model.SnapshotGuiObject;
import com.brentcroft.gtd.adapter.model.swing.AbstractButtonGuiObject;
import com.brentcroft.gtd.adapter.model.swing.ComponentGuiObject;
import com.brentcroft.gtd.adapter.model.swing.ContainerGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JComboBoxGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JComponentGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JDialogGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JFrameGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JInternalFrameGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JLabelGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JListGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JSliderGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JSpinnerGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JTabbedPaneGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JTableGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JTextComponentGuiObject;
import com.brentcroft.gtd.adapter.model.swing.JTreeGuiObject;

public class GuiCameraObjectServiceTest
{
	CameraObjectService gos;

	Object object = new Object();

	@Mock
	JPanel panel;

	@Mock
	JEditorPane editorPane;

	@Mock
	JMenu menu;

	@Mock
	AbstractButton abstractButton;

	@Mock
	JComboBox< ? > comboBox;

	@Mock
	JInternalFrame internalFrame;

	@Mock
	JLabel label;

	@Mock
	JList< ? > list;

	@Mock
	JSlider slider;

	@Mock
	JSpinner spinner;

	@Mock
	JTabbedPane tabbedPane;

	@Mock
	JTable table;

	@Mock
	JTree tree;

	@Mock
	JTextComponent textComponent;

	@Mock
	JComponent jComponent;

	@Mock
	JDialog dialog;

	@Mock
	JFrame frame;

	@Mock
	Container container;

	@Mock
	Component component;

	@Before
	public void setUp()
	{
		initMocks( this );

		gos = new CameraObjectService().install( new Properties() );
	}

	@SuppressWarnings( "unchecked" )
	private < C > Class< C > adapteeClass( C c )
	{
		return ( Class< C > ) gos.getManager().adapt( c, null ).getClass();
	}

	@Test
	@Ignore
	public void installsDefaultAdapter() throws Exception
	{
		assertEquals( DefaultGuiObject.class, adapteeClass( object ) );
	}

	@Test
	public void installsSnapshotAdapter() throws Exception
	{
		assertEquals( SnapshotGuiObject.class, adapteeClass( new Snapshot() ) );
	}

	@Test
	public void installsSwingAdapters() throws Exception
	{
		assertNotNull( gos.getManager().adapt( this.abstractButton, null ) );
		assertNotNull( gos.getManager().adapt( this.comboBox, null ) );
		assertNotNull( gos.getManager().adapt( this.internalFrame, null ) );
		assertNotNull( gos.getManager().adapt( this.label, null ) );
		assertNotNull( gos.getManager().adapt( this.list, null ) );
		assertNotNull( gos.getManager().adapt( this.slider, null ) );
		assertNotNull( gos.getManager().adapt( this.spinner, null ) );
		assertNotNull( gos.getManager().adapt( this.tabbedPane, null ) );
		assertNotNull( gos.getManager().adapt( this.table, null ) );
		assertNotNull( gos.getManager().adapt( this.tree, null ) );
		assertNotNull( gos.getManager().adapt( this.textComponent, null ) );
		assertNotNull( gos.getManager().adapt( this.jComponent, null ) );
		assertNotNull( gos.getManager().adapt( this.dialog, null ) );
		assertNotNull( gos.getManager().adapt( this.frame, null ) );
		assertNotNull( gos.getManager().adapt( this.container, null ) );
		assertNotNull( gos.getManager().adapt( this.component, null ) );
	}

	@Test
	public void assignsSwingAdapters() throws Exception
	{
		assertEquals( AbstractButtonGuiObject.class, adapteeClass( abstractButton ) );
		assertEquals( JComboBoxGuiObject.class, adapteeClass( comboBox ) );
		assertEquals( JInternalFrameGuiObject.class, adapteeClass( internalFrame ) );
		assertEquals( JLabelGuiObject.class, adapteeClass( label ) );
		assertEquals( JListGuiObject.class, adapteeClass( list ) );
		assertEquals( JSliderGuiObject.class, adapteeClass( slider ) );
		assertEquals( JSpinnerGuiObject.class, adapteeClass( spinner ) );
		assertEquals( JTabbedPaneGuiObject.class, adapteeClass( tabbedPane ) );
		assertEquals( JTableGuiObject.class, adapteeClass( table ) );
		assertEquals( JTreeGuiObject.class, adapteeClass( tree ) );
		assertEquals( JTextComponentGuiObject.class, adapteeClass( textComponent ) );
		assertEquals( JComponentGuiObject.class, adapteeClass( jComponent ) );
		assertEquals( JDialogGuiObject.class, adapteeClass( dialog ) );
		assertEquals( JFrameGuiObject.class, adapteeClass( frame ) );
		assertEquals( ContainerGuiObject.class, adapteeClass( container ) );
		assertEquals( ComponentGuiObject.class, adapteeClass( component ) );
	}

}