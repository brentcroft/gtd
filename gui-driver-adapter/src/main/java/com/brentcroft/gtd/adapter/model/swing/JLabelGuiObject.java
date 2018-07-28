package com.brentcroft.gtd.adapter.model.swing;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javax.swing.JLabel;

import static com.brentcroft.gtd.driver.Backend.HASH_FOR_ATTRIBUTE;
import static java.util.Optional.ofNullable;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JLabelGuiObject< T extends JLabel > extends JComponentGuiObject< T >
{
    public JLabelGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
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

    enum Attr implements AttrSpec< JLabel >
    {
        TEXT( "text", go -> ofNullable( go.getText() )
                .orElse( null ) ),

        ICON( "icon", go -> ofNullable( go.getIcon() )
                .map( JComponentGuiObject::getPathyModel )
                .orElse( null ) ),

        FOR_HASH( HASH_FOR_ATTRIBUTE, go -> ofNullable( go.getLabelFor() )
                .map( component -> Integer.toString( component.hashCode() ) )
                .orElse( null ) );

        final String n;
        final Function< JLabel, String > f;

        Attr( String name, Function< JLabel, String > f )
        {
            this.n = name;
            this.f = f;
        }

        public String getName()
        {
            return n;
        }

        @Override
        public String getAttribute( JLabel go )
        {
            return f.apply( go );
        }
    }
}
