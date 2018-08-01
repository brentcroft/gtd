package com.brentcroft.gtd.adapter.model.fx;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.scene.control.TabPane;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxTabPaneGuiObject< T extends TabPane > extends FxNodeGuiObject< T > implements GuiObject.Index
{
	public FxTabPaneGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager gom )
	{
		super( go, parent, guiObjectConsultant, gom );
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		List< GuiObject< ? > > results = getObject().getTabs().stream().map( child -> getManager().adapt( child, this ) )
				.collect( Collectors.toList() );

		// TODO: justify
		if ( super.hasChildren() )
		{
			results.addAll( super.loadChildren() );
		}

		return results;
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		addIndexAction( element, options );
		addTabsAction( element, options );
	}

	@Override
	public Integer getItemCount()
	{
		return getObject().getTabs().size();
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

	enum Attr implements AttrSpec< TabPane >
	{
		SIZE( "size", go -> "" + go.getTabs().size() ),
		SELECTED( "selected-index", go -> "" + go.getSelectionModel().getSelectedIndex() );

		final String n;
		final Function< TabPane, String > f;

		Attr( String name, Function< TabPane, String > f )
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
		public String getAttribute( TabPane go )
		{
			return f.apply( go );
		}
	}

}
