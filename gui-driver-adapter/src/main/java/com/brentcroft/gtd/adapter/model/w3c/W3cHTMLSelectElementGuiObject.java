package com.brentcroft.gtd.adapter.model.w3c;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.html.HTMLSelectElement;

import static com.brentcroft.gtd.adapter.model.DefaultGuiObject.Converter.maybeConvertValue;
import static com.brentcroft.gtd.adapter.model.DefaultGuiObject.Converter.maybeGetValueType;
import static com.brentcroft.gtd.adapter.model.swing.JComboBoxGuiObject.MODEL_TAG;
import static com.brentcroft.util.XmlUtils.maybeSetElementAttribute;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

/**
 * Created by Alaric on 15/07/2017.
 */
public class W3cHTMLSelectElementGuiObject< T extends HTMLSelectElement > extends W3cHTMLElementGuiObject< T > implements GuiObject.Index, GuiObject.Click
{
    public W3cHTMLSelectElementGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }

    @Override
    public void buildProperties( Element element, Map< String, Object > options )
    {
        super.buildProperties( element, options );

        //buildSelectModel( element, options );

        addIndexAction( element, options );
    }

    public Integer getItemCount()
    {
        return getObject().getLength();
    }

    public Integer getSelectedIndex()
    {
        return getObject().getSelectedIndex();
    }

    public void setSelectedIndex( final int index )
    {
        FXUtils.maybeInvokeNowOnFXThread( () -> getObject().setSelectedIndex( index ) );
    }


    @Override
    public void click()
    {
        // NB: unlike HTMLInputElement, HTMLSelectElement doesn't expose a click() method
        FXUtils.reflectiveCallRunAndWait( getObject(), "click" );
    }


    public void buildSelectModel( Element element, Map< String, Object > options )
    {
        HTMLSelectElement list = getObject();

        if ( ! GuiObject.isShallow( options ) )
        {
            // rip the cells into a simple xml structure:
            Element modelElement = element.getOwnerDocument().createElement( MODEL_TAG );
            element.appendChild( modelElement );

            int itemCount = DataLimit
                    .MAX_LIST_DEPTH
                    .getMin( list.getLength(), options );

            for ( int i = 0, n = itemCount; i < n; i++ )
            {
                HTMLOptionElement value = ( HTMLOptionElement ) list.getOptions().item( i );

                Element cellElement = modelElement.getOwnerDocument().createElement( "c" );

                cellElement.setAttribute( "index", "" + i );

                modelElement.appendChild( cellElement );

                maybeSetElementAttribute( cellElement, "text", format( "[%s]%s", maybeGetValueType( value ), maybeConvertValue( value.getValue() ) ) );
            }
        }
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

    enum Attr implements AttrSpec< HTMLSelectElement >
    {
        SELECTED_INDEX( "selected-index", go -> "" + go.getSelectedIndex() ),
        SIZE( "size", go -> "" + go.getChildNodes().getLength() ),
        TEXT( "text", go -> ofNullable( go.getValue() ).orElse( null ) );

        final String n;
        final Function< HTMLSelectElement, String > f;

        Attr( String name, Function< HTMLSelectElement, String > f )
        {
            this.n = name;
            this.f = f;
        }

        public String getName()
        {
            return n;
        }

        @Override
        public String getAttribute( HTMLSelectElement go )
        {
            return f.apply( go );
        }
    }

}
