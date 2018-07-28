package com.brentcroft.util.templates.jstl.tag;

import java.util.Map;

import javax.el.ValueExpression;

import com.brentcroft.util.templates.ELTemplateManager;
import com.brentcroft.util.templates.jstl.JstlTemplate;
import com.brentcroft.util.tools.MapBindings;

public class JstlWhile extends AbstractJstlElement
{
    private final static String TAG = "c:while";

    private String testEL;

    private ValueExpression valueExpression;

    private final ELTemplateManager elTemplateManager;

    private final String varStatus;


    public JstlWhile( ELTemplateManager elTemplateManager, String testEL, String varStatus )
    {
        this.elTemplateManager = elTemplateManager;
        this.testEL = testEL;

        this.varStatus = varStatus;

        innerRenderable = new JstlTemplate( this );
    }


    private void compile()
    {
        valueExpression = elTemplateManager.getValueExpression( testEL, EMPTY_MAP, Object.class );
    }

    @Override
    public void normalize()
    {
        compile();
    }

    public String render( Map<String, ? super Object> rootObjects )
    {
        final LoopTagStatus<Object> loopTagStatus = new LoopTagStatus<>( null, null, null );

        Object value = valueExpression.getValue( elTemplateManager.getELContext( rootObjects ) );

        final StringBuilder b = new StringBuilder();

        while ( value instanceof Boolean && ( (Boolean) value ) )
        {
            // protect external bindings from pollution in the loop
            // scope
            final MapBindings localObjects = new MapBindings( rootObjects );


            localObjects.put( varStatus, loopTagStatus );


            b.append( innerRenderable.render( localObjects ) );

            loopTagStatus.increment();

            // but always test in outer scope
            value = valueExpression.getValue( elTemplateManager.getELContext( rootObjects ) );
        }

        return b.toString();
    }


    public String toText()
    {
        return String.format( "<%s test=\"%s\">%s</%s>", TAG, testEL, innerRenderable, TAG );
    }
}