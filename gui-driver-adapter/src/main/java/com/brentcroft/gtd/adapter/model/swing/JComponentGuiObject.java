package com.brentcroft.gtd.adapter.model.swing;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javax.swing.JComponent;

import static java.util.Optional.ofNullable;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JComponentGuiObject< T extends JComponent > extends ContainerGuiObject< T > implements GuiObject.Click
{
    public JComponentGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }


    /**
     * If there's a backslash in the pathyItem then extracts only characters after the last backslash.
     * <p/>
     * If there's an @ then not to the right (as its probably a dynamic object reference).
     *
     * @param pathyItem
     */

    public static String getPathyModel( Object pathyItem )
    {
        if ( pathyItem == null )
        {
            return null;
        }

        // this might not always be a url
        // but I haven't seen one that isn't
        String path = pathyItem.toString();

        int p = path.lastIndexOf( '/' );

        path = p < 0 && ( ( p + 1 ) < path.length() )
                ? path
                : path.substring( p + 1 );

        p = path.indexOf( '@' );

        return p < 0 || ( ( p + 1 ) >= path.length() )
                ? path
                : path.substring( 0, p );
    }


    @Override
    public void click()
    {
        getObject().requestFocus();
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


    // "text", "size", "selected-index"
    enum Attr implements AttrSpec< JComponent >
    {
        TOOLTIP( "tooltip", go -> ofNullable( go.getToolTipText() ).orElse( null ) );

        final String n;
        final Function< JComponent, String > f;

        Attr( String name, Function< JComponent, String > f )
        {
            this.n = name;
            this.f = f;
        }

        public String getName()
        {
            return n;
        }

        @Override
        public String getAttribute( JComponent go )
        {
            return f.apply( go );
        }
    }
}
