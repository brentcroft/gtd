package com.brentcroft.gtd.adapter.model.w3c;

import java.util.Map;
import java.util.Optional;

import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.brentcroft.gtd.adapter.model.DefaultGuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 15/07/2017.
 */
public class W3cTextGuiObject< T extends Text > extends DefaultGuiObject< T >
{
	public W3cTextGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	/**
	 * Returns \"text\".<br/>
	 *
	 * @return \"text\"
	 */
	@Override
	public String getComponentTag()
	{
		return "text";
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		Optional
				.ofNullable( element.getOwnerDocument() )
				.ifPresent( document -> element
						.appendChild(
								document
										.createTextNode(
												DataLimit.MAX_TEXT_LENGTH
														.maybeTruncate(
																getObject()
																		.getData()
																		.trim() ) ) ) );
	}
}
