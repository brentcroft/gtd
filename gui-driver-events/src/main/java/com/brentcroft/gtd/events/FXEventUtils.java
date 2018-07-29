package com.brentcroft.gtd.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.apache.log4j.Logger;

import static java.lang.String.format;

/**
 * Created by Alaric on 25/05/2017.
 */
public class FXEventUtils extends AbstractEventUtils< Event, EventHandler< Event > >
{
    private final static Logger logger = Logger.getLogger( FXEventUtils.class );


    public EventHandler< Event > getHandler( Consumer< Event > consumer )
    {
        return fxEvent ->
        {
            try
            {
                consumer.accept( fxEvent );
            }
            catch ( Exception ex )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.warn( format( "Ignored FXEvent (exception): [%s]; %s", fxEvent.getEventType(), ex ) );
                }
            }
        };
    }


    private Map< Class< ? extends Event >, Set< EventType<?> > > ignorable = new HashMap<>();

    // TODO: configurise
    {
        Set< EventType<?> > eventTypes = new HashSet<>();

        eventTypes.add( MouseEvent.MOUSE_DRAGGED );
        eventTypes.add( MouseEvent.MOUSE_MOVED );
        eventTypes.add( MouseEvent.MOUSE_ENTERED );
        eventTypes.add( MouseEvent.MOUSE_ENTERED_TARGET );
        eventTypes.add( MouseEvent.MOUSE_EXITED );
        eventTypes.add( MouseEvent.MOUSE_EXITED_TARGET );

        ignorable.put( MouseEvent.class, eventTypes );
    }


    {
        Set< EventType<?> > eventTypes = new HashSet<>();

        eventTypes.add( KeyEvent.KEY_PRESSED );
        eventTypes.add( KeyEvent.KEY_RELEASED );

        ignorable.put( KeyEvent.class, eventTypes );
    }

    public boolean canIgnore( Event event )
    {
        Set< EventType<?> > ignorableTypes = ignorable.get( event.getClass() );

        if ( ignorableTypes != null && ! ignorableTypes.isEmpty() )
        {
            return ignorableTypes.contains( event.getEventType() );
        }

        return false;
    }


    public String getParams( Event fxEvent )
    {

        StringBuilder b = new StringBuilder();

        buildParams( fxEvent, b );

        return b.toString();
    }


    protected FXEventUtils buildParams( Event event, StringBuilder b )
    {
        if ( event instanceof MouseEvent )
        {
            buildParamsMouseEvent( ( MouseEvent ) event, b );
        }
        else if ( event instanceof InputMethodEvent )
        {
            buildParamsInputMethodEvent( ( InputMethodEvent ) event, b );
        }
        else if ( event instanceof KeyEvent )
        {
            buildParamsKeyEvent( ( KeyEvent ) event, b );
        }
        else
        {
            b.append( event.toString() );
        }
        return this;
    }


    protected FXEventUtils buildParamsKeyEvent( KeyEvent ke, StringBuilder b )
    {
        if ( ke.getEventType() == KeyEvent.KEY_TYPED )
        {
            b.append( "char=" ).append( removeAnyNonPrintingCharacters( ke.getCharacter() ) );
        }
        else
        {
            b.append( "code=" ).append( ke.getCode() );
        }

        b.append( "&text=" ).append( removeAnyNonPrintingCharacters( ke.getText() ) );

        boolean[] modifiers = {
                ke.isAltDown(),
                ke.isControlDown(),
                ke.isShiftDown(),
                ke.isShortcutDown(),
                ke.isMetaDown()
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
            b.append( modifiers[ 3 ] ? "Short" : "" );
            b.append( modifiers[ 4 ] ? "Meta" : "" );
        }
        return this;
    }


    protected FXEventUtils buildParamsInputMethodEvent( InputMethodEvent ime, StringBuilder b )
    {
        b.append( "caret=" ).append( ime.getCaretPosition() );
        b.append( "&composed=" ).append( ime.getComposed() );
        b.append( "&committed=" ).append( ime.getCommitted() );
        return this;
    }

    protected FXEventUtils buildParamsMouseEvent( MouseEvent me, StringBuilder b )
    {
        b.append( "button=" ).append( me.getButton().name() );
        b.append( "&buttonId=" ).append( me.getButton().ordinal() );
        b.append( "&clicks=" ).append( me.getClickCount() );
        b.append( "&x=" ).append( me.getX() );
        b.append( "&y=" ).append( me.getY() );
        b.append( "&sceneX=" ).append( me.getSceneX() );
        b.append( "&sceneY=" ).append( me.getSceneY() );
        b.append( "&screenX=" ).append( me.getScreenX() );
        b.append( "&screenY=" ).append( me.getScreenY() );

        boolean[] modifiers = {
                me.isAltDown(),
                me.isControlDown(),
                me.isShiftDown(),

                me.isPrimaryButtonDown(),
                me.isMiddleButtonDown(),
                me.isSecondaryButtonDown(),

                me.isShortcutDown(),
                me.isDragDetect(),
                me.isMetaDown(),
                me.isStillSincePress()
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

            b.append( modifiers[ 3 ] ? "B1" : "" );
            b.append( modifiers[ 4 ] ? "B2" : "" );
            b.append( modifiers[ 5 ] ? "B3" : "" );

            b.append( modifiers[ 6 ] ? "Short" : "" );
            b.append( modifiers[ 7 ] ? "Drag" : "" );
            b.append( modifiers[ 8 ] ? "Meta" : "" );
            b.append( modifiers[ 9 ] ? "Still" : "" );
        }

        return this;
    }


}
