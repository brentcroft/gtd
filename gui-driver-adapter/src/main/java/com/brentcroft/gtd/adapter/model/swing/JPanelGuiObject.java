package com.brentcroft.gtd.adapter.model.swing;

import static java.util.Optional.ofNullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class JPanelGuiObject< T extends JPanel > extends JComponentGuiObject< T >
{
	public JPanelGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
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

	enum Attr implements AttrSpec< JPanel >
	{
		TITLE( "title", go -> ofNullable( go.getBorder() ).filter( border -> border instanceof TitledBorder )
				.map( border -> ( TitledBorder ) border ).map( border -> border.getTitle() ).orElse( null ) );

		final String n;
		final Function< JPanel, String > f;

		Attr( String name, Function< JPanel, String > f )
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
		public String getAttribute( JPanel go )
		{
			return f.apply( go );
		}
	}
}
