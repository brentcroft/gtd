package com.brentcroft.gtd.camera.model.swt;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.widgets.MenuItem;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.DefaultGuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 * 
 */
public class MenuItemGuiObject< T extends MenuItem > extends DefaultGuiObject< T >
{
    public MenuItemGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
            CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
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


    static enum Attr implements AttrSpec< MenuItem >
    {
        TEXT( "text", go -> go.getText() );

        final String n;
        final Function< MenuItem, String > f;

        Attr( String name, Function< MenuItem, String > f )
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
        public String getAttribute( MenuItem go )
        {
            return f.apply( go );
        }
    }

}
