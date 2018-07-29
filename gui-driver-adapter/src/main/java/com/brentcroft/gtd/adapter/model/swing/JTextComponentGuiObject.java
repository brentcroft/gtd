package com.brentcroft.gtd.adapter.model.swing;

import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.text.JTextComponent;

import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.SwingUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JTextComponentGuiObject< T extends JTextComponent > extends JComponentGuiObject< T > implements GuiObject.Text, GuiObject.Click
{
	public JTextComponentGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		addTextAction( element, options );
	}

	@Override
	public String getText()
	{
		return getObject().getText();
	}

	@Override
	public void setText( String text )
	{
		SwingUtils.maybeInvokeNowOnEventThread( () -> getObject().setText( text ) );
	}

	@Override
	public void click()
	{
		getObject().requestFocus();
		// RobotUtils.awtRobotClick( getObjectMidpointLocationOnScreen() );
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

	enum Attr implements AttrSpec< JTextComponent >
	{
		TEXT( "text", go -> ofNullable( DataLimit.MAX_TEXT_LENGTH.maybeTruncate( go.getText() ) ).orElse( null ) );

		final String n;
		final Function< JTextComponent, String > f;

		Attr( String name, Function< JTextComponent, String > f )
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
		public String getAttribute( JTextComponent go )
		{
			return f.apply( go );
		}
	}
}
