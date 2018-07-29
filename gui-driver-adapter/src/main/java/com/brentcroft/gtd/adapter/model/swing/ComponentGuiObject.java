package com.brentcroft.gtd.adapter.model.swing;

import static java.util.Optional.ofNullable;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.DefaultGuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.RobotUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class ComponentGuiObject< T extends Component > extends DefaultGuiObject< T >
{
	private final static Logger logger = Logger.getLogger( ComponentGuiObject.class );

	public ComponentGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public int[] getLocation()
	{
		Rectangle p = getObject().getBounds();
		return new int[] {
				p.x,
				p.y,
				p.width,
				p.height };
	}

	@Override
	public int[] getObjectLocationOnScreen()
	{
		if ( getObject().isVisible() )
		{
			Point p = getObject().getLocationOnScreen();
			return new int[] {
					p.x,
					p.y,
					getObject().getWidth(),
					getObject().getHeight() };
		}
		else
		{
			return new int[] { -1, -1, -1, -1 };
		}
	}

	@Override
	public void robotClick()
	{
		RobotUtils.awtRobotClick( getObjectMidpointLocationOnScreen() );
	}

	@Override
	public void robotDoubleClick()
	{
		RobotUtils.awtRobotDoubleClick( getObjectMidpointLocationOnScreen() );
	}

	@Override
	public void robotKeys( String keys )
	{
		if ( !getObject().isVisible() )
		{
			logger.warn( "Object is not visible." );
		}
		else if ( !getObject().isShowing() )
		{
			logger.warn( "Object is not showing." );
		}

		RobotUtils
				.awtRobotKeys(
						getObjectMidpointLocationOnScreen(),
						() -> getObject().hasFocus(),
						keys
				);
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		// every Component is accessible by robot actions
		addRobotAction( element, options );
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

	// "name", "disabled", "visible", "focus"
	enum Attr implements AttrSpec< Component >
	{
		NAME( "name", go -> ofNullable( go.getName() )
				.orElse( null ) ),

		DISABLED( "disabled", go -> go.isEnabled() ? "" : "true" ),

		VISIBLE( "visible", go -> go.isVisible() ? "true" : null ),

		FOCUS( "focus", go -> go.hasFocus() ? "true" : null );

		final String n;
		final Function< Component, String > f;

		Attr( String name, Function< Component, String > f )
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
		public String getAttribute( Component go )
		{
			return f.apply( go );
		}
	}
}
