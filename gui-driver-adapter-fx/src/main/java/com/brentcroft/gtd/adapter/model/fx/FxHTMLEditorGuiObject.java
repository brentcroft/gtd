package com.brentcroft.gtd.adapter.model.fx;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

import javafx.scene.web.HTMLEditor;

/**
 * Created by Alaric on 15/07/2017.
 */
@SuppressWarnings( "restriction" )
public class FxHTMLEditorGuiObject< T extends HTMLEditor > extends FxControlGuiObject< T > implements GuiObject.Text
{

	public FxHTMLEditorGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
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
		return getObject().getHtmlText();
	}

	@Override
	public void setText( String text )
	{
		FXUtils.maybeInvokeNowOnFXThread( () -> getObject().setHtmlText( text ) );
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

	// "text", "size", "selected-index"
	enum Attr implements AttrSpec< HTMLEditor >
	{
		TEXT( "text", go -> "" + go.getHtmlText() );

		final String n;
		final Function< HTMLEditor, String > f;

		Attr( String name, Function< HTMLEditor, String > f )
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
		public String getAttribute( HTMLEditor go )
		{
			return f.apply( go );
		}
	}
}
