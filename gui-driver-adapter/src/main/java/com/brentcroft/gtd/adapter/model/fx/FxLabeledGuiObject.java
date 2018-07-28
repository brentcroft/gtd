package com.brentcroft.gtd.adapter.model.fx;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import javafx.scene.control.Labeled;

import static java.util.Optional.ofNullable;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxLabeledGuiObject< T extends Labeled > extends FxNodeGuiObject< T >
{
    public FxLabeledGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
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

    enum Attr implements AttrSpec< Labeled >
    {
        TEXT( "text", go -> ofNullable( go.getText() ).orElse( null ) );

        final String n;
        final Function< Labeled, String > f;

        Attr( String name, Function< Labeled, String > f )
        {
            this.n = name;
            this.f = f;
        }

        public String getName()
        {
            return n;
        }

        @Override
        public String getAttribute( Labeled go )
        {
            return f.apply( go );
        }
    }
}
