package com.brentcroft.gtd.camera.model.swt;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.model.GuiObjectFactory;
import com.brentcroft.gtd.adapter.model.SpecialistGuiObject;
import com.brentcroft.gtd.adapter.utils.SpecialistAttribute;
import com.brentcroft.gtd.adapter.utils.SpecialistMethod;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.TriFunction;
import com.brentcroft.util.Waiter8;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class WidgetGuiObject< T extends Widget > extends SpecialistGuiObject< T > implements GuiObject.Click
{
	protected Function< T, Rectangle > fn_getBounds;
	protected TriFunction< T, Integer, Integer, Point > fn_toDisplay;
	protected Function< T, Display > fn_getDisplay;
	protected Function< T, Object[] > fn_getModelItems;

	protected boolean isListModel = false;

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
		this( go, parent, guiObjectConsultant, objectManager, null, null );
	}

	@SuppressWarnings( "unchecked" )
	public WidgetGuiObject(
			T go, Gob parent,
			GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager,
			Map< String, Object > methods,
			List< AttrSpec< T > > attr )
	{
		super( go, parent, guiObjectConsultant, objectManager, methods, attr );

		if ( methods != null )
		{
			fn_getBounds = ( Function< T, Rectangle > ) methods.get( Functions.GET_BOUNDS.getMethodName() );
			fn_toDisplay = ( TriFunction< T, Integer, Integer, Point > ) methods.get( Functions.GET_TO_DISPLAY.getMethodName() );
			fn_getDisplay = ( Function< T, Display > ) methods.get( Functions.GET_DISPLAY.getMethodName() );

			fn_getModelItems = ( Function< T, Object[] > ) methods.get( Functions.GET_LIST_MODEL_ITEMS.getMethodName() );
		}
		
		if ( fn_getItemCount != null && fn_getSelectionIndex != null && fn_getModelItems != null )
		{
			isListModel = getConsultant().isListModel( go );
		}

	}

	@Override
	public boolean hasChildren()
	{
		if ( isListModel )
		{
			return false;
		}
		return super.hasChildren();
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		if ( fn_getBounds != null && fn_toDisplay != null && fn_getDisplay != null )
		{
			addClickAction( element, options );
		}

		if ( isListModel )
		{
			Optional
					.ofNullable( onDisplayThread( getObject(), fn_getModelItems ) )
					.ifPresent( items -> appendListModelElement(
							element,
							options,
							getItemCount(),
							Arrays
									.asList( items )
									.stream()
									.map( item -> item.toString() )
									.collect( Collectors.toList() )
									.toArray( new String[ 0 ] )
					) );
		}
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
			// only ever known to be null when is a mock!
			if ( widget == null || widget.getDisplay() == null )
			{
				return null;
			}

			boolean isSynch = true;

			if ( !widget.getDisplay().isDisposed() )
			{
				if ( isSynch )
				{
					widget.getDisplay().syncExec( () -> {
						value[ 0 ] = function.apply( widget );
					} );
				}
				else
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
							value.wait( 10 * 1000 );
						}
					}
					catch ( InterruptedException e )
					{
						logger.warn( "Interrupted waiting for Display Thread task to complete." );
					}
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
			specialist = objectManager.newSoftFactory(
					( Class< T > ) go.getClass(),
					WidgetGuiObject.class,
					consultant,
					wrapAvailableFunctions(
							getAvailableFunctions( go, ( haplotype, availableFunctions ) -> {
								installFunctions( haplotype, parent, availableFunctions );
								return null;
							} )
					),
					wrapAvailableAttributes( Attributes.getAttributes( go ) )
			);
		}

		return specialist;
	}

	protected static final < T extends Widget > Map< String, Object > getAvailableFunctions( T go, BiFunction< T, Map< String, Object >, Object > installer )
	{
		final Map< String, Object > availableFunctions = SpecialistFunctions.getSpecialistFunctions( go );

		installer.apply( go, availableFunctions );

		return availableFunctions;
	}

	/**
	 * Override, and call ${super.class}.installFunctions().
	 * 
	 * @param go
	 * @param fns
	 */
	public static < T extends Widget > void installFunctions( T go, Gob parent, final Map< String, Object > fns )
	{
		// currently relying on LinkedHashMap to preserve order
		Functions
				.getSpecialistFunctions( go, parent )
				.entrySet()
				.stream()
				.filter( entry -> entry.getValue() != null )
				.forEachOrdered( af -> {
					fns.remove( af.getKey() );
					fns.put( af.getKey(), af.getValue() );
				} );
	}

	@SuppressWarnings( "unchecked" )
	public static final Map< String, Object > wrapAvailableFunctions( final Map< String, Object > availableFunctions )
	{
		return availableFunctions
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
	}

	public static final < V extends Widget > List< AttrSpec< ? > > wrapAvailableAttributes( final List< AttrSpec< V > > availableAttributes )
	{
		return availableAttributes
				.stream()
				.map( as -> new AttrSpec< V >()
				{
					@Override
					public String getName()
					{
						return as.getName();
					}

					@Override
					public String getAttribute( V go )
					{
						return AttrSpec.stringOrNull( onDisplayThread( go, a -> as.getAttribute( a ) ) );
					}
				} )
				.collect( Collectors.toList() );
	}

	@Override
	public int[] getLocation()
	{
		if ( fn_getBounds == null )
		{
			throw new UnsupportedOperationException();
		}

		Rectangle p = onDisplayThread( getObject(), fn_getBounds );

		return new int[] { p.x, p.y, p.width, p.height };
	}

	@Override
	public int[] getObjectLocationOnScreen()
	{
		if ( fn_getBounds == null || fn_toDisplay == null )
		{
			return super.getObjectLocationOnScreen();
		}

		int[] r = getLocation();

		int centreX = r[ 0 ] + r[ 2 ] / 2;
		int centreY = r[ 1 ] + r[ 3 ] / 2;

		Point p = onDisplayThread( getObject(), a -> fn_toDisplay.apply( a, centreX, centreY ) );

		return new int[] { p.x, p.y };
	}

	@Override
	public void click()
	{
		click( 1 );
	}

	@Override
	public void rightClick()
	{
		click( 3 );
	}

	public void click( int mouseButton )
	{
		if ( fn_getBounds == null || fn_toDisplay == null || fn_getDisplay == null )
		{
			return;
		}

		long delay = 80;

		Display targetDisplay = onDisplayThread( getObject(), fn_getDisplay );

		int[] locOnScreen = getObjectLocationOnScreen();

		final Point[] pt = { new Point( locOnScreen[ 0 ], locOnScreen[ 1 ] ) };

		processEvents(
				new Event[] {
						new Event()
						{
							{
								type = SWT.MouseMove;
								display = targetDisplay;
								x = pt[ 0 ].x;
								y = pt[ 0 ].y;
							}
						},
						new Event()
						{
							{
								type = SWT.MouseDown;
								display = targetDisplay;
								button = mouseButton;
							}
						},
						new Event()
						{
							{
								type = SWT.MouseUp;
								display = targetDisplay;
								button = mouseButton;
							}
						}

				}, delay );
	}

	public enum Functions implements SpecialistMethod
	{
		// click action
		GET_BOUNDS( "GET_BOUNDS", "getBounds" ),
		GET_TO_DISPLAY( "TO_DISPLAY", "toDisplay", int.class, int.class ),
		GET_DISPLAY( "GET_DISPLAY", "getDisplay" ),
		GET_PARENT_TO_DISPLAY( "TO_DISPLAY", "getParent.toDisplay", int.class, int.class ),
		GET_PARENT_DISPLAY( "GET_DISPLAY", "getParent.getDisplay" ),

		//
		GET_MENU_BAR( SpecialistFunctions.LOAD_CHILDREN, "getMenuBar", Type.EXTEND ),
		GET_MENU( SpecialistFunctions.LOAD_CHILDREN, "getMenu", Type.EXTEND ),
		GET_CONTROL( SpecialistFunctions.LOAD_CHILDREN, "getControl", Type.EXTEND ),
		GET_ITEMS( SpecialistFunctions.LOAD_CHILDREN, "getItems", Type.EXTEND ),
		GET_CHILDREN( SpecialistFunctions.LOAD_CHILDREN, "getChildren", Type.EXTEND ),

		// indexed action
		GET_ITEMS_COUNT( SpecialistFunctions.GET_ITEM_COUNT, "getItemCount" ),

		// list mode items
		GET_LIST_MODEL_ITEMS( "MODEL_ITEMS", "getItems" ),

		// text action
		GET_TEXT( SpecialistFunctions.GET_TEXT, "getText" ),
		SET_TEXT( SpecialistFunctions.SET_TEXT, "setText", String.class ),

		// index action
		GET_SELECTION_INDEX( SpecialistFunctions.GET_SELECTED_INDEX, "getSelectionIndex" ),

		SET_SELECTION( SpecialistFunctions.SET_SELECTED_INDEX, "setSelection", int.class ),
		// Combo & List
		SELECT( SpecialistFunctions.SET_SELECTED_INDEX, "select", int.class ),

		// tree action
		GET_PATH( SpecialistFunctions.GET_PATH, "getPath", String.class ),
		SELECT_PATH( SpecialistFunctions.SELECT_PATH, "selectPath", String.class ),

		;

		private final String overridingMethodName;
		private final String overriddenMethodName;
		private final Class< ? >[] args;
		private final Type type;

		Functions( SpecialistMethod overridden, String overrideName, Class< ? >... args )
		{
			this( overridden.getMethodName(), overrideName, Type.REPLACE, args );
		}

		Functions( SpecialistMethod overridden, String overrideName, Type type, Class< ? >... args )
		{
			this( overridden.getMethodName(), overrideName, type, args );
		}

		Functions( String overridden, String overrideName, Class< ? >... args )
		{
			this( overridden, overrideName, Type.REPLACE, args );
		}

		Functions( String overridden, String overrideName, Type type, Class< ? >... args )
		{
			this.overriddenMethodName = overridden;
			this.overridingMethodName = overrideName;
			this.args = args == null ? new Class< ? >[ 0 ] : args;
			this.type = type;
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
		public Object getFunction( Object owner )
		{
			return getFunction( owner, getOverridingMethodName() );
		}

		@SuppressWarnings( "unchecked" )
		public static Map< String, Object > getSpecialistFunctions( Widget go, Gob parent )
		{
			Map< String, Object > functions = new LinkedHashMap<>();

			Arrays
					.asList( values() )
					.stream()
					.filter( m -> !m.getOverridingMethodName().contains( "." ) )
					.forEachOrdered( m -> Optional
							.ofNullable( m.getFunction( go ) )
							.ifPresent( extensionFn -> {

								switch ( m.type )
								{
									case EXTEND:

										Function< Object, List< GuiObject< ? > > > existingFn = ( Function< Object, List< GuiObject< ? > > > ) functions
												.remove( m.getMethodName() );

										Function< Widget, Object > newFn = w -> {

											List< GuiObject< ? > > gobs = existingFn == null ? null : existingFn.apply( w );

											gobs = extend(
													w,
													( GuiObject< ? > ) parent,
													gobs,
													( Function< Object, Object > ) extensionFn
											);

											return gobs;
										};

										functions.put( m.getMethodName(), newFn );

										break;

									default:
										functions.remove( m.getMethodName() );
										functions.put( m.getMethodName(), extensionFn );
										break;
								}

							} ) );

			Arrays
					.asList( values() )
					.stream()
					.filter( m -> m.getOverridingMethodName().contains( "." ) )
					.forEachOrdered( a -> Optional
							.ofNullable( ( Function< Widget, Widget > ) a.getFunction( go, a.getOverridingMethodName().substring( 0, a.getOverridingMethodName().indexOf( "." ) ),
									new Class[ 0 ] ) )
							.ifPresent( firstFn -> {

								// need to get haplotype of other Widget now
								// so can look up next method
								Widget otherW = onDisplayThread( go, firstFn );

								if ( otherW != null )
								{
									String secondFnName = a.getOverridingMethodName().substring( a.getOverridingMethodName().indexOf( "." ) + 1 );

									// method of otherW
									Object secondFnRaw = a.getFunction( otherW, secondFnName, a.getArgs() );

									Object composedFn = null;

									if ( secondFnRaw != null )
									{
										try
										{
											if ( secondFnRaw instanceof Function )
											{
												Function< Widget, Object > secondFn = ( Function< Widget, Object > ) secondFnRaw;

												Function< Widget, Object > cFn = x -> secondFn.apply( firstFn.apply( x ) );

												composedFn = cFn;
											}
											else if ( secondFnRaw instanceof BiFunction )
											{
												BiFunction< Widget, Widget, Object > secondFn = ( BiFunction< Widget, Widget, Object > ) secondFnRaw;

												BiFunction< Widget, Widget, Object > cFn = ( x, y ) -> secondFn.apply( firstFn.apply( x ), y );

												composedFn = cFn;
											}
											else if ( secondFnRaw instanceof TriFunction )
											{
												TriFunction< Widget, Object, Object, Object > secondFn = ( TriFunction< Widget, Object, Object, Object > ) secondFnRaw;

												TriFunction< Widget, Object, Object, Object > cFn = ( x, y, z ) -> secondFn.apply( firstFn.apply( x ), y, z );

												composedFn = cFn;
											}
											else
											{
												throw new IllegalArgumentException( "Unknown function type: " + secondFnRaw );
											}
										}
										catch ( Exception e )
										{
											throw new IllegalStateException( String.format( "Error wrapping function [%s] %s", secondFnName, secondFnRaw ), e );
										}

										if ( composedFn != null )
										{
											functions.remove( a.getMethodName() );
											functions.put( a.getMethodName(), composedFn );
										}
									}
								}
							} ) );

			return functions;
		}
	}

	public enum Attributes implements SpecialistAttribute
	{
		ENABLED( "enabled", "isEnabled" ),
		VISIBLE( "visible", "isVisible" ),
		FOCUS( "focus", "isFocusControl" ),
		TOOLTIP( "tooltip", "getToolTipText" ),

		INDEX( "index", "getParent.indexOf" ),;

		private final String n;
		private final String m;
		private final Class< ? >[] args;

		Attributes( String name, String methodName, Class< ? >... args )
		{
			this.n = name;
			this.m = methodName;
			this.args = args;
		}

		@Override
		public String getName()
		{
			return n;
		}

		@Override
		public String getMethodName()
		{
			return m;
		}

		@Override
		public Class< ? >[] getArgs()
		{
			return args;
		}

		@SuppressWarnings( "unchecked" )
		public static < V extends Widget > List< AttrSpec< V > > getAttributes( Widget w )
		{
			Map< String, AttrSpec< V > > attributes = new LinkedHashMap<>();

			Arrays
					.asList( values() )
					.stream()
					.filter( v -> !v.getMethodName().contains( "." ) )
					.forEach( a -> Optional
							.ofNullable( ( Function< Widget, Object > ) a.getFunction( w ) )
							.ifPresent( fn -> {
								attributes.remove( a.getName() );
								attributes.put( a.getName(), new AttrSpec< V >()
								{
									@Override
									public String getName()
									{
										return a.getName();
									}

									@Override
									public String getAttribute( Widget go )
									{
										return AttrSpec.stringOrNull( fn.apply( go ) );
									}
								} );
							} ) );

			Arrays
					.asList( values() )
					.stream()
					.filter( v -> v.getMethodName().contains( "." ) )
					.forEach( a -> Optional
							.ofNullable( ( Function< Widget, Widget > ) a.getFunction( w, a.getMethodName().substring( 0, a.getMethodName().indexOf( "." ) ) ) )
							.ifPresent( firstFn -> {

								// need to get haplotype of other Widget now
								// so can look up next method
								Widget otherW = onDisplayThread( w, firstFn );

								if ( otherW != null )
								{
									String secondFnName = a.getMethodName().substring( a.getMethodName().indexOf( "." ) + 1 );

									// method of otherW
									Object secondFnRaw = a.getFunction( otherW, secondFnName, new Class[] { w.getClass() } );

									if ( secondFnRaw instanceof BiFunction )
									{
										BiFunction< Widget, Widget, Object > secondFn = ( BiFunction< Widget, Widget, Object > ) secondFnRaw;

										attributes.remove( a.getName() );
										attributes.put( a.getName(), new AttrSpec< V >()
										{

											@Override
											public String getName()
											{
												return a.getName();
											}

											@Override
											public String getAttribute( Widget go )
											{
												return AttrSpec.stringOrNull( secondFn.apply( firstFn.apply( go ), go ) );
											}
										} );
									}
								}
							} ) );

			return new ArrayList<>( attributes.values() );
		}
	}
}
