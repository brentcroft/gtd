package com.brentcroft.gtd.camera;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.util.Waiter8;
import com.brentcroft.util.XmlUtils;

public class SwtCameraTest
{
    Thread swtThread = null;

    SwtCamera camera = new SwtCamera();
    SwtApplication app = new SwtApplication( camera, () -> swtThread = null );

    private Thread getSwtThread()
    {
        return new Thread( () -> app.run() );
    }

    @Before
    public void setUp()
    {
        if ( swtThread == null )
        {
            swtThread = getSwtThread();
        }

        swtThread.start();

        Waiter8.delay( 1 * 1000 );

        assertNotNull( app.display[ 0 ] );

        assertFalse( app.shell[ 0 ].isDisposed() );
    }

    @After
    public void tearDown()
    {
        if ( !app.shell[ 0 ].isDisposed() )
        {
            System.out.println( "tearDown disposing swtThread..." );

            app.display[ 0 ].syncExec( () -> app.shell[ 0 ].dispose() );
        }
    }

    @Test
    public void testButtonExists()
    {
        app.display[ 0 ].syncExec( () -> {
            GuiObject guiObject = camera.getGuiObject( 1000, 5000, app.buttonXPath );

            assertNotNull( guiObject );

            assertEquals( app.buttonName, guiObject.getAttribute( "text" ) );
        } );
    }

    @Test
    public void testButtonClick()
    {
        assertFalse( app.shell[ 0 ].isDisposed() );

        app.display[ 0 ].syncExec( () -> {

            GuiObject guiObject = camera.getGuiObject( 1000, 5000, app.buttonXPath );

            assertNotNull( guiObject );

            System.out.println( XmlUtils.serialize( camera.takeSnapshot( guiObject.getObject(), null ) ) );

            guiObject.asClick().click();
        } );

        Waiter8.delay( 1 * 1000 );

        new Waiter8()
                .withTimeoutMillis( 100 )
                .onTimeout( timeout -> assertTrue( app.shell[ 0 ].isDisposed() ) )
                .until( () -> app.shell[ 0 ].isDisposed() );
    }

    @Test
    public void testTextExists()
    {
        app.display[ 0 ].syncExec( () -> {
            GuiObject guiObject = camera.getGuiObject( 1000, 5000, "//Text" );
            
            assertNotNull( guiObject );

            assertEquals( app.initialText, guiObject.asText().getText() );

            guiObject.asText().setText( app.finalText );

            assertEquals( app.finalText, guiObject.asText().getText() );
        } );
    }
    
    
    @Test
    public void testLabelExists()
    {
        app.display[ 0 ].syncExec( () -> {
            GuiObject guiObject = camera.getGuiObject( 1000, 5000, "//Label" );
            
            assertNotNull( guiObject );
        } );
    }
    

    @Test
    public void testComboExists()
    {
        Integer index = 2;

        app.display[ 0 ].syncExec( () -> {
            GuiObject guiObject = camera.getGuiObject( 1000, 5000, "//Combo" );

            guiObject.asIndex().setSelectedIndex( index );

            assertEquals( index, guiObject.asIndex().getSelectedIndex() );

            System.out.println( XmlUtils.serialize( camera.takeSnapshot( guiObject.getObject(), null ) ) );
        } );
    }

    @Test
    public void testComboText()
    {
        String text = "magnolia";

        app.display[ 0 ].syncExec( () -> {
            GuiObject guiObject = camera.getGuiObject( 1000, 5000, "//Combo" );

            guiObject.asText().setText( text );

            assertEquals( text, guiObject.asText().getText() );

            System.out.println( XmlUtils.serialize( camera.takeSnapshot( guiObject.getObject(), null ) ) );
        } );
    }

    @Test
    public void testListExists()
    {
        Integer index = 2;

        app.display[ 0 ].syncExec( () -> {
            GuiObject guiObject = camera.getGuiObject( 1000, 5000, "//List" );

            guiObject.asIndex().setSelectedIndex( index );

            assertEquals( index, guiObject.asIndex().getSelectedIndex() );

            System.out.println( XmlUtils.serialize( camera.takeSnapshot( guiObject.getObject(), null ) ) );
        } );
    }

    @Test
    public void testTableExists()
    {
        Integer index = 2;

        app.display[ 0 ].syncExec( () -> {
            GuiObject guiObject = camera.getGuiObject( 1000, 5000, "//Table" );

            guiObject.asTable().selectRow( index );

            System.out.println( XmlUtils.serialize( camera.takeSnapshot( guiObject.getObject(), null ) ) );
        } );
    }
}