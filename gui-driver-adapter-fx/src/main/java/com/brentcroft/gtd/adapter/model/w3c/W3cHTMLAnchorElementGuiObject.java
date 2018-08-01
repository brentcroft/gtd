package com.brentcroft.gtd.adapter.model.w3c;

import static com.brentcroft.util.XmlUtils.maybeSetElementAttribute;

import java.util.Map;
import java.util.Optional;

import org.w3c.dom.Element;
import org.w3c.dom.html.HTMLAnchorElement;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.FXUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 15/07/2017.
 */
public class W3cHTMLAnchorElementGuiObject< T extends HTMLAnchorElement > extends W3cHTMLElementGuiObject< T > implements GuiObject.Click
{
	public W3cHTMLAnchorElementGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		// actions
		addClickAction( element, options );

		HTMLAnchorElement anchor = getObject();

		// maybe re-write href
		Optional
				.ofNullable( anchor.getHref() )
				.ifPresent( href -> {
					int p = href.indexOf( ';' );

					maybeSetElementAttribute(
							element,
							null,
							ATTRIBUTE_HREF,
							(p > -1)
									? href.substring( 0, p )
									: href
					);
				} );
	}

	@Override
	public void click()
	{
		// NB: unlike HTMLInputElement, HTMLAnchorElement doesn't expose a click()
		// method
		FXUtils.reflectiveCallRunAndWait( getObject(), "click" );
	}
	
	@Override
	public void rightClick()
	{
		throw new UnsupportedOperationException();
	}
	
}
