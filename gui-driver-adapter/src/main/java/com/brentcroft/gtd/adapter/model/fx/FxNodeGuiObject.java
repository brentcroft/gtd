package com.brentcroft.gtd.adapter.model.fx;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.DefaultGuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.adapter.utils.RobotUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.geometry.Bounds;
import javafx.scene.Node;

/**
 * Created by Alaric on 14/07/2017.
 */
public class FxNodeGuiObject< T extends Node > extends DefaultGuiObject< T >
{
	private final static Logger logger = Logger.getLogger( FxNodeGuiObject.class );

	public FxNodeGuiObject( T t, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager hgom )
	{
		super( t, parent, guiObjectConsultant, hgom );
	}

	@Override
	public int[] getLocation()
	{
		Bounds screenBounds = getObject().getBoundsInParent();

		return new int[] { Double.valueOf( screenBounds.getMinX() ).intValue(),
				Double.valueOf( screenBounds.getMinY() ).intValue(),
				Double.valueOf( screenBounds.getWidth() ).intValue(),
				Double.valueOf( screenBounds.getHeight() ).intValue() };
	}

	@Override
	public int[] getObjectLocationOnScreen()
	{
		Bounds screenBounds = getObject().localToScreen( getObject().getBoundsInLocal() );

		return new int[] { Double.valueOf( screenBounds.getMinX() ).intValue(),
				Double.valueOf( screenBounds.getMinY() ).intValue(),
				Double.valueOf( screenBounds.getWidth() ).intValue(),
				Double.valueOf( screenBounds.getHeight() ).intValue() };
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		addRobotAction( element, options );
	}

	@Override
	public void robotKeys( String keys )
	{
		if ( !getObject().isVisible() )
		{
			logger.warn( "Object is not visible." );
		}

		FXUtils.maybeInvokeNowOnFXThread( () -> {

			getObject().requestFocus();

			RobotUtils.fxRobotKeys(
					getObject(), () -> getObject().isFocused() || true, keys );
		} );
	}

	@Override
	public void robotClick()
	{
		RobotUtils.fxRobotClick( getObject() );
	}

	@Override
	public void robotDoubleClick()
	{
		RobotUtils.fxRobotDoubleClick( getObject() );
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

	enum Attr implements AttrSpec< Node >
	{
		ID( "id", Node::getId ),
		FOCUS( "focus", go -> "" + go.isFocused() ),
		VISIBLE( "visible", go -> "" + go.isVisible() ),
		DISABLED( "focus", go -> "" + go.isDisable() );

		final String n;
		final Function< Node, String > f;

		Attr( String name, Function< Node, String > f )
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
		public String getAttribute( Node go )
		{
			return f.apply( go );
		}
	}
}
