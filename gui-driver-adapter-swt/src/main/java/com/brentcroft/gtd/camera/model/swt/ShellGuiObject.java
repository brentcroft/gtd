package com.brentcroft.gtd.camera.model.swt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.swt.widgets.Shell;

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
public class ShellGuiObject< T extends Shell > extends CompositeGuiObject< T >
{
	public ShellGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
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

	// "disabled", "visible", "focus"
	enum Attr implements AttrSpec< Shell >
	{
		TEXT( "text", go -> DataLimit.MAX_TEXT_LENGTH.maybeTruncate( go.getText() ) );

		final String n;
		final Function< Shell, Object > f;

		Attr( String name, Function< Shell, Object > f )
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
		public String getAttribute( Shell go )
		{
			return onSwtDisplayThreadAsText( go, f );
		}
	}

	@Override
	public boolean hasChildren()
	{
		return true;
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		List< GuiObject< ? > > children = new ArrayList<>();

		// add any menu bar
		Optional
				.ofNullable( onDisplayThread( getObject(), go -> go.getMenuBar() ) )
				.filter( Objects::nonNull )
				.map( child -> getManager().adapt( child, this ) )
				.ifPresent( children::add );

		// add any menu
		Optional
				.ofNullable( onDisplayThread( getObject(), go -> go.getMenu() ) )
				.filter( Objects::nonNull )
				.map( child -> getManager().adapt( child, this ) )
				.ifPresent( children::add );

		children
				.addAll( super.loadChildren() );

		return children;
	}
}
