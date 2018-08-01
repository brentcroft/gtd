package com.brentcroft.gtd.camera.model.swt;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.browser.Browser;

import static com.brentcroft.gtd.adapter.model.AttrSpec.trueOrNull;
import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class BrowserGuiObject< T extends Browser > extends ControlGuiObject< T >
{
	public BrowserGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
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

	enum Attr implements AttrSpec< Browser >
	{
		BACK_ENABLED( "back-enabled", go -> trueOrNull( go.isBackEnabled() ) ),
		FOCUS( "focus", go -> trueOrNull( go.isFocusControl() ) ),
		FORWARD_ENABLED( "forward-enabled", go -> trueOrNull( go.isForwardEnabled() ) ),
		BROWSER_TYPE( "browser-type", go -> go.getBrowserType() ),
		;

		final String n;
		final Function< Browser, Object > f;

		Attr( String name, Function< Browser, Object > f )
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
		public String getAttribute( Browser go )
		{
			return onSwtDisplayThreadAsText( go, f );
		}
	}
}
