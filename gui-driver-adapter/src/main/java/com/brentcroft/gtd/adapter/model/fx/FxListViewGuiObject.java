package com.brentcroft.gtd.adapter.model.fx;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.scene.control.ListView;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxListViewGuiObject< T extends ListView< ? > > extends FxControlGuiObject< T > implements GuiObject.Index
{
	public FxListViewGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public boolean hasChildren()
	{
		return false;
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		List< GuiObject< ? > > children = super.loadChildren();

		getObject().getItems().forEach( child -> children.add( getManager().adapt( child, this ) ) );

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

	@SuppressWarnings( "unchecked" )
	@Override
	public List< AttrSpec< T > > loadAttrSpec()
	{
		if ( attrSpec == null )
		{
			attrSpec = super.loadAttrSpec();
			attrSpec.addAll( Arrays.asList( ( AttrSpec< T >[] ) Attr.values() ) );
		}

		return attrSpec;
	}

	enum Attr implements AttrSpec< ListView< ? > >
	{
		SIZE( "size", go -> "" + go.getItems().size() ),
		SELECTED( "selected-index", go -> "" + go.getSelectionModel().getSelectedIndex() );

		final String n;
		final Function< ListView< ? >, String > f;

		Attr( String name, Function< ListView< ? >, String > f )
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
		public String getAttribute( ListView< ? > go )
		{
			return f.apply( go );
		}
	}

}
