package com.brentcroft.gtd.camera.model.swt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.DefaultGuiObject;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.RobotUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.camera.SwtSnapshot;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class ControlGuiObject< T extends Control > extends DefaultGuiObject< T > implements GuiObject.Click
{
    private static Logger logger = Logger.getLogger( ControlGuiObject.class );
    
    public ControlGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
            CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }

    @Override
    public void click()
    {
        getObject().setFocus();
        
        if ( getObject().isFocusControl() )
        {
            RobotUtils.awtRobotClickOnPoint( getObjectLocationOnScreen() );
        }
        else
        {
            logger.warn( "Object is not in focus" );
        }
    }

    @Override
    public int[] getLocation()
    {
        Rectangle p = getObject().getBounds();
        return new int[] { p.x, p.y, p.width, p.height };
    }

    @Override
    public int[] getObjectLocationOnScreen()
    {
        int[] r = getLocation();
        
        int centreX = r[ 2 ] / 2;
        int centreY = r[ 3 ] / 2;
        
        Point p = getObject().toDisplay( centreX, centreY );

        return new int[] { p.x, p.y };
    }

    //
    // @Override
    // public void robotClick()
    // {
    // RobotUtils.awtRobotClick( getObjectMidpointLocationOnScreen() );
    // }
    //
    //
    // @Override
    // public void robotDoubleClick()
    // {
    // RobotUtils.awtRobotDoubleClick( getObjectMidpointLocationOnScreen() );
    // }
    //
    //
    // @Override
    // public void robotKeys( String keys )
    // {
    // if ( ! getObject().isVisible() )
    // {
    // logger.warn( "Object is not visible." );
    // }
    // else if ( ! getObject().isShowing() )
    // {
    // logger.warn( "Object is not showing." );
    // }
    //
    // RobotUtils
    // .awtRobotKeys(
    // getObjectMidpointLocationOnScreen(),
    // () -> getObject().hasFocus(),
    // keys
    // );
    // }

    @Override
    public void buildProperties( Element element, Map< String, Object > options )
    {
        super.buildProperties( element, options );

        // every Control is clickable
        addClickAction( element, options );
    }

    @Override
    public List< AttrSpec > loadAttrSpec()
    {
        if ( attrSpec == null )
        {
            attrSpec = super.loadAttrSpec();
            attrSpec.addAll( Arrays.asList( Attr.values() ) );
        }

        return attrSpec;
    }

    // "disabled", "visible", "focus"
    enum Attr implements AttrSpec< Control >
    {
        DISABLED( "disabled", go -> go.isEnabled() ? "" : "true" ),

        VISIBLE( "visible", go -> go.isVisible() ? "true" : null ),

        FOCUS( "focus", go -> go.isFocusControl() ? "true" : null );

        final String n;
        final Function< Control, String > f;

        Attr( String name, Function< Control, String > f )
        {
            this.n = name;
            this.f = f;
        }

        @Override
        public String getName()
        {
            return n;
        }

        @Override
        public String getAttribute( Control go )
        {
            return f.apply( go );
        }
    }
}
