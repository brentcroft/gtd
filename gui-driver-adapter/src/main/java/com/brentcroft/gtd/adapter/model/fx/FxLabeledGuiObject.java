package com.brentcroft.gtd.adapter.model.fx;

import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.scene.control.Labeled;

/**
 * Created by Alaric on 15/07/2017.
 */
public class FxLabeledGuiObject< T extends Labeled > extends FxNodeGuiObject< T >
{
	public FxLabeledGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
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

	enum Attr implements AttrSpec< Labeled >
	{
		TEXT( "text", go -> ofNullable( go.getText() ).orElse( null ) );

		final String n;
		final Function< Labeled, String > f;

		Attr( String name, Function< Labeled, String > f )
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
		public String getAttribute( Labeled go )
		{
			return f.apply( go );
		}
	}
}
