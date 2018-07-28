package com.brentcroft.gtd.adapter.model.swing;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.RobotUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Map;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import org.w3c.dom.Element;

import static com.brentcroft.gtd.adapter.model.DefaultGuiObject.Converter.maybeConvertValue;
import static com.brentcroft.gtd.adapter.model.DefaultGuiObject.Converter.maybeGetValueType;
import static com.brentcroft.gtd.adapter.model.swing.JComboBoxGuiObject.MODEL_TAG;
import static com.brentcroft.util.XmlUtils.maybeSetElementAttribute;
import static java.lang.String.format;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JTableGuiObject< T extends JTable > extends JComponentGuiObject< T > implements GuiObject.Table
{
    public JTableGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
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

        maybeSetElementAttribute( modelElement, "rows", getObject().getRowCount() );
        maybeSetElementAttribute( modelElement, "columns", getObject().getColumnCount() );
        maybeSetElementAttribute( modelElement, "selected-row",
                ( getObject().getSelectedRow() == - 1 )
                        ? null
                        : getObject().getSelectedRow() );

        maybeSetElementAttribute( modelElement, "selected-column",
                ( getObject().getSelectedColumn() == - 1 )
                        ? null
                        : getObject().getSelectedColumn() );

        if ( ! GuiObject.isShallow( options ) )
        {

            int rowCount = DataLimit
                    .MAX_TABLE_ROWS
                    .getMin( getObject().getRowCount(), options );


            int colCount = DataLimit
                    .MAX_TABLE_COLUMNS
                    .getMin( getObject().getColumnCount(), options );


            for ( int i = 0, n = rowCount; i < n; i++ )
            {
                Element rowElement = modelElement.getOwnerDocument().createElement( "r" );

                rowElement.setAttribute( "index", "" + i );

                modelElement.appendChild( rowElement );

                TableModel tableModel = getObject().getModel();

                for ( int j = 0; j < colCount; j++ )
                {
                    Element cellElement = rowElement.getOwnerDocument().createElement( "c" );

                    cellElement.setAttribute( "index", "" + j );

                    rowElement.appendChild( cellElement );

                    Object value = tableModel.getValueAt( i, j );

                    maybeSetElementAttribute( cellElement, "text", format( "[%s]%s", maybeGetValueType( value ), maybeConvertValue( value ) ) );
                }
            }
        }
    }


    @Override
    public void selectRow( int row )
    {
        selectCell( row, 0 );
    }

    @Override
    public void selectColumn( int column )
    {
        selectCell( 0, column );
    }

    @Override
    public void selectCell( int row, int column )
    {
        JTable table = getObject();

        Rectangle rect = table.getCellRect(
                table.convertRowIndexToView( row ),
                table.convertColumnIndexToView( column ),
                false );

        table.scrollRectToVisible( rect );

        Point point = new Point(
                rect.x + ( rect.width / 2 ),
                rect.y + ( rect.height / 2 ) );

        SwingUtilities.convertPointToScreen( point, table );

        RobotUtils.awtRobotClickOnPoint( new int[]{ point.x, point.y } );
    }
}
