package com.brentcroft.gtd.adapter.model.w3c;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLTableElement;

/**
 * Created by Alaric on 15/07/2017.
 */
public class W3cHTMLTableGuiObject< T extends HTMLTableElement > extends W3cHTMLElementGuiObject< T > implements GuiObject.Table
{
    private final static transient Logger logger = Logger.getLogger( W3cHTMLTableGuiObject.class );

    public W3cHTMLTableGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }


    @Override
    public boolean hasChildren()
    {
        return getObject().hasChildNodes();
    }

    @Override
    public List< GuiObject > loadChildren()
    {
        List< GuiObject > children = new ArrayList<>();

        for ( Node child = getObject().getFirstChild(); child != null; child = child.getNextSibling() )
        {
            if ( child != null && child instanceof HTMLElement )
            {
                children.add( getManager().adapt( child, this ) );
            }
        }

        return children;
    }

    @Override
    public void selectRow( int row )
    {
        HTMLCollection h = getObject().getRows();

        if ( h == null )
        {
            return;
        }

        Node rowNode = h.item( row );

        // click on row node
        //rowNode.click
    }

    @Override
    public void selectColumn( int column )
    {

    }

    @Override
    public void selectCell( int row, int column )
    {

    }
}
