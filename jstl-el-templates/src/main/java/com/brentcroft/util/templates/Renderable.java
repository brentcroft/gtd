package com.brentcroft.util.templates;

import java.util.Map;

public interface Renderable
{
    String render( Map<String, ? super Object> rootObjects );
}
