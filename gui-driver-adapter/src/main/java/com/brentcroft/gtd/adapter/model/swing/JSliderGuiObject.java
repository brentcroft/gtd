package com.brentcroft.gtd.adapter.model.swing;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JSlider;

import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.SwingUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JSliderGuiObject< T extends JSlider > extends JComponentGuiObject< T > implements GuiObject.Index
{
	public JSliderGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
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
		return getObject().getExtent();
	}

	@Override
	public Integer getSelectedIndex()
	{
		return getObject().getValue();
	}

	@Override
	public void setSelectedIndex( int index )
	{
		SwingUtils.maybeInvokeNowOnEventThread( () -> getObject().setValue( index ) );
	}

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

	// "name", "disabled", "visible", "focus"
	enum Attr implements AttrSpec< JSlider >
	{
		SIZE( "size", go -> "" + go.getExtent() ), SELECTED_INDEX( "selected-index", go -> "" + go.getValue() );

		final String n;
		final Function< JSlider, String > f;

		Attr( String name, Function< JSlider, String > f )
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
		public String getAttribute( JSlider go )
		{
			return f.apply( go );
		}
	}

}
