package com.brentcroft.gtd.camera.model.swt;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.widgets.Label;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 *
 */
public class LabelGuiObject< T extends Label > extends ControlGuiObject< T >
{
	public LabelGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
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

	enum Attr implements AttrSpec< Label >
	{
		TEXT( "text", go -> DataLimit.MAX_TEXT_LENGTH.maybeTruncate( go.getText() ) );

		final String n;
		final Function< Label, Object > f;

		Attr( String name, Function< Label, Object > f )
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
		public String getAttribute( Label go )
		{
			return onSwtDisplayThreadAsText( go, f );
		}
	}
}
