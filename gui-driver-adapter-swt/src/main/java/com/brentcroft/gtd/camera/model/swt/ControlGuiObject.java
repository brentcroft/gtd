package com.brentcroft.gtd.camera.model.swt;

import static com.brentcroft.gtd.adapter.model.AttrSpec.trueOrNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class ControlGuiObject< T extends Control > extends WidgetGuiObject< T > implements GuiObject.Click
{
	private static Logger logger = Logger.getLogger( ControlGuiObject.class );

	public ControlGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		// every Control is clickable
		addClickAction( element, options );
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

	enum Attr implements AttrSpec< Control >
	{
		DISABLED( "disabled", go -> trueOrNull( !go.isEnabled() ) ),
		VISIBLE( "visible", go -> trueOrNull( go.isVisible() ) ),
		FOCUS( "focus", go -> trueOrNull( go.isFocusControl() ) ),

		TOOLTIP( "tooltip", go -> DataLimit.MAX_TEXT_LENGTH.maybeTruncate( go.getToolTipText() ) ),
		;

		final String n;
		final Function< Control, Object > f;

		Attr( String name, Function< Control, Object > f )
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
		public String getAttribute( Control go )
		{
			return onSwtDisplayThreadAsText( go, f );
		}
	}

	@Override
	public void click()
	{
		click( 1 );
	}

	@Override
	public void rightClick()
	{
		click( 3 );
	}

	@Override
	public int[] getLocation()
	{
		Rectangle p = onDisplayThread( getObject(), go -> go.getBounds() );
		return new int[] { p.x, p.y, p.width, p.height };
	}

	@Override
	public int[] getObjectLocationOnScreen()
	{
		int[] r = getLocation();

		int centreX = r[ 2 ] / 2;
		int centreY = r[ 3 ] / 2;

		Point p = onDisplayThread( getObject(), go -> go.toDisplay( centreX, centreY ) );

		return new int[] { p.x, p.y };
	}

	protected void click( int mouseButton )
	{
		onDisplayThread( getObject(), go -> go.setFocus() );

		onDisplayThread( getObject(), go -> {
			go.getShell().setActive();
			return null;
		} );

		if ( onDisplayThread( getObject(), go -> go.isFocusControl() ) &&
				onDisplayThread( getObject(), go -> go.isVisible() ) )
		{
			click( getObject(), mouseButton );
		}
		else
		{
			logger.warn( "Object is not in focus" );
		}
	}

	public static void click( Control control, int mouseButton )
	{
		long delay = 80;

		final Point[] pt = { null };

		Display controlDisplay = control.getDisplay();

		controlDisplay.syncExec( () -> {
			Rectangle r = control.getBounds();
			pt[ 0 ] = controlDisplay.map( control, null, r.width / 2, r.height / 2 );
		} );

		processEvents(
				new Event[] {
						new Event()
						{
							{
								type = SWT.MouseMove;
								display = controlDisplay;
								x = pt[ 0 ].x;
								y = pt[ 0 ].y;
							}
						},
						new Event()
						{
							{
								type = SWT.MouseDown;
								display = controlDisplay;
								button = mouseButton;
							}
						},
						new Event()
						{
							{
								type = SWT.MouseUp;
								display = control.getDisplay();
								button = mouseButton;
							}
						}
				}, delay );
	}

}
