package com.brentcroft.gtd.adapter.model.swing;

import com.brentcroft.gtd.adapter.model.AbstractGuiObjectConsultant;
import java.awt.Component;
import java.util.Properties;

public class ComponentGuiObjectConsultant< T extends Component > extends AbstractGuiObjectConsultant< T >
{
    public ComponentGuiObjectConsultant( Properties properties )
    {
        configure( properties, "Component" );
    }

    public boolean isHidden( T t )
    {
        return ! t.isShowing();
    }
}
