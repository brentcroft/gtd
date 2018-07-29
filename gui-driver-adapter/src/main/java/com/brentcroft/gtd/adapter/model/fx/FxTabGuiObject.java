package com.brentcroft.gtd.adapter.model.fx;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.DefaultGuiObject;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.adapter.utils.RobotUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.scene.Node;
import javafx.scene.control.Tab;

/**
 * Created by Alaric on 14/07/2017.
 */
public class FxTabGuiObject< T extends Tab > extends DefaultGuiObject< T > implements GuiObject.Click
{

	public FxTabGuiObject( T t, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager gom )
	{
		super( t, parent, guiObjectConsultant, gom );
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		addRobotAction( element, options );
		addClickAction( element, options );
		addTabAction( element, options );
	}

	@Override
	public boolean hasChildren()
	{
		return getObject().getContent() != null;
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		Node content = getObject().getContent();

		if ( content == null )
		{
			return null;
		}

		List< GuiObject< ? > > list = new ArrayList<>();

		list.add( getManager().adapt( content, this ) );

		return list;
	}

	@Override
	public void click()
	{
		FXUtils.maybeInvokeNowOnFXThread( () -> getObject().getTabPane().getSelectionModel().select( getObject() ) );
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

	enum Attr implements AttrSpec< Tab >
	{
		ID( "id", Tab::getId ),
		SELECTED( "selected", go -> "" + go.isSelected() ),
		TEXT( "text", go -> ofNullable( go.getText() ).orElse( null ) ),
		DISABLED( "focus", go -> "" + go.isDisable() ),
		TOOLTIP( "tooltip", go -> ofNullable( go.getTooltip() ).map( tooltip -> tooltip.getText() ).orElse( null ) );

		final String n;
		final Function< Tab, String > f;

		Attr( String name, Function< Tab, String > f )
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
		public String getAttribute( Tab go )
		{
			return f.apply( go );
		}
	}

}
