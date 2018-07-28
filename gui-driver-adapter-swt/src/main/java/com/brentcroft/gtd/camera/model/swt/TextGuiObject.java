package com.brentcroft.gtd.camera.model.swt;

import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;


import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.camera.SwtSnapshot;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 * 
 */
// using: org.eclipse.swt.widgets.Text avoid clash
public class TextGuiObject< T extends org.eclipse.swt.widgets.Text > extends ControlGuiObject< T > implements GuiObject.Text
{
    public TextGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
            CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }

    public String getText()
    {
        final String[] text = { null };
        SwtSnapshot.getDisplay().syncExec( () -> {
            text[ 0 ] = getObject().getText();
        } );
        return text[ 0 ];
    }

    public void setText( final String text )
    {
        SwtSnapshot.getDisplay().syncExec( () -> {
            getObject().setText( text );
        } );
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
    enum Attr implements AttrSpec< org.eclipse.swt.widgets.Text >
    {
        TEXT( "text", go -> ofNullable( DataLimit.MAX_TEXT_LENGTH.maybeTruncate( go.getText() ) ).orElse( null ) );

        final String n;
        final Function< org.eclipse.swt.widgets.Text, String > f;

        Attr( String name, Function< org.eclipse.swt.widgets.Text, String > f )
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
        public String getAttribute( org.eclipse.swt.widgets.Text go )
        {
            return f.apply( go );
        }
    }
}
