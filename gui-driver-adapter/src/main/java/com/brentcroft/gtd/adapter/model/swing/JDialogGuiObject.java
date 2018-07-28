package com.brentcroft.gtd.adapter.model.swing;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javax.swing.JDialog;

import static java.util.Optional.ofNullable;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JDialogGuiObject< T extends JDialog > extends ContainerGuiObject< T >
{
    public JDialogGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
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

    enum Attr implements AttrSpec< JDialog >
    {
        TITLE( "title", go -> ofNullable( go.getTitle() ).orElse( null ) );

        final String n;
        final Function< JDialog, String > f;

        Attr( String name, Function< JDialog, String > f )
        {
            this.n = name;
            this.f = f;
        }

        public String getName()
        {
            return n;
        }

        @Override
        public String getAttribute( JDialog go )
        {
            return f.apply( go );
        }
    }
}
