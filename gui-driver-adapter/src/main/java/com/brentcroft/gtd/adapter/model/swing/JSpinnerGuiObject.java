package com.brentcroft.gtd.adapter.model.swing;

import static java.util.Optional.ofNullable;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.swing.JSpinner;

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
public class JSpinnerGuiObject< T extends JSpinner > extends JComponentGuiObject< T > implements GuiObject.Text
{
	public JSpinnerGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
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
		JSpinner spinner = getObject();

		try
		{
			spinner.commitEdit();
		}
		catch ( ParseException e )
		{
			throw new RuntimeException( e );
		}

		Object value = spinner.getValue();

		return value == null ? "" : value.toString();
	}

	@Override
	public void setText( String text )
	{
		SwingUtils.maybeInvokeNowOnEventThread( () -> {
			JSpinner item = getObject();

			item.setValue( text );

			try
			{
				item.commitEdit();
			}
			catch ( ParseException e )
			{
				throw new RuntimeException( e );
			}
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

	enum Attr implements AttrSpec< JSpinner >
	{
		TEXT( "text", go -> {
			try
			{
				go.commitEdit();
			}
			catch ( ParseException e )
			{
				throw new RuntimeException( e );
			}

			return ofNullable( go.getValue() ).map( Object::toString ).orElse( null );
		} );

		final String n;
		final Function< JSpinner, String > f;

		Attr( String name, Function< JSpinner, String > f )
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
		public String getAttribute( JSpinner go )
		{
			return f.apply( go );
		}
	}
}
