package com.brentcroft.util.templates.jstl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.brentcroft.util.templates.Renderable;
import com.brentcroft.util.templates.jstl.tag.JstlElement;

/**
 * A Renderable decomposition of a text stream into a list of
 * <code>Renderable</code> elements.
 */
public class JstlTemplate implements Renderable
{
    private final List<Renderable> elements = new ArrayList<>();

    private final JstlElement parent;


    public JstlTemplate( JstlElement parent )
    {
        this.parent = parent;
    }


    public JstlElement getParent()
    {
        return parent;
    }

    /**
     * The key function of a template is to render itself (using a Map of
     * objects providing the EL namespace).
     *
     * @param rootObjects
     * @return String the rendered output as a String
     */
    public String render( Map<String, ? super Object> rootObjects )
    {
        final StringBuilder out = new StringBuilder();

        for ( Renderable element : elements )
        {
            out.append( element.render( rootObjects ) );
        }

        return out.toString();
    }

    public List<Renderable> getElements()
    {
        return elements;
    }


    public void addRenderable( Renderable renderable )
    {
        elements.add( renderable );
    }


    public String toString()
    {
        final StringBuilder out = new StringBuilder();

        elements.forEach( out::append );

        return out.toString();
    }

}
