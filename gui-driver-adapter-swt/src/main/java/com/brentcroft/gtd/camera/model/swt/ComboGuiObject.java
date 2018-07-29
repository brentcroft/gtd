package com.brentcroft.gtd.camera.model.swt;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.eclipse.swt.widgets.Combo;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class ComboGuiObject< T extends Combo > extends CompositeGuiObject< T >
		implements GuiObject.Index, GuiObject.Text
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

		appendListModelElement(
				element,
				options,
				getItemCount(),
				onDisplayThread( getObject(), go -> go.getItems() ) );

		addTextAction( element, options );
		addIndexAction( element, options );
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

	// "disabled", "visible", "focus"
	static enum Attr implements AttrSpec< Combo >
	{
		TEXT( "text", go -> go.getText() ),
		SIZE( "size", go -> go.getItemCount() ),
		SELECTED_INDEX( "selected-index", go -> go.getSelectionIndex() );

		final String n;
		final Function< Combo, Object > f;

		Attr( String name, Function< Combo, Object > f )
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
			return onSwtDisplayThreadAsText( go, f );
		}
	}

	@Override
	public Integer getItemCount()
	{
		return onDisplayThread( getObject(), go -> go.getItemCount() );
	}

	@Override
	public Integer getSelectedIndex()
	{
		return onDisplayThread( getObject(), go -> go.getSelectionIndex() );
	}

	@Override
	public void setSelectedIndex( final int index )
	{
		onDisplayThread( getObject(), go -> {
			go.select( index );
			return null;
		} );
	}

	@Override
	public String getText()
	{
		return onDisplayThread( getObject(), go -> go.getText() );
	}

	@Override
	public void setText( final String text )
	{
		onDisplayThread( getObject(), go -> {
			go.setText( text );
			return null;
		} );
	}
}
