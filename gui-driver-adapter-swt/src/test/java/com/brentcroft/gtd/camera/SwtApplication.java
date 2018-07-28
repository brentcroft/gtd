package com.brentcroft.gtd.camera;

import static java.lang.String.format;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.brentcroft.util.XmlUtils;

public class SwtApplication
{
    
    public String buttonName = "Abort";
    public String buttonXPath = format( "//Button[ @text='%s' ]", buttonName );

    public String initialText = "text01";
    public String finalText = "text02";
    
    public final Display[] display = { null };
    public final Shell[] shell = { null };    
    
    private final Camera camera;
    private final OnShutdown onShutdown;
    
    interface OnShutdown
    {
        void onShutdown();
    }
    
    
    public SwtApplication(Camera camera, OnShutdown onShutdown)
    {
        this.camera = camera;
        this.onShutdown= onShutdown;
    }
    
    
    public void run(  )
    {
        display[ 0 ] = new Display();
        shell[ 0 ] = new Shell( display[ 0 ] );

        shell[ 0 ].setBounds( 400, 400, 400, 400 );

        // Composite composite = shell[ 0 ];
        Composite composite = new Composite( shell[ 0 ], SWT.EMBEDDED );

        Button button = new Button( composite, SWT.PUSH );
        button.setText( buttonName );
        button.setBounds( new Rectangle( 50, 500, 200, 30 ) );
        button.pack();

        Text text = new Text( composite, SWT.NONE );

        text.setText( initialText );
        text.setBounds( new Rectangle( 50, 100, 200, 30 ) );
        text.pack();

        
        Label label = new Label( composite, SWT.NONE );

        label.setText( "My label" );
        label.setBounds( new Rectangle( 150, 100, 200, 30 ) );
        label.pack();

        
        Combo combo = new Combo( composite, SWT.EMBEDDED );

        combo.add( "blue" );
        combo.add( "green" );
        combo.add( "red" );
        combo.add( "yellow" );

        combo.select( 0 );

        combo.setBounds( new Rectangle( 50, 200, 200, 30 ) );
        combo.pack();

        List list = new List( composite, SWT.EMBEDDED );

        list.add( "apple" );
        list.add( "banana" );
        list.add( "mango" );
        list.add( "orange" );

        list.select( 0 );

        list.setBounds( new Rectangle( 50, 300, 200, 30 ) );
        list.pack();

        Table table = new Table( composite, SWT.EMBEDDED );
        table.setItemCount( 2 );
        for ( int i = 0; i < 5; i++ )
        {
            TableItem item = new TableItem( table, SWT.EMBEDDED, i );
            int index = table.indexOf( item );
            item.setText( "Item " + index );
        }
        table.pack();

        composite.pack();

        button.addSelectionListener( new SelectionAdapter() {

            @Override
            public void widgetSelected( SelectionEvent e )
            {
                System.out.println( "button: button-selected: " + e );
                shell[ 0 ].dispose();
            }
        } );

        text.addMouseListener( new TestMouseAdapter( "text" ) );

        composite.addMouseListener( new TestMouseAdapter( "composite" ) );

        shell[ 0 ].addMouseListener( new TestMouseAdapter( "shell" ) );

        shell[ 0 ].pack();

        shell[ 0 ].open();

        System.out.println( XmlUtils.serialize( camera.takeSnapshot() ) );

        while ( !shell[ 0 ].isDisposed() )
        {
            if ( !display[ 0 ].readAndDispatch() )
            {
                display[ 0 ].sleep();
            }
        }

        display[ 0 ].dispose();
        
        onShutdown.onShutdown();
        
        System.out.println( "swtThread finished." );       
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
            System.out.println( format( "%s: mouse-down: %s", name, e ) );
            // shell[ 0 ].dispose();
        }

        public void mouseUp( MouseEvent e )
        {
            System.out.println( format( "%s: mouse-up: %s", name, e ) );
            // shell[ 0 ].dispose();
        }
    }
}
