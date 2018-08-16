package com.brentcroft.gtd.camera.model.swt;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.model.GuiObjectFactory;
import com.brentcroft.gtd.adapter.model.SpecialistGuiObject;
import com.brentcroft.gtd.adapter.utils.Specialist;
import com.brentcroft.gtd.adapter.utils.SpecialistMethod;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.TriFunction;
import com.brentcroft.util.Waiter8;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class WidgetGuiObject< T extends Widget > extends SpecialistGuiObject< T >
{
	/**
	 * This is needed or else this class won't be loaded by a hard factory.
	 * <p/>
	 * 
	 * However it will raise an UnsupportedOperationException().
	 * 
	 * @param go
	 * @param parent
	 * @param guiObjectConsultant
	 * @param objectManager
	 */
	public WidgetGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager, null, null );
	}

	public WidgetGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager,
			Map< String, Object > methods, Map< String, AttrSpec< GuiObject< ? > > > extraAttr )
	{
		super( go, parent, guiObjectConsultant, objectManager, methods, extraAttr );
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

	/**
	 * This is called by reflection by the CameraObjectManager allowing an
	 * alternative adapter to be provided.
	 * <p/>
	 * 
	 * 
	 * @param go
	 * @param parent
	 * @param consultant
	 * @param objectManager
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	public static < T extends Widget > GuiObjectFactory< T > getSpecialist( T go, Gob parent, GuiObjectConsultant< T > consultant,
			CameraObjectManager objectManager )
	{
		GuiObjectFactory< T > specialist = null;

		if ( consultant.specialise( go ) )
		{
			final Map< String, Object > availableFunctions = SpecialistFunctions.getSpecialistFunctions( go );

			// currently relying on LinkedHashMap to preserve order
			Functions
					.getSpecialistFunctions( go )
					.entrySet()
					.stream()
					.filter( entry -> entry.getValue() != null )
					.forEachOrdered( af -> {
						availableFunctions.remove( af.getKey() );
						availableFunctions.put( af.getKey(), af.getValue() );
					} );

			final Map< String, Object > wrappedFunctions = availableFunctions
					.entrySet()
					.stream()
					.filter( entry -> entry.getValue() != null )
					.collect( Collectors.toMap( entry -> entry.getKey(), entry -> {
						final Object function = entry.getValue();
						try
						{
							if ( function instanceof Function )
							{
								Function< Object, Object > innerFunction = ( Function< Object, Object > ) entry.getValue();
								Function< Object, Object > outerFunction = widget -> onDisplayThread( ( Widget ) widget, w -> innerFunction.apply( w ) );
								return outerFunction;
							}
							else if ( function instanceof BiFunction )
							{
								BiFunction< Object, Object, Object > innerFunction = ( BiFunction< Object, Object, Object > ) entry.getValue();
								BiFunction< Object, Object, Object > outerFunction = ( widget, args ) -> onDisplayThread( ( Widget ) widget,
										w -> innerFunction.apply( w, args ) );
								return outerFunction;
							}
							else if ( function instanceof TriFunction )
							{
								TriFunction< Object, Object, Object, Object > innerFunction = ( TriFunction< Object, Object, Object, Object > ) entry.getValue();
								TriFunction< Object, Object, Object, Object > outerFunction = ( widget, args, e ) -> onDisplayThread( ( Widget ) widget,
										w -> innerFunction.apply( w, args, e ) );
								return outerFunction;
							}

							throw new IllegalArgumentException( "Unknown function type: " + function );
						}
						catch ( Exception e )
						{
							throw new IllegalStateException( String.format( "Error wrapping function [%s] %s", entry.getKey(), function ), e );
						}
					} ) );

			// availableFunctions = availableFunctions.entrySet()
			// .stream()
			// .filter( Objects::nonNull )
			// .collect( Collectors.toMap( e -> e.getValue(), e -> e.getValue() ) );

			// Map< String, AttrSpec< GuiObject< ? > > > availableAttributes =
			// SpecialistGuiObject.Attributes.getAttributes( go );

			Map< String, AttrSpec< GuiObject< ? > > > availableAttributes = new HashMap<>();

			availableAttributes = availableAttributes
					.entrySet()
					.stream()
					.filter( Objects::nonNull )
					.collect( Collectors.toMap( e -> e.getKey(), e -> new AttrSpec< GuiObject< ? > >()
					{

						@Override
						public String getSpecialAttribute( GuiObject< ? > gob )
						{
							return onDisplayThread( ( Widget ) gob.getObject(), go -> e.getValue().getSpecialAttribute( gob ) );
						}

						@Override
						public String getName()
						{
							return e.getKey();
						}
					} ) );

			specialist = objectManager.newSoftFactory( ( Class< T > ) go.getClass(), WidgetGuiObject.class, consultant, wrappedFunctions, availableAttributes );

			//logger.info( String.format( "Adpating [%s] with specialist: %s", go.getClass().getName(), specialist ) );
		}

		return specialist;
	}

	public enum Functions implements SpecialistMethod
	{
		GET_CHILDREN( SpecialistFunctions.LOAD_CHILDREN, "getChildren" ),
		GET_ITEMS( SpecialistFunctions.LOAD_CHILDREN, "getItems" ),
		GET_CONTROL( SpecialistFunctions.LOAD_CHILDREN, "getControl" ),

		// indexed
		GET_ITEMS_COUNT( SpecialistFunctions.GET_ITEM_COUNT, "getItemCount" ),

		GET_TEXT( SpecialistFunctions.GET_TEXT, "getText" ),
		SET_TEXT( SpecialistFunctions.SET_TEXT, "setText", String.class ),

		GET_SELECTION_INDEX( SpecialistFunctions.GET_SELECTED_INDEX, "getSelectionIndex" ),

		SET_SELECTION( SpecialistFunctions.SET_SELECTED_INDEX, "setSelection", Integer.class ),
		// Combo
		SELECT( SpecialistFunctions.SET_SELECTED_INDEX, "select", Integer.class ),

		GET_PATH( SpecialistFunctions.GET_PATH, "getPath", String.class ),
		SELECT_PATH( SpecialistFunctions.SELECT_PATH, "selectPath", String.class ),
		;

		private final String overridingMethodName;
		private final String overriddenMethodName;
		private final Class< ? >[] args;

		Functions( SpecialistFunctions overridden, String overrideName, Class< ? >... args )
		{
			this.overriddenMethodName = overridden.getMethodName();
			this.overridingMethodName = overrideName;
			this.args = args == null ? new Class< ? >[ 0 ] : args;
		}

		public String getOverridingMethodName()
		{
			return overridingMethodName;
		}

		public String getMethodName()
		{
			return overriddenMethodName;
		}

		public Class< ? >[] getArgs()
		{
			return args;
		}

		@Override
		public Object getFunctionFrom( Object owner )
		{
			return getFunction( owner, getOverridingMethodName() );
		}

		public static Map< String, Object > getSpecialistFunctions( Object go )
		{
			return Specialist.extractFunctions( go, Arrays.asList( values() ) );
		}
	}
}
