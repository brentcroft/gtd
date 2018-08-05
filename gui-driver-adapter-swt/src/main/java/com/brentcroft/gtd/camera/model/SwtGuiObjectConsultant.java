package com.brentcroft.gtd.camera.model;

import java.util.Properties;

import org.eclipse.swt.widgets.Widget;

import com.brentcroft.gtd.adapter.model.AbstractGuiObjectConsultant;

public class SwtGuiObjectConsultant< T extends Widget > extends AbstractGuiObjectConsultant< T >
{
	public SwtGuiObjectConsultant( Properties properties )
	{
		configure( properties, "SWTWidget" );
	}

	@Override
	public boolean isHidden( T t )
	{
		return false;
	}
}
