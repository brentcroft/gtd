package com.brentcroft.gtd.camera.model.swt;

import static com.brentcroft.gtd.driver.Backend.XML_NAMESPACE_URI;
import static com.brentcroft.util.XmlUtils.maybeAppendElementAttribute;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectFactory;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.model.SpecialistGuiObject;
import com.brentcroft.gtd.adapter.model.SpecialistGuiObject.SpecialistFunctions;
import com.brentcroft.gtd.adapter.utils.Specialist;
import com.brentcroft.gtd.adapter.utils.SpecialistAttribute;
import com.brentcroft.gtd.adapter.utils.SpecialistMethod;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.gtd.camera.model.swt.WidgetGuiObject.Attributes;
import com.brentcroft.util.TriFunction;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 *
 */
public class ItemGuiObject< T extends Item > extends WidgetGuiObject< T >
{
	private static Logger logger = Logger.getLogger( ItemGuiObject.class );

	public ItemGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
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
			attrSpec.addAll( Arrays.asList( ( AttrSpec< T >[] ) Attr.values() ) );
		}

		return attrSpec;
	}

	static enum Attr implements AttrSpec< Item >
	{
		TEXT( "text", go -> go.getText() );

		final String n;
		final Function< Item, Object > f;

		Attr( String name, Function< Item, Object > f )
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
		public String getAttribute( Item go )
		{
			return onSwtDisplayThreadAsText( go, f );
		}
	}

	/**
	 * This is called by reflection by the CameraObjectManager allowing an
	 * alternative adapter to be provided.
	 * <p/>
	 * 
	 * 
	 * @param go
	 * @param parent
	 * @param guiObjectConsultant
	 * @param objectManager
	 * @return
	 */
	@SuppressWarnings( "unchecked" )
	public static < T extends Item > GuiObjectFactory< T > getSpecialist( T go, Gob parent, GuiObjectConsultant< T > consultant,
			CameraObjectManager objectManager )
	{
		GuiObjectFactory< T > specialist = null;

		if ( consultant.specialise( go ) )
		{
			final Map< String, Object > availableFunctions = SpecialistFunctions.getSpecialistFunctions( go );

			ItemWithControl
					.getSpecialistFunctions( go )
					.entrySet()
					.stream()
					.filter( entry -> entry.getValue() != null )
					.forEach( af -> {
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

			// Map< String, AttrSpec< GuiObject< ? > > > availableAttributes =
			// SpecialistGuiObject.SpecialistAttributes.getAttributes( go );


			List< AttrSpec< ? > > availableAttributes = Attributes.getAttributes( go );

			specialist = objectManager.newSoftFactory( ( Class< T > ) go.getClass(), SpecialistGuiObject.class, consultant, wrappedFunctions, availableAttributes );

			logger.info( String.format( "Adpating [%s] with specialist: %s", go.getClass().getName(), specialist ) );

			return specialist;
		}

		return null;
	}

	public static class ItemWithControlGuiObject< T extends Item > extends ItemGuiObject< T >
	{
		private final Method getControlMethod;

		public ItemWithControlGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager,
				Map< SpecialistMethod, Method > methods )
		{
			super( go, parent, guiObjectConsultant, objectManager );

			getControlMethod = methods.get( ItemWithControl.Functions.GET_CONTROL );
		}

		@Override
		public void buildProperties( Element element, Map< String, Object > options )
		{
			super.buildProperties( element, options );

			maybeAppendElementAttribute( options, element, XML_NAMESPACE_URI, "a:spec", "item" );

			// the last action in the list is the prime action
		}

		@Override
		public boolean hasChildren()
		{
			return onDisplayThread(
					getObject(),
					go -> CameraObjectManager.valueOrRuntimeException( go, getControlMethod ) != null );
		}

		@Override
		public List< GuiObject< ? > > loadChildren()
		{
			List< GuiObject< ? > > children = super.loadChildren();

			Optional
					.ofNullable( onDisplayThread( getObject(), go -> CameraObjectManager.valueOrRuntimeException( go, getControlMethod ) ) )
					.ifPresent( child -> children.add( getManager().adapt( child, this ) ) );

			return children;
		}
	}
}

interface ItemWithControl
{
	public static Map< String, Object > getSpecialistFunctions( Object go )
	{
		return Specialist.extractFunctions( go, Arrays.asList( Functions.values() ) );
	}

	public enum Functions implements SpecialistMethod
	{
		GET_CONTROL( "getControl" );

		private final String methodName;
		private final Class< ? >[] args;

		Functions( String name, Class< ? >... args )
		{
			this.methodName = name;
			this.args = args;
		}

		public String getMethodName()
		{
			return methodName;
		}

		public Class< ? >[] getArgs()
		{
			return args;
		}

		@Override
		public Object getFunction( Object owner )
		{
			return WidgetGuiObject.onDisplayThread( ( Widget ) owner, w -> getFunction( w ) );
		}
	}
}
