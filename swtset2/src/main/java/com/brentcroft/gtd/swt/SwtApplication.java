package com.brentcroft.gtd.swt;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class SwtApplication
{
	private static final Logger logger = Logger.getLogger( SwtApplication.class
			.getName() );

	public static final String buttonName = "Abort";
	public static final String buttonXPath = format( "//Button[ @text='%s' ]",
			buttonName );

	public static final String initialText = "text01";
	public static final String finalText = "text02";
	public static final String textXPath = format( "//Text" );

	public final Display[] display = { null };
	public final Shell[] shell = { null };

	private final AtomicBoolean started = new AtomicBoolean( false );

	public static final String[] fruit = { "apple", "pear", "banana", "orange",
			"damson", "greengage", "mango", "plum", "fig" };

	private String randomFruit()
	{
		return fruit[ new Random().nextInt( fruit.length ) ];
	}

	private void shutdown()
	{
		if ( !shell[ 0 ].isDisposed() )
		{
			shell[ 0 ].dispose();
		}
		else
		{
			logger.warn( "Shutdown: Shell already disposed" );
		}
	}

	public void kill()
	{
		if ( !display[ 0 ].isDisposed() )
		{
			display[ 0 ].asyncExec( () -> shutdown() );
		}
		else
		{
			logger.warn( "Kill: Display already disposed" );
		}
	}


	public void run()
	{
		display[ 0 ] = new Display();
		shell[ 0 ] = new Shell( display[ 0 ], SWT.ON_TOP );

		shell[ 0 ].setText( "SWT Application Example" );

		shell[ 0 ].setLayout( new FillLayout() );

		shell[ 0 ].setBounds( 400, 400, 800, 600 );

		buildTabFolder( shell[ 0 ], createNamedControls() );

		shell[ 0 ].addMouseListener( new TestMouseAdapter( "shell" ) );

		shell[ 0 ].pack();

		shell[ 0 ].open();

		while ( !shell[ 0 ].isDisposed() )
		{
			if ( !display[ 0 ].readAndDispatch() )
			{
				display[ 0 ].sleep();
			}
			if ( !started.get() )
			{
				started.set( true );
			}
		}

		started.set( false );

		display[ 0 ].close();

		logger.info( "Finished." );
	}
	

	interface NamedControl
	{
		String getName();

		Control getControl( Composite parent );
	}

	private void buildTabFolder( Shell parent, ArrayList< Function< TabFolder, NamedControl > > fns )
	{
		TabFolder folder = new TabFolder( parent, SWT.NONE );

		for ( Function< TabFolder, NamedControl > fn : fns )
		{
			NamedControl nc = fn.apply( folder );

			TabItem tab1 = new TabItem( folder, SWT.NONE );
			tab1.setText( nc.getName() );
			tab1.setControl( nc.getControl( folder ) );
		}
	}

	private ArrayList< Function< TabFolder, NamedControl > > createNamedControls()
	{
		ArrayList< Function< TabFolder, NamedControl > > list = new ArrayList<>();

		list.add( parent -> new NamedControl()
		{

			@Override
			public String getName()
			{
				return "Sample Panel 01";
			}

			@Override
			public Control getControl( Composite parent )
			{
				return buildSamplePanel01( parent );
			}
		} );

		list.add( parent -> new NamedControl()
		{

			@Override
			public String getName()
			{
				return "Sample Panel 02";
			}

			@Override
			public Control getControl( Composite parent )
			{
				return buildSamplePanel02( parent );
			}
		} );

		return list;

	}

	public boolean isStarted()
	{
		return started.get();
	}

	class TestMouseAdapter extends MouseAdapter
	{
		final String name;

		TestMouseAdapter( String name )
		{
			this.name = name;
		}

		public void mouseDown( MouseEvent e )
		{
			logger.info( format( "%s: mouse-down: %s", name, e ) );
		}

		public void mouseUp( MouseEvent e )
		{
			logger.info( format( "%s: mouse-up: %s", name, e ) );
		}
	}

	public static void main( String[] args )
	{
		new SwtApplication().run();
	}

	private Composite buildSamplePanel01( Composite parent )
	{
		// Composite composite = shell[ 0 ];
		Composite composite = new Composite( parent, SWT.EMBEDDED );
		composite.setData( "GUID", "SamplePanel01" );

		{
			ToolBar toolbar = new ToolBar( composite, SWT.HORIZONTAL );
			toolbar.setBounds( new Rectangle( 5, 5, 300, 30 ) );

			for ( int i : new int[] { 0, 1, 2, 3, 4 } )
			{
				ToolItem toolItem = new ToolItem( toolbar, SWT.PUSH );
				toolItem.setText( format( "TI-%d", i ) );
				toolItem.setToolTipText( format( "TI-%d ToolTip", i ) );

				toolItem.addSelectionListener( new SelectionAdapter()
				{
					@Override
					public void widgetSelected( SelectionEvent e )
					{
						logger.info( format( "%s: selected: %s", toolItem.getText(), e ) );
					}
				} );

			}
		}

		{
			Button button = new Button( composite, SWT.PUSH );
			button.setData( "GUID", "bunionButton" );
			button.setText( buttonName );
			button.setBounds( new Rectangle( 50, 50, 200, 30 ) );
			button.pack();

			button.addSelectionListener( new SelectionAdapter()
			{

				@Override
				public void widgetSelected( SelectionEvent e )
				{
					shutdown();
				}
			} );
		}

		{

			Text text = new Text( composite, SWT.NONE );
			text.setData( "GUID", "travisText" );

			text.setText( initialText );
			text.setBounds( new Rectangle( 50, 100, 200, 30 ) );
			text.pack();

			text.addMouseListener( new TestMouseAdapter( "text" ) );
		}

		{
			Label label = new Label( composite, SWT.NONE );
			label.setData( "GUID", "larryLabel" );

			label.setText( "My label" );
			label.setBounds( new Rectangle( 50, 150, 200, 30 ) );
			label.pack();
		}

		{
			Combo combo = new Combo( composite, SWT.EMBEDDED );
			combo.setData( "GUID", "carrieCombo" );
			combo.add( "blue" );
			combo.add( "green" );
			combo.add( "red" );
			combo.add( "yellow" );

			combo.select( 0 );

			combo.setBounds( new Rectangle( 150, 50, 200, 30 ) );
			combo.pack();
		}

		{
			List list = new List( composite, SWT.EMBEDDED );
			list.setData( "GUID", "lucasList" );
			list.setBounds( new Rectangle( 150, 100, 200, 30 ) );

			list.add( "apple" );
			list.add( "banana" );
			list.add( "mango" );
			list.add( "orange" );

			list.select( 0 );

			list.pack();
		}

		{
			Table table = new Table( composite, SWT.MULTI | SWT.BORDER
					| SWT.FULL_SELECTION );
			table.setLinesVisible( true );
			table.setHeaderVisible( true );

			table.setBounds( new Rectangle( 50, 200, 200, 200 ) );
			table.setData( "GUID", "theloniusTable" );

			final String[] titles = { "Sample", "1", "2", "3", "4", "5", "6" };

			for ( int h = 0; h < titles.length; h++ )
			{
				TableColumn column = new TableColumn( table, SWT.NONE );
				column.setText( titles[ h ] );
				column.pack();
			}

			int rowCount = 4;

			for ( int i = 0; i < rowCount; i++ )
			{
				TableItem item = new TableItem( table, SWT.NONE );
				item.setText( 0, "" + i );

				for ( int h = 0; h < titles.length; h++ )
				{
					item.setText( h + 1,

							randomFruit() );
				}
			}

			table.pack();
		}

		{

			final Tree tree = new Tree( composite, SWT.VIRTUAL | SWT.BORDER );
			tree.setSize( 290, 260 );
			tree.setBounds( new Rectangle( 350, 50, 150, 300 ) );
			for ( int loopIndex0 = 0; loopIndex0 < 5; loopIndex0++ )
			{
				TreeItem treeItem0 = new TreeItem( tree, 0 );
				treeItem0.setText(

						format( "%s", randomFruit() ) );
				for ( int loopIndex1 = 0; loopIndex1 < 4; loopIndex1++ )
				{
					TreeItem treeItem1 = new TreeItem( treeItem0, 0 );
					treeItem1.setText( format( "%s", randomFruit() ) );
					for ( int loopIndex2 = 0; loopIndex2 < 3; loopIndex2++ )
					{
						TreeItem treeItem2 = new TreeItem( treeItem1, 0 );
						treeItem2.setText( format( "%s", randomFruit() ) );
					}
				}
			}
			tree.addListener( SWT.SetData, new Listener()
			{
				public void handleEvent( Event event )
				{
					TreeItem item = ( TreeItem ) event.item;
					logger.info( format( "%s: selected: %s", item.getText(), event ) );
				}
			} );
		}

		composite.pack();

		composite.addMouseListener( new TestMouseAdapter( "composite" ) );

		return composite;
	}

	private Composite buildSamplePanel02( Composite parent )
	{
		// Composite composite = shell[ 0 ];
		Composite composite = new Composite( parent, SWT.EMBEDDED );
		composite.setData( "GUID", "SamplePanel02" );

		{
			ToolBar toolbar = new ToolBar( composite, SWT.HORIZONTAL );
			toolbar.setBounds( new Rectangle( 5, 5, 300, 30 ) );

			for ( int i : new int[] { 0, 1, 2, 3, 4 } )
			{
				ToolItem toolItem = new ToolItem( toolbar, SWT.PUSH );
				toolItem.setText( format( "TI-%d", i ) );
				toolItem.setToolTipText( format( "TI-%d ToolTip", i ) );

				toolItem.addSelectionListener( new SelectionAdapter()
				{
					@Override
					public void widgetSelected( SelectionEvent event )
					{
						logger.info( format( "%s: selected: %s", toolItem.getText(), event ) );
					}
				} );

			}
		}

		{

			final Tree tree = new Tree( composite, SWT.VIRTUAL | SWT.BORDER );
			tree.setSize( 300, 300 );
			tree.setBounds( new Rectangle( 0, 50, 300, 300 ) );
			for ( int loopIndex0 = 0; loopIndex0 < 8; loopIndex0++ )
			{
				TreeItem treeItem0 = new TreeItem( tree, 0 );
				treeItem0.setText(

						format( "%s", randomFruit() ) );
				for ( int loopIndex1 = 0; loopIndex1 < 4; loopIndex1++ )
				{
					TreeItem treeItem1 = new TreeItem( treeItem0, 0 );
					treeItem1.setText( format( "%s", randomFruit() ) );
					for ( int loopIndex2 = 0; loopIndex2 < 3; loopIndex2++ )
					{
						TreeItem treeItem2 = new TreeItem( treeItem1, 0 );
						treeItem2.setText( format( "%s", randomFruit() ) );
					}
				}
			}
			tree.addListener( SWT.SetData, new Listener()
			{
				public void handleEvent( Event event )
				{
					TreeItem item = ( TreeItem ) event.item;
					logger.info( format( "%s: selected: %s", item.getText(), event ) );
				}
			} );
		}

		composite.pack();

		composite.addMouseListener( new TestMouseAdapter( "composite" ) );

		return composite;
	}
}
