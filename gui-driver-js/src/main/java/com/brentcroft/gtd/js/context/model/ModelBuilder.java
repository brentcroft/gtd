package com.brentcroft.gtd.js.context.model;


import com.brentcroft.util.XPathUtils;
import java.util.Map;
import javax.xml.xpath.XPathConstants;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static java.lang.String.format;

public class ModelBuilder
{
    private final static Logger logger = Logger.getLogger( ModelBuilder.class );

    public static ModelMember newMember( String key, Object value, ModelObject caller )
    {
        if ( value == null )
        {
            throw new RuntimeException( "mirror is null!" );
        }

        if ( ! ( value instanceof ScriptObjectMirror ) )
        {
            return new ModelProperty( key, value );
        }

        ScriptObjectMirror mirror = ( ScriptObjectMirror ) value;

        if ( ( mirror.isFunction() || mirror.isStrictFunction() ) )
        {
            return new ModelFunction( key, mirror, caller );
        }
        else
        {
            return new ModelObject( key, mirror );
        }
    }


    public static boolean activateObject( ScriptObjectMirror som, Document document, boolean deActivateOnNotFound )
    {
        final String HASH_ATTRIBUTE = "hash";
        final String HASH_KEY = "$hash";
        final String ACTIVATED_KEY = "$active";

        ModelMember mm = ModelBuilder.newMember( "root", som, null );

        if ( ! mm.isObject() )
        {
            // we only activate objects
            return false;
        }

        boolean isActivated = false;

        ModelObject mo = ( ModelObject ) mm;

        if ( mo.hasXPath() )
        {
            String mox = mo.xpath();

            Element moe = null;

            try
            {
                moe = ( Element ) XPathUtils
                        .getCompiledPath( mox )
                        .evaluate( document, XPathConstants.NODE );
            }
            catch ( Exception e )
            {
                StringBuilder b = new StringBuilder( e.getMessage() );

                Throwable t = e.getCause();
                while ( t != null )
                {
                    b
                            .append( "\n  " )
                            .append( t.getMessage() );
                    t = t.getCause();
                }

                logger.warn( format( "Failed to evaluate xpath for ModelObject [%s]: %s\n %s",
                        mo.fullname(),
                        mox,
                        b ) );
            }


            if ( moe != null )
            {
                String currentHash = ( String ) mo.get( HASH_KEY );
                String newHash = moe.hasAttribute( HASH_ATTRIBUTE )
                        ? moe.getAttribute( HASH_ATTRIBUTE )
                        : null;

                if ( newHash != null )
                {
                    if ( ! newHash.equals( currentHash ) )
                    {
                        mo.set( HASH_KEY, newHash );
                    }

                    if ( ! mo.has( ACTIVATED_KEY ) )
                    {
                        //logger.info( format( "Activated object: hash=[%s], name=[%s].", hash, mo.fullname() ) );

                        mo.set( ACTIVATED_KEY, System.currentTimeMillis() );
                        isActivated = true;
                    }
                }
            }
            else if ( mo.has( ACTIVATED_KEY ) )
            {
                //logger.debug( format( "De-activated object: name=[%s].", mo.fullname() ) );

                isActivated = true;

                if ( deActivateOnNotFound )
                {
                    mo.remove( ACTIVATED_KEY );
                    isActivated = false;
                }
            }
        }


        for ( Map.Entry< String, Object > entry : som.entrySet() )
        {
            if ( entry.getKey().startsWith( "$" ) )
            {
                continue;
            }

            if ( entry.getValue() instanceof ScriptObjectMirror )
            {
                boolean activeChild = activateObject( ( ScriptObjectMirror ) entry.getValue(), document, deActivateOnNotFound );

                if ( ! isActivated && activeChild )
                {
                    mo.set( ACTIVATED_KEY, System.currentTimeMillis() );
                    isActivated = true;
                }
            }
        }

        return isActivated;
    }
}
