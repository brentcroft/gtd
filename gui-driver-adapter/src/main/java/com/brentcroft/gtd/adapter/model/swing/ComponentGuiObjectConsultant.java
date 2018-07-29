package com.brentcroft.gtd.adapter.model.swing;

import java.awt.Component;
import java.util.Properties;

import com.brentcroft.gtd.adapter.model.AbstractGuiObjectConsultant;

public class ComponentGuiObjectConsultant< T extends Component > extends AbstractGuiObjectConsultant< T >
{
	public ComponentGuiObjectConsultant( Properties properties )
	{
		configure( properties, "Component" );
	}

	@Override
	public boolean isHidden( T t )
	{
		return !t.isShowing();
	}
}
