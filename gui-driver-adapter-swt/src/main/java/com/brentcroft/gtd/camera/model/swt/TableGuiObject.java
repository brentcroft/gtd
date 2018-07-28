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

import javax.swing.table.TableModel;

import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
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
public class TableGuiObject< T extends org.eclipse.swt.widgets.Table > extends ControlGuiObject< T >
        implements GuiObject.Table
{
    public TableGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
            CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }

    @Override
    public void buildProperties( Element element, Map< String, Object > options )
    {
        super.buildProperties( element, options );

        buildTableModel( element, options );
        addTableAction( element, options );
    }


    private void buildTableModel( Element element, Map< String, Object > options )
    {
        Element modelElement = element.getOwnerDocument().createElement( MODEL_TAG );
        element.appendChild( modelElement );

        modelElement.setAttribute( "type", "table" );

        maybeSetElementAttribute( modelElement, "rows", getObject().getItemCount() );
        maybeSetElementAttribute( modelElement, "columns", getObject().getColumnCount() );
        maybeSetElementAttribute( modelElement, "selected-row",
                ( getObject().getSelectionIndex() == - 1 )
                        ? null
                        : getObject().getSelectionIndex() );

//        maybeSetElementAttribute( modelElement, "selected-column",
//                ( getObject().getSelectedColumn() == - 1 )
//                        ? null
//                        : getObject().getSelectedColumn() );

        if ( ! GuiObject.isShallow( options ) )
        {

            int rowCount = DataLimit
                    .MAX_TABLE_ROWS
                    .getMin( getObject().getItemCount(), options );


            int colCount = DataLimit
                    .MAX_TABLE_COLUMNS
                    .getMin( getObject().getColumnCount(), options );


            for ( int i = 0, n = rowCount; i < n; i++ )
            {
                Element rowElement = modelElement.getOwnerDocument().createElement( "r" );

                rowElement.setAttribute( "index", "" + i );

                modelElement.appendChild( rowElement );

                TableItem[] tableModel = getObject().getItems();

                for ( int j = 0; j < tableModel.length; j++ )
                {
                    Element cellElement = rowElement.getOwnerDocument().createElement( "c" );

                    cellElement.setAttribute( "index", "" + j );

                    rowElement.appendChild( cellElement );

                    Object value = tableModel[j].getText();

                    maybeSetElementAttribute( cellElement, "text", format( "[%s]%s", maybeGetValueType( value ), maybeConvertValue( value ) ) );
                }
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
    static enum Attr implements AttrSpec< org.eclipse.swt.widgets.Table >
    {
        // TEXT( "text", go -> go.getItem( go.getS ) ),
        SIZE( "size", go -> "" + go.getItemCount() ),
        SELECTED_INDEX( "selected-index", go -> "" + go.getSelectionIndex() );

        final String n;
        final Function< org.eclipse.swt.widgets.Table, String > f;

        Attr( String name, Function< org.eclipse.swt.widgets.Table, String > f )
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
        public String getAttribute( org.eclipse.swt.widgets.Table go )
        {
            return f.apply( go );
        }
    }

    @Override
    public void selectRow( int row )
    {
        SwtSnapshot.getDisplay().syncExec( () -> getObject().select( row ) );
    }

    @Override
    public void selectColumn( final int column )
    {
        SwtSnapshot.getDisplay().syncExec( () -> getObject().select( column ) );
    }

    @Override
    public void selectCell( int row, int column )
    {
        
    }
}
