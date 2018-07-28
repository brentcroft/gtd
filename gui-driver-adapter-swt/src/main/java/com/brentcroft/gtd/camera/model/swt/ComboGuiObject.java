package com.brentcroft.gtd.camera.model.swt;

import static com.brentcroft.gtd.adapter.model.DefaultGuiObject.Converter.maybeConvertValue;
import static com.brentcroft.gtd.adapter.model.DefaultGuiObject.Converter.maybeGetValueType;
import static com.brentcroft.gtd.adapter.model.swing.JComboBoxGuiObject.MODEL_TAG;
import static com.brentcroft.util.XmlUtils.maybeSetElementAttribute;
import static java.lang.String.format;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.swing.JList;
import javax.swing.ListModel;

import org.eclipse.swt.widgets.Combo;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.camera.SwtSnapshot;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class ComboGuiObject< T extends Combo > extends ControlGuiObject< T > implements GuiObject.Index, GuiObject.Text
{
    public ComboGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
            CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }
    
    @Override
    public void buildProperties( Element element, Map< String, Object > options )
    {
        super.buildProperties( element, options );

        buildListModel( element, options );

        addTextAction( element, options );
        addIndexAction( element, options );
    }
    
    public void buildListModel( Element element, Map< String, Object > options )
    {
        Combo list = getObject();

        // rip the cells into a simple xml structure:
        Element modelElement = element.getOwnerDocument().createElement( MODEL_TAG );
        element.appendChild( modelElement );

        modelElement.setAttribute( "type", "list" );

        String[] model = list.getItems();


        if ( ! GuiObject.isShallow( options ) )
        {

            int itemCount = DataLimit
                    .MAX_LIST_DEPTH
                    .getMin( model.length, options );


            for ( int i = 0, n = itemCount; i < n; i++ )
            {
                Object value = model[i];

                Element cellElement = modelElement.getOwnerDocument().createElement( "c" );

                cellElement.setAttribute( "index", "" + i );

                modelElement.appendChild( cellElement );

                maybeSetElementAttribute( cellElement, "text", format( "[%s]%s", maybeGetValueType( value ), maybeConvertValue( value ) ) );
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

    // "disabled", "visible", "focus"
    static enum Attr implements AttrSpec< Combo >
    {
        TEXT( "text", go -> go.getText() ),
        SIZE( "size", go -> "" + go.getItemCount() ),
        SELECTED_INDEX( "selected-index", go -> "" + go.getSelectionIndex() );

        final String n;
        final Function< Combo, String > f;

        Attr( String name, Function< Combo, String > f )
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
        public String getAttribute( Combo go )
        {
            return f.apply( go );
        }
    }

    @Override
    public Integer getItemCount()
    {
        AtomicInteger ai = new AtomicInteger();
        SwtSnapshot.getDisplay().syncExec( () -> ai.set( getObject().getItemCount() ) );
        return ai.get();
    }

    @Override
    public Integer getSelectedIndex()
    {
        AtomicInteger ai = new AtomicInteger();
        SwtSnapshot.getDisplay().syncExec( () -> ai.set( getObject().getSelectionIndex() ) );
        return ai.get();
    }

    @Override
    public void setSelectedIndex( final int index )
    {
        SwtSnapshot.getDisplay().syncExec( () -> getObject().select( index ) );
    }

    @Override
    public String getText()
    {
        String[] ai = {null};
        SwtSnapshot.getDisplay().syncExec( () -> ai[0] = getObject().getText() );
        return ai[0];
    }

    @Override
    public void setText( final String text )
    {
        SwtSnapshot.getDisplay().syncExec( () -> getObject().setText( text ) );
    }
}
