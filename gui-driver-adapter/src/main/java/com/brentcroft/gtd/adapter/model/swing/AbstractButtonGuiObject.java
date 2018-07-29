package com.brentcroft.gtd.adapter.model.swing;

import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.AbstractButton;

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
public class AbstractButtonGuiObject< T extends AbstractButton > extends JComponentGuiObject< T > implements GuiObject.Click
{
	public AbstractButtonGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		addClickAction( element, options );
	}

	@Override
	public void click()
	{
		SwingUtils.maybeInvokeNowOnEventThread( () -> getObject().doClick() );
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

	enum Attr implements AttrSpec< AbstractButton >
	{
		ICON( "icon", go -> ofNullable( go.getIcon() )
				.map( JComponentGuiObject::getPathyModel )
				.orElse( null ) ),

		SELECTED( "selected", go -> go.isSelected() ? "true" : null ),

		TEXT( "text", go -> ofNullable( go.getText() )
				.orElse( null ) );

		final String n;
		final Function< AbstractButton, String > f;

		Attr( String name, Function< AbstractButton, String > f )
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
		public String getAttribute( AbstractButton go )
		{
			return f.apply( go );
		}
	}
}
