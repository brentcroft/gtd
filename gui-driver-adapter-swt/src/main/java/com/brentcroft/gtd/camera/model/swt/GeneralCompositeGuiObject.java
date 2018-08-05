package com.brentcroft.gtd.camera.model.swt;

import static com.brentcroft.gtd.adapter.model.AttrSpec.stringOrNull;
import static com.brentcroft.gtd.driver.Backend.XML_NAMESPACE_URI;
import static com.brentcroft.util.XmlUtils.maybeAppendElementAttribute;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;
import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.ReflectionUtils;
import com.brentcroft.gtd.adapter.utils.Specialist;
import com.brentcroft.gtd.adapter.utils.SpecialistMethod;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

public class GeneralCompositeGuiObject< T extends Composite > extends CompositeGuiObject< T > implements GuiObject.Text, GuiObject.Index
{
	protected Function< T, String > fn_getText;
	protected BiFunction< T, String, Object > fn_setText;

	protected Function< T, Integer > fn_getItemCount;
	protected Function< T, Object[] > fn_getItems;

	protected Function< T, Integer > fn_getSelectionIndex;
	protected BiFunction< T, Integer, Object > fn_setSelection;

	@SuppressWarnings( "unchecked" )
	public GeneralCompositeGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager,
			Map< SpecialistMethod, Object > methods )
	{
		super( go, parent, guiObjectConsultant, objectManager );

		fn_getText = ( Function< T, String > ) methods.get( Functions.General.GET_TEXT );
		fn_setText = ( BiFunction< T, String, Object > ) methods.get( Functions.General.SET_TEXT );

		fn_getItemCount = ( Function< T, Integer > ) methods.get( Functions.General.GET_ITEMS_COUNT );
		fn_getItems = ( Function< T, Object[] > ) methods.get( Functions.General.GET_ITEMS );

		fn_getSelectionIndex = ( Function< T, Integer > ) methods.get( Functions.General.GET_SELECTION_INDEX );
		fn_setSelection = ( BiFunction< T, Integer, Object > ) methods.get( Functions.General.SET_SELECTION );
		
		spec = new ArrayList<>();
		
		if (fn_getText != null)
		{
			spec.add( Attr.TEXT );
		}
		if (fn_getItemCount != null)
		{
			spec.add( Attr.SIZE );
		}
		if (fn_getSelectionIndex != null)
		{
			spec.add( Attr.SELECTED_INDEX );
		}
	}

	@Override
	public void buildProperties( Element element, Map< String, Object > options )
	{
		super.buildProperties( element, options );

		maybeAppendElementAttribute( options, element, XML_NAMESPACE_URI, "a:spec", "general" );

		// the last action in the list is the prime action

		if ( fn_setText != null && fn_getText != null )
		{
			addTextAction( element, options );
		}

		if ( fn_getSelectionIndex != null && fn_setSelection != null )
		{
			addIndexAction( element, options );
		}
	}


	enum Attr implements AttrSpec< GuiObject< ? > >
	{
		SIZE( "size", go -> go.asIndex().getItemCount() ),
		SELECTED_INDEX( "selected-index", go -> go.asIndex().getSelectedIndex() ),		
		TEXT( "text", go -> go.asText().getText() ),
		;

		final String n;
		final Function< GuiObject< ? >, Object > f;

		Attr( String name, Function< GuiObject< ? >, Object > f )
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
		public String getSpecialAttribute( GuiObject< ? > go )
		{
			return stringOrNull( f.apply( go ));
		}
	}

	@Override
	public String getText()
	{
		if ( fn_getText == null )
		{
			throw new UnsupportedOperationException();
		}
		return fn_getText.apply( getObject() );
	}

	@Override
	public void setText( String text )
	{
		if ( fn_setText == null )
		{
			throw new UnsupportedOperationException();
		}
		fn_setText.apply( getObject(), text );
	}

	@Override
	public boolean hasChildren()
	{
		if ( fn_getItemCount == null )
		{
			return super.hasChildren();
		}
		return fn_getItemCount.apply( getObject() ) > 0;
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		if ( fn_getItems == null )
		{
			return super.loadChildren();
		}

		return Arrays
				.asList( fn_getItems.apply( getObject() ) )
				.stream()
				.map( child -> getManager().adapt( child, this ) )
				.collect( Collectors.toList() );
	}

	@Override
	public Integer getItemCount()
	{
		if ( fn_getItemCount == null )
		{
			throw new UnsupportedOperationException();
		}
		return fn_getItemCount.apply( getObject() );
	}

	@Override
	public Integer getSelectedIndex()
	{
		if ( fn_getSelectionIndex == null )
		{
			throw new UnsupportedOperationException();
		}
		return fn_getSelectionIndex.apply( getObject() );
	}

	@Override
	public void setSelectedIndex( int index )
	{
		if ( fn_setSelection == null )
		{
			throw new UnsupportedOperationException();
		}
		fn_setSelection.apply( getObject(), index );
	}

	interface Functions
	{
		static Map< SpecialistMethod, Object > getSpecialistMethods( Object go )
		{
			return Specialist.getSpecialistMethods( go, Arrays.asList( General.values() ) );
		}

		static Map< SpecialistMethod, Object > getSpecialistFunctions( Object go )
		{
			return Specialist.getSpecialistFunctions( go, Arrays.asList( General.values() ) );
		}

		enum General implements SpecialistMethod
		{
			GET_ITEMS_COUNT( "getItemCount" ),
			GET_ITEMS( "getItems" ),

			GET_TEXT( "getText" ),
			SET_TEXT( "setText", String.class ),

			GET_SELECTION_INDEX( "getSelectionIndex" ),
			SET_SELECTION( "setSelection", Integer.class ),

			;

			private final String methodName;
			private final Class< ? >[] args;

			General( String name, Class< ? >... args )
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

				if ( args == null || args.length < 1 )
				{
					Function< Object, Object > fn = t -> WidgetGuiObject.onDisplayThread(
							( Widget ) t, go -> CameraObjectManager.valueOrRuntimeException( go, m ) );

					return fn;
				}
				switch ( args.length )
				{
					case 1:
						BiFunction< Object, Object, Object > fn = ( t, a ) -> WidgetGuiObject.onDisplayThread(
								( Widget ) t, go -> CameraObjectManager.valueOrRuntimeException( t, m, a ) );

						return fn;

				}

				return null;
			}
		}
	}
}