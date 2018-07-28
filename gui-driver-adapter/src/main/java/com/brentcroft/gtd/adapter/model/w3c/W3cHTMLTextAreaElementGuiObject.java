package com.brentcroft.gtd.adapter.model.w3c;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLTextAreaElement;

/**
 * Created by Alaric on 15/07/2017.
 */
public class W3cHTMLTextAreaElementGuiObject< T extends HTMLTextAreaElement > extends W3cHTMLElementGuiObject< T > implements GuiObject.Text
{
    public W3cHTMLTextAreaElementGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
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
        return getObject().getValue();
    }

    @Override
    public void setText( String text )
    {
        FXUtils.maybeInvokeNowOnFXThread( () -> getObject().setValue( text ) );
    }
}
