package com.brentcroft.gtd.events;

import com.sun.webkit.dom.KeyboardEventImpl;
import com.sun.webkit.dom.UIEventImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.events.MutationEvent;

import static java.lang.String.format;

/**
 * Created by Alaric on 25/05/2017.
 */
public class DOMEventUtils extends AbstractEventUtils< Event, DOMEventHandler >
{
    private final static Logger logger = Logger.getLogger( DOMEventUtils.class );

    private static final List< DOMEventHandler > listeners = new ArrayList<>();




    public static final String DOM_SUBTREE_MODIFIED = "DOMSubtreeModified";
    public static final String DOM_NODE_INSERTED = "DOMNodeInserted";
    public static final String DOM_NODE_REMOVED = "DOMNodeRemoved";
    public static final String DOM_NODE_REMOVED_FROM_DOCUMENT = "DOMNodeRemovedFromDocument";
    public static final String DOM_NODE_INSERTED_INTO_DOCUMENT = "DOMNodeInsertedIntoDocument";
    public static final String DOM_ATTR_MODIFIED = "DOMAttrModified";
    public static final String DOM_CHARACTER_DATA_MODIFIED = "DOMCharacterDataModified";


    private static String[] defaultDOMEventTypes = {

            "click",

            "mousedown",
            "mouseup",
//            "mouseover",

            "contextmenu",

            "input",
            "select",
            "change",

            "submit",

            "keydown",
            "keypress",
            "keyup",

            "focus",
            "blur",

            DOM_SUBTREE_MODIFIED,
            DOM_ATTR_MODIFIED,
            DOM_CHARACTER_DATA_MODIFIED
    };

    private static Set< String > allowedDOMEvents = new HashSet<>( Arrays.asList( defaultDOMEventTypes ) );


    public DOMEventUtils()
    {
        // DOMEventSource is provided by the browser module
        // and delivers notifications of new documents once loaded
        DOMEventSource
                .addConsumer( document -> attachDOMListenerOnDocument( document, false ) );
    }


    public void setDefaultAllowedDOMEvents()
    {
        allowedDOMEvents.clear();
        allowedDOMEvents.addAll( Arrays.asList( defaultDOMEventTypes ) );
    }

    public void setAllowedDOMEvents( Set< String > newAllowedDOMEvents )
    {
        allowedDOMEvents.clear();
        allowedDOMEvents.addAll( newAllowedDOMEvents );
    }


    public Set< String > getAllowedDOMEvents()
    {
        return allowedDOMEvents;
    }


    /**
     * Registers the listener on the given document for each allowed DOM Event.<p/>
     * <p>
     * If the document is null then just returns, after logging a message.
     *
     * @param document        the document to have listeners attached
     * @param useCapturePhase in what phase the listener is attached (for each event type)
     */
    public void attachDOMListenerOnDocument( Document document, boolean useCapturePhase )
    {
        if ( document == null )
        {
            logger.debug( "No document!" );
            return;
        }

        EventTarget h = ( EventTarget ) document;

        for ( String eventType : getAllowedDOMEvents() )
        {
            h.addEventListener( eventType, getEventListener(), useCapturePhase );
        }

        logger.debug( format( "Installed DOM Listeners: events=%s", getAllowedDOMEvents() ) );
    }


    public void addListener( DOMEventHandler listener )
    {
        listeners.remove( listener );
        listeners.add( listener );
    }

    public void removeListener( DOMEventHandler listener )
    {
        listeners.remove( listener );
    }


    public boolean canIgnore( Event event )
    {
        return ! allowedDOMEvents.contains( event.getType() );
    }


    public DOMEventHandler getHandler( Consumer< Event > consumer )
    {
        return domEvent ->
        {
            try
            {
                consumer.accept( domEvent );
            }
            catch ( Exception ex )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.warn( format( "Ignored DOMEvent (exception): [%s]; %s", domEvent.getType(), ex ) );
                }
            }
        };
    }

    //
    private final EventListener EVENT_LISTENER = evt ->
    {
        if ( listeners == null || listeners.isEmpty() )
        {
            return;
        }

        for ( DOMEventHandler bel : listeners )
        {
            try
            {
                bel.handleDOMEvent( evt );
            }
            catch ( Exception e )
            {
                logger.warn( format( "Error on event [%s] calling DOMEventHandler [%s]: %s", evt, bel, e ) );
            }
        }
    };


    /**
     * Provides an EventListener to a source of DOMEvents
     * so that the events can be propagated to registered DOMEventHandlers.<p/>
     * <p>
     * E.g. WebUIController adds this listener (in the capture phase)
     * to the document element whenever a page is loaded.<p/>
     * Listeners of this class can then tap into this stream of events.
     *
     * @return
     */
    public EventListener getEventListener()
    {
        return EVENT_LISTENER;
    }


    public String getParams( Event event )
    {
        StringBuilder b = new StringBuilder();

        buildParams( event, b );

        return b.toString();
    }


    protected DOMEventUtils buildParams( Event event, StringBuilder b )
    {
        if ( event instanceof MouseEvent )
        {
            buildParamsMouseEvent( ( MouseEvent ) event, b );
        }
        else if ( event instanceof KeyboardEventImpl )
        {
            buildParamsKeyboardEvent( ( KeyboardEventImpl ) event, b );
        }
        else if ( event instanceof UIEventImpl )
        {
            buildParamsUIEvent( ( UIEventImpl ) event, b );
        }
        else if ( event instanceof MutationEvent )
        {
            buildParamsMutationEvent( ( MutationEvent ) event, b );
        }
        else
        {
            try
            {
                b.append( event.toString() );
            }
            catch (Exception e)
            {

            }
        }
        return this;
    }

    private DOMEventUtils buildParamsKeyboardEvent( KeyboardEventImpl event, StringBuilder b )
    {
        if ( "keypress".equalsIgnoreCase( event.getType() ) )
        {
            b.append( "key=" ).append( replaceAnyNonPrintingCharacters( event.getKeyIdentifier() ) );
        }
        else
        {
            b.append( "code=" ).append( event.getKeyCode() );

        }
        b.append( "&x=" ).append( event.getPageX() );
        b.append( "&y=" ).append( event.getPageY() );

        return this;
    }

    private DOMEventUtils buildParamsMouseEvent( MouseEvent event, StringBuilder b )
    {
        b.append( "button=" ).append( event.getButton() );
        b.append( "&clicks=" ).append( event.getDetail() );
        b.append( "&screenX=" ).append( event.getScreenX() );
        b.append( "&screenY=" ).append( event.getScreenY() );
        b.append( "&x=" ).append( event.getClientX() );
        b.append( "&y=" ).append( event.getClientY() );

        boolean[] modifiers = {
                event.getAltKey(),
                event.getCtrlKey(),
                event.getShiftKey()
        };

        int numModifiers = modifiers.length;

        boolean hasModifiers = false;

        for ( int i = 0; i < numModifiers; i++ )
        {
            if ( ! modifiers[ i ] )
            {
                continue;
            }

            hasModifiers = true;
            break;
        }

        if ( hasModifiers )
        {
            b.append( "&modifiers=" );

            b.append( modifiers[ 0 ] ? "Alt" : "" );
            b.append( modifiers[ 1 ] ? "Ctrl" : "" );
            b.append( modifiers[ 2 ] ? "Shift" : "" );
        }
        return this;
    }


    private DOMEventUtils buildParamsUIEvent( UIEventImpl event, StringBuilder b )
    {
        b.append( "type=" ).append( event.getType() );
        b.append( "&code=" ).append( event.getKeyCode() );
        b.append( "&char-code=" ).append( event.getCharCode() );
        b.append( "&which=" ).append( event.getWhich() );

        b.append( "&x=" ).append( event.getPageX() );
        b.append( "&y=" ).append( event.getPageY() );

        return this;
    }

    private DOMEventUtils buildParamsMutationEvent( MutationEvent event, StringBuilder b )
    {
        b.append( "type=" ).append( event.getType() );
        b.append( "&attrName=" ).append( event.getAttrName() );
        b.append( "&attrChange=" ).append( event.getAttrChange() );
        b.append( "&newValue=" ).append( event.getNewValue() );
        b.append( "&prevValue=" ).append( event.getPrevValue() );

        Node relatedNode = event.getRelatedNode();

        if ( relatedNode != null)
        {
            b.append( "&node=" ).append( relatedNode.getLocalName() );
        }

        return this;
    }
}
