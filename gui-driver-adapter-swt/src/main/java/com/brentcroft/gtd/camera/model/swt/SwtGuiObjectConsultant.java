package com.brentcroft.gtd.camera.model.swt;

import com.brentcroft.gtd.adapter.model.AbstractGuiObjectConsultant;
import java.util.Properties;

import org.eclipse.swt.widgets.Widget;

public class SwtGuiObjectConsultant< T extends Widget > extends AbstractGuiObjectConsultant< T >
{
    public SwtGuiObjectConsultant( Properties properties )
    {
        configure( properties, "Widget" );
    }

    public boolean isHidden( T t )
    {
        return false;
    }
}
