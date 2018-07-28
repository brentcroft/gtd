package com.brentcroft.gtd.adapter.model.fx;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javafx.scene.control.TextInputControl;
import org.w3c.dom.Element;

import static java.util.Optional.ofNullable;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxTextInputControlGuiObject< T extends TextInputControl > extends FxControlGuiObject< T > implements GuiObject.Text
{
    public FxTextInputControlGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }


    @Override
    public void buildProperties( Element element, Map< String, Object > options )
    {
        super.buildProperties( element, options );

        addTextAction( element, options );
    }


    @Override
    public String getText()
    {
        return getObject().getText();
    }

    @Override
    public void setText( String text )
    {
        FXUtils.maybeInvokeNowOnFXThread( () -> getObject().setText( text ) );
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

    enum Attr implements AttrSpec< TextInputControl >
    {
        TEXT( "text", go -> ofNullable( DataLimit.MAX_TEXT_LENGTH.maybeTruncate( go.getText() ) ).orElse( null ) );

        final String n;
        final Function< TextInputControl, String > f;

        Attr( String name, Function< TextInputControl, String > f )
        {
            this.n = name;
            this.f = f;
        }

        public String getName()
        {
            return n;
        }

        @Override
        public String getAttribute( TextInputControl go )
        {
            return f.apply( go );
        }
    }

}
