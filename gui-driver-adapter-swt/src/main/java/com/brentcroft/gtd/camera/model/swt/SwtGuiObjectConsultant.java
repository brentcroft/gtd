package com.brentcroft.gtd.camera.model.swt;

import java.util.Properties;

import org.eclipse.swt.widgets.Widget;

import com.brentcroft.gtd.adapter.model.AbstractGuiObjectConsultant;

public class SwtGuiObjectConsultant< T extends Widget > extends AbstractGuiObjectConsultant< T >
{
	public SwtGuiObjectConsultant( Properties properties )
	{
		configure( properties, "Widget" );
	}

	@Override
	public boolean isHidden( T t )
	{
		return false;
	}
}
