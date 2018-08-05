package com.brentcroft.gtd.camera.model.swt;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.DefaultGuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.Waiter8;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class WidgetGuiObject< T extends Widget > extends DefaultGuiObject< T >
{
	public WidgetGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public List< AttrSpec< T > > loadAttrSpec()
	{
		if ( attrSpec == null )
		{
			attrSpec = super.loadAttrSpec();
			attrSpec.addAll( Arrays.asList( ( AttrSpec< T >[] ) WidgetAttr.values() ) );
		}

		return attrSpec;
	}

	enum WidgetAttr implements AttrSpec< Widget >
	{
		GUID( "guid", go -> go.getData( "GUID" ) );

		final String n;
		final Function< Widget, Object > f;

		WidgetAttr( String name, Function< Widget, Object > f )
		{
			this.n = name;
			this.f = f;
		}

		@Override
		public String getName()
		{
			return n;
		}

		@Override
		public String getAttribute( Widget go )
		{
			return onSwtDisplayThreadAsText( go, f );
		}
	}

	/**
	 *
	 * @param t
	 *            A Widget
	 * @param f
	 *            A Function on a Widget
	 * @return The result of calling the Function on the Widget on the Widget's
	 *         display thread as a String
	 */
	protected static < T extends Widget > String onSwtDisplayThreadAsText( T t, Function< T, Object > f )
	{
		Object value = onDisplayThread( t, f );
		return value == null ? null : value.toString();
	}

	/**
	 *
	 * @param t
	 *            A type of Widget.
	 * @param f
	 *            A Function on the type returning a Value.
	 * @return The result of calling the Function on the type on the type's display
	 *         thread.
	 */
	@SuppressWarnings( "unchecked" )
	public static < W extends Widget, V > V onDisplayThread( W widget, Function< W, V > function )
	{
		Object[] value = { null };

		try
		{
			if ( !widget.getDisplay().isDisposed() )
			{
				widget.getDisplay().asyncExec( () -> {
					try
					{
						value[ 0 ] = function.apply( widget );
					}
					finally
					{
						synchronized ( value )
						{
							value.notifyAll();
						}
					}
				} );

				try
				{
					synchronized ( value )
					{
						value.wait( 100 );
					}
				}
				catch ( InterruptedException e )
				{
					logger.warn( "Interrupted waiting for Display Thread task to complete." );
				}
			}
			else
			{
				logger.warn( format( "Display already disposed" ) );
			}
		}
		catch ( SWTException e )
		{
			// duh... because display is disposed
			logger.warn( format( "SWTException: %s", e.getMessage() ) );
		}

		return ( V ) value[ 0 ];
	}

	/**
	 * Post each event to each event's Display, waiting for the given delay after
	 * each post.
	 *
	 * @param events
	 * @param delay
	 */
	protected static void processEvents( Event[] events, long delay )
	{
		Arrays
				.asList( events )
				.stream()
				.forEach( event -> {
					if ( event.display == null )
					{
						throw new NullPointerException( "Event.display is null" );
					}

					if ( !event.display.isDisposed() )
					{
						event.display.asyncExec( () -> event.display.post( event ) );
						Waiter8.delay( delay );
					}
				} );
	}

}
