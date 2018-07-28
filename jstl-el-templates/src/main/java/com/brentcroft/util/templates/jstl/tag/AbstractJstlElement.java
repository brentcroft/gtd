package com.brentcroft.util.templates.jstl.tag;


import org.apache.log4j.Logger;

import com.brentcroft.util.templates.jstl.JstlTemplate;
import com.brentcroft.util.tools.MapBindings;

public abstract class AbstractJstlElement implements JstlElement
{
    protected final Logger logger = Logger.getLogger( getClass().getCanonicalName() );

    protected final static MapBindings EMPTY_MAP = new MapBindings();

    protected JstlTemplate innerRenderable;


    public JstlTemplate getInnerJstlTemplate()
    {
        return innerRenderable;
    }

    public void normalize()
    {
    }

    public String toString()
    {
        return toText() + "\n";
    }
}
