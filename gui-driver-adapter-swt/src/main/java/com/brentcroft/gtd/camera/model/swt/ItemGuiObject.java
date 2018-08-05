package com.brentcroft.gtd.camera.model.swt;

import static com.brentcroft.gtd.driver.Backend.XML_NAMESPACE_URI;
import static com.brentcroft.util.XmlUtils.maybeAppendElementAttribute;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.swt.widgets.Item;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectAdapter;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.ReflectionUtils;
import com.brentcroft.gtd.adapter.utils.Specialist;
import com.brentcroft.gtd.adapter.utils.SpecialistMethod;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 *
 */
public class ItemGuiObject< T extends Item > extends WidgetGuiObject< T >
{
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
	public static < T extends Item > GuiObjectAdapter< T > getSpecialist( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		Map< SpecialistMethod, Object > candidate = ItemWithControl.getSpecialistMethods( go );

		if ( ItemWithControl.isComplete(candidate) )
		{
			return objectManager.newAdapter( ( Class< T > ) go.getClass(), ItemWithControlGuiObject.class, guiObjectConsultant, candidate );
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

			getControlMethod = methods.get( ItemWithControl.Required.GET_CONTROL );
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
	static Map< SpecialistMethod, Object > getSpecialistMethods( Object go )
	{
		return Specialist.getSpecialistMethods( go, Arrays.asList( Required.values() ) );
	}
	
	static boolean isComplete(Map< SpecialistMethod, Object > methods)
	{
		return Required.values().length == methods.size();
	}

	enum Required implements SpecialistMethod
	{
		GET_CONTROL( "getControl" );

		private final String methodName;
		private final Class< ? >[] args;

		Required( String name, Class< ? >... args )
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
		public Object getFunctionFrom( Object owner )
		{
			Method m = ReflectionUtils.findMethod( 
					owner.getClass(),
					getMethodName(),
					getArgs()
			);
			
			if ( m == null )
			{
				return null;
			}
			
			if ( args == null || args.length < 1)
			{
				Function< Object, Object > fn = t -> CameraObjectManager.valueOrRuntimeException( t, m, null );
			
				return fn;
			}
			switch(args.length)
			{
				case 1:
					BiFunction< Object, Object, Object > fn = (t, a) -> CameraObjectManager.valueOrRuntimeException( t, m, a );
					
					return fn;
					
			}

			return null;
		}		
	}
}
