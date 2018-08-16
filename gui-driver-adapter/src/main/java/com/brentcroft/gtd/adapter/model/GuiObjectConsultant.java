package com.brentcroft.gtd.adapter.model;

import java.util.Properties;

import org.w3c.dom.Element;

public interface GuiObjectConsultant< T >
{
	void configure( Properties properties, String name );

	boolean ignore( GuiObject< T > t );

	boolean ignoreAttribute( String name );

	void extendElement( GuiObject< T > t, Element element );

	default boolean specialise( T go )
	{
		return false;
	}
}
