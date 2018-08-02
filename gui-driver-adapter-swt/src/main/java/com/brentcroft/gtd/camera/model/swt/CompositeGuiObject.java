package com.brentcroft.gtd.camera.model.swt;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AbstractGuiObjectAdapter;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectAdapter;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.ReflectionUtils;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class CompositeGuiObject< T extends Composite > extends ControlGuiObject< T >
{
	private static Logger logger = Logger.getLogger( CompositeGuiObject.class );

	public CompositeGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		super( go, parent, guiObjectConsultant, objectManager );
	}

	@Override
	public boolean hasChildren()
	{
		return onDisplayThread( getObject(), go -> go.getChildren().length > 0 );
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		return Arrays
				.asList( onDisplayThread( getObject(), go -> go.getChildren() ) )
				.stream()
				.map( child -> getManager().adapt( child, this ) )
				.collect( Collectors.toList() );
	}

	static class ParentCompositeGuiObject< T extends Composite > extends CompositeGuiObject< T >
	{
		final Method getItemCountMethod;
		final Method getItemsMethod;

		static Method[] getMethods( Object go )
		{
			Method[] methods = {
					ReflectionUtils.findMethod( go.getClass(), "getItemCount" ),
					ReflectionUtils.findMethod( go.getClass(), "getItems" )
			};

			return methods.length == Arrays
					.asList( methods )
					.stream()
					.filter( Objects::nonNull )
					.count()
							? methods
							: null;
		}

		ParentCompositeGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager, Method[] methods )
		{
			super( go, parent, guiObjectConsultant, objectManager );

			getItemCountMethod = methods[ 0 ];
			getItemsMethod = methods[ 1 ];
		}

		static < T extends Composite > GuiObjectAdapter< T > newAdapter( T go, GuiObjectConsultant< T > guiObjectConsultant,
				CameraObjectManager objectManager, Method[] methods )
		{
			return new AbstractGuiObjectAdapter< T >( ( Class< T > ) go.getClass() )
			{
				{
					logger.info( "Creating ParentCompositeGuiObject: -> " + go.getClass() );
				}

				@Override
				public GuiObject< T > adapt( T t, Gob parent )
				{
					return new ParentCompositeGuiObject< T >( go, parent, guiObjectConsultant, objectManager, methods );
				}

				@Override
				public Class< ParentCompositeGuiObject > getAdapterClass()
				{

					return ParentCompositeGuiObject.class;
				}
			};
		}

		@Override
		public boolean hasChildren()
		{
			return onDisplayThread(
					getObject(),
					go -> ( int ) CameraObjectManager.valueOrRuntimeException( go, getItemCountMethod ) > 0 );
		}

		@Override
		public List< GuiObject< ? > > loadChildren()
		{
			return Arrays
					.asList( onDisplayThread( getObject(), go -> CameraObjectManager.arrayOrRuntimeException( go, getItemsMethod ) ) )
					.stream()
					.map( child -> getManager().adapt( child, this ) )
					.collect( Collectors.toList() );
		}
	}

	static class IndexCompositeGuiObject< T extends Composite > extends ParentCompositeGuiObject< T > implements GuiObject.Index
	{
		final Method getSelectionIndexMethod;
		final Method setSelectionIndexMethod;

		static Method[] getMethods( Object go )
		{
			Method[] methods = {

					ReflectionUtils.findMethod( go.getClass(), "getItemCount" ),
					ReflectionUtils.findMethod( go.getClass(), "getItems" ),

					ReflectionUtils.findMethod( go.getClass(), "getSelectionIndex" ),
					ReflectionUtils.findMethod( go.getClass(), "setSelection", int.class )
			};

			return methods.length == Arrays
					.asList( methods )
					.stream()
					.filter( Objects::nonNull )
					.count()
							? methods
							: null;
		}

		IndexCompositeGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager, Method[] methods )
		{
			super( go, parent, guiObjectConsultant, objectManager, methods );

			getSelectionIndexMethod = methods[ 2 ];
			setSelectionIndexMethod = methods[ 3 ];
		}

		static < T extends Composite > GuiObjectAdapter< T > newAdapter( T go, GuiObjectConsultant< T > guiObjectConsultant,
				CameraObjectManager objectManager, Method[] methods )
		{
			return new AbstractGuiObjectAdapter< T >( ( Class< T > ) go.getClass() )
			{
				{
					logger.info( "Creating IndexCompositeGuiObject: -> " + go.getClass() );
				}

				@Override
				public GuiObject< T > adapt( T t, Gob parent )
				{
					return new IndexCompositeGuiObject< T >( go, parent, guiObjectConsultant, objectManager, methods );
				}

				@Override
				public Class< IndexCompositeGuiObject > getAdapterClass()
				{

					return IndexCompositeGuiObject.class;
				}
			};
		}

		@Override
		public void buildProperties( Element element, Map< String, Object > options )
		{
			super.buildProperties( element, options );

			// the last action in the list is the prime action
			addIndexAction( element, options );
		}

		@Override
		public Integer getItemCount()
		{
			return onDisplayThread(
					getObject(),
					go -> ( int ) CameraObjectManager.valueOrRuntimeException( go, getItemCountMethod ) );
		}

		@Override
		public Integer getSelectedIndex()
		{
			return onDisplayThread(
					getObject(),
					go -> ( int ) CameraObjectManager.valueOrRuntimeException( go, getSelectionIndexMethod ) );
		}

		@Override
		public void setSelectedIndex( int index )
		{
			onDisplayThread(
					getObject(),
					go -> CameraObjectManager.voidOrRuntimeException( go, setSelectionIndexMethod, index ) );

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
	public static < T extends Composite > GuiObjectAdapter< T > getSpecialist( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant,
			CameraObjectManager objectManager )
	{
		Method[] methods = IndexCompositeGuiObject.getMethods( go );

		if ( methods != null )
		{
			return IndexCompositeGuiObject.newAdapter( go, guiObjectConsultant, objectManager, methods );
		}

		methods = ParentCompositeGuiObject.getMethods( go );

		if ( methods != null )
		{
			ParentCompositeGuiObject.newAdapter( go, guiObjectConsultant, objectManager, methods );
		}

		return null;
	}

}
