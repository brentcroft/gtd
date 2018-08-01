package com.brentcroft.gtd.adapter.model.w3c;

import java.util.Properties;

import org.w3c.dom.html.HTMLElement;

import com.brentcroft.gtd.adapter.model.AbstractGuiObjectConsultant;
import com.sun.webkit.dom.HTMLElementImpl;

public class W3CHTMLElementGuiObjectConsultant< T extends HTMLElement > extends AbstractGuiObjectConsultant< T >
{
	public W3CHTMLElementGuiObjectConsultant( Properties properties )
	{
		configure( properties, "HTMLElement" );
	}

	@Override
	public boolean isHidden( T t )
	{
		return (t instanceof HTMLElementImpl) && (( HTMLElementImpl ) t).getHidden();
	}
}
