package com.brentcroft.util.templates.jstl.tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.brentcroft.util.templates.Renderable;
import com.brentcroft.util.templates.jstl.JstlTemplate;

public class JstlChoose extends AbstractJstlElement
{
    private final static String TAG = "c:choose";

    public JstlChoose()
    {
        innerRenderable = new JstlTemplate( this );
    }

    @Override
    public void normalize()
    {
        final List<Renderable> elements = innerRenderable.getElements();

        if ( elements != null )
        {
            List<Renderable> elementsToRemove = null;

            for ( Renderable r : elements )
            {
                if ( r instanceof JstlWhen || r instanceof JstlOtherwise )
                {
                    continue;
                }

                if ( elementsToRemove == null )
                {
                    elementsToRemove = new ArrayList<>();
                }

                // remove from elements
                elementsToRemove.add( r );
            }

            if ( elementsToRemove != null )
            {
                elements.removeAll( elementsToRemove );
            }
        }
    }


    public String render( Map<String, ? super Object> rootObjects )
    {
        final List<Renderable> elements = innerRenderable.getElements();

        if ( elements != null )
        {
            for ( Renderable r : elements )
            {
                if ( r instanceof JstlWhen && ( (JstlWhen) r ).test( rootObjects ) )
                {
                    return r.render( rootObjects );
                }
                else if ( r instanceof JstlOtherwise )
                {
                    return r.render( rootObjects );
                }
            }
        }

        return "";
    }

    public String toText()
    {
        return String.format( "<%s>%s</%s>", TAG, innerRenderable, TAG );
    }

}