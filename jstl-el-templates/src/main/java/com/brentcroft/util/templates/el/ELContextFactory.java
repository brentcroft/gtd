package com.brentcroft.util.templates.el;

import java.util.Map;

import javax.el.ELContext;

public interface ELContextFactory
{
    ELContext getELContext( Map<?, ?> rootObjects );

    ELContext getELConfigContext();
}
