package com.brentcroft.gtd.camera.model.swt;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 *
 */
// using: org.eclipse.swt.widgets.Text avoid clash
public class TextGuiObject< T extends org.eclipse.swt.widgets.Text > extends ControlGuiObject< T >
		implements GuiObject.Text
{
	public TextGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public String getText()
	{
		return onDisplayThread( getObject(), go -> getObject().getText() );
	}

	@Override
	public void setText( final String text )
	{
		onDisplayThread( getObject(), go -> {
			go.setText( text );
			return null;
		} );
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
	enum Attr implements AttrSpec< org.eclipse.swt.widgets.Text >
	{
		TEXT( "text", go -> DataLimit.MAX_TEXT_LENGTH.maybeTruncate( go.getText() ) );

		final String n;
		final Function< org.eclipse.swt.widgets.Text, Object > f;

		Attr( String name, Function< org.eclipse.swt.widgets.Text, Object > f )
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
		public String getAttribute( org.eclipse.swt.widgets.Text go )
		{
			return onSwtDisplayThreadAsText( go, f );
		}
	}
}
