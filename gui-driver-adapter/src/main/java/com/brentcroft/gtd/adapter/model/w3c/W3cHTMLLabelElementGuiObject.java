package com.brentcroft.gtd.adapter.model.w3c;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLLabelElement;

import static java.util.Optional.ofNullable;


/**
 * Provides a text attribute with the text content.
 *
 * @param <T>
 */
public class W3cHTMLLabelElementGuiObject< T extends HTMLLabelElement > extends W3cHTMLElementGuiObject< T >
{
    public W3cHTMLLabelElementGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }

    /**
     * Only load and adapt HTMLElement children (no text nodes).
     *
     * @return a list of adaptees (GuiObjects)
     */
    @Override
    public List< GuiObject > loadChildren()
    {
        List< GuiObject > children = new ArrayList<>();

        for ( Node child = getObject().getFirstChild(); child != null; child = child.getNextSibling() )
        {
            if ( child instanceof HTMLElement )
            {
                children.add( getManager().adapt( child, this ) );
            }
        }

        return children;
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

    enum Attr implements AttrSpec< HTMLLabelElement >
    {
        TEXT( "text", go -> ofNullable( DataLimit.MAX_TEXT_LENGTH.maybeTruncate( go.getTextContent() ) ).orElse( null ) );

        final String n;
        final Function< HTMLLabelElement, String > f;

        Attr( String name, Function< HTMLLabelElement, String > f )
        {
            this.n = name;
            this.f = f;
        }

        public String getName()
        {
            return n;
        }

        @Override
        public String getAttribute( HTMLLabelElement go )
        {
            return f.apply( go );
        }
    }

}
