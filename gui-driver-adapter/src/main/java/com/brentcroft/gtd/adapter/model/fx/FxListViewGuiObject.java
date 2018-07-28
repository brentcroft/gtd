package com.brentcroft.gtd.adapter.model.fx;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import javafx.scene.control.ListView;
import org.w3c.dom.Element;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxListViewGuiObject< T extends ListView > extends FxControlGuiObject< T > implements GuiObject.Index
{
    public FxListViewGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }

    @Override
    public boolean hasChildren()
    {
        return false;
    }

    @Override
    public List< GuiObject > loadChildren()
    {
        List< GuiObject > children = new ArrayList<>();

        getObject()
                .getItems()
                .forEach( child -> children.add( getManager().adapt( child, this ) ) );

        children.addAll( super.loadChildren() );

        return children;
    }

    @Override
    public void buildProperties( Element element, Map< String, Object > options )
    {
        super.buildProperties( element, options );

        addIndexAction( element, options );
    }

    @Override
    public Integer getItemCount()
    {
        return getObject().getItems().size();
    }

    @Override
    public Integer getSelectedIndex()
    {
        return getObject().getSelectionModel().getSelectedIndex();
    }

    @Override
    public void setSelectedIndex( int index )
    {
        FXUtils.maybeInvokeNowOnFXThread( () -> getObject().getSelectionModel().select( index ) );
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

    enum Attr implements AttrSpec< ListView >
    {
        SIZE( "size", go -> "" + go.getItems().size() ),
        SELECTED( "selected-index", go -> "" + go.getSelectionModel().getSelectedIndex() );

        final String n;
        final Function< ListView, String > f;

        Attr( String name, Function< ListView, String > f )
        {
            this.n = name;
            this.f = f;
        }

        public String getName()
        {
            return n;
        }

        @Override
        public String getAttribute( ListView go )
        {
            return f.apply( go );
        }
    }

}
