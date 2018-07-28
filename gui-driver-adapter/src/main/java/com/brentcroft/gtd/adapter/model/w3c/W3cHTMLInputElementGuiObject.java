package com.brentcroft.gtd.adapter.model.w3c;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Attribute;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLInputElement;

import static java.lang.String.format;

/**
 * Created by Alaric on 15/07/2017.
 */
public class W3cHTMLInputElementGuiObject< T extends HTMLInputElement > extends W3cHTMLElementGuiObject< T > implements GuiObject.Text, GuiObject.Click
{
    private final static transient Logger logger = Logger.getLogger( W3cHTMLInputElementGuiObject.class );

    enum InputType
    {
        hidden, text, password, submit, reset, radio, checkbox, button, file,
        color, date, datetime_local,
        email, month, number, range, search, tel, time, url, week,
        unknown;

        static InputType getInputType( String type )
        {
            try
            {
                return InputType.valueOf( type );
            }
            catch ( IllegalArgumentException e )
            {
                logger.warn( format( "Unknown InputType [%s].", type ) );

                return unknown;
            }
        }
    }

    public W3cHTMLInputElementGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }

    @Override
    public List< Attribute > readAttributes()
    {
        final String attrValue = "value";

        List< Attribute > attributes = super.readAttributes();

        // remove any existing attribute
        attributes
                .stream()
                .filter( attribute -> attrValue.equals( attribute.getName() ) )
                .findFirst()
                .ifPresent( attribute -> attributes.remove( attribute ) );

        attributes.add( attributeForNameAndValue( attrValue, getObject().getValue() ) );

        return attributes;
    }


    @Override
    public void buildProperties( Element element, Map< String, Object > options )
    {
        super.buildProperties( element, options );

        switch ( InputType.getInputType( getObject().getType() ) )
        {
            case submit:
            case reset:
            case radio:
            case checkbox:
            case button:
                addClickAction( element, options );
                break;

            case text:
            case password:
            case hidden:

                addClickAction( element, options );
                addTextAction( element, options );
                break;

            default:
                // but don't extract text
                addClickAction( element, options );
                addTextAction( element, options );
                break;
        }
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


    @Override
    public void click()
    {
        FXUtils.maybeInvokeNowOnFXThread( () -> getObject().click() );
    }
}
