package com.brentcroft.gtd.adapter.model;

import static com.brentcroft.gtd.adapter.model.AttrSpec.stringOrNull;
import static com.brentcroft.gtd.driver.Backend.XML_NAMESPACE_URI;
import static com.brentcroft.util.XmlUtils.maybeAppendElementAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.w3c.dom.Element;

import com.brentcroft.gtd.adapter.utils.Specialist;
import com.brentcroft.gtd.adapter.utils.SpecialistMethod;
import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;

public class SpecialistGuiObject< T > extends DefaultGuiObject< T > implements GuiObject.Text, GuiObject.Index, GuiObject.Tree, GuiObject.Table
{

	protected Function< T, String > fn_getText;
	protected BiFunction< T, String, Object > fn_setText;

	protected Function< T, Object > fn_loadChildren;
	protected Function< T, Integer > fn_getItemCount;

	protected Function< T, Integer > fn_getSelectionIndex;
	protected BiFunction< T, Integer, Object > fn_setSelection;

	protected BiFunction< T, String, String > fn_getPath;
	protected BiFunction< T, String, String > fn_selectPath;

	@SuppressWarnings( "unchecked" )
	public SpecialistGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager,
			Map< String, Object > methods, Map< String, AttrSpec< GuiObject< ? > > > extraAttr )
	{
		super( go, parent, guiObjectConsultant, objectManager );

		if ( methods != null )
		{
			fn_loadChildren = ( Function< T, Object > ) methods.get( SpecialistFunctions.LOAD_CHILDREN.getMethodName() );

			fn_getText = ( Function< T, String > ) methods.get( SpecialistFunctions.GET_TEXT.getMethodName() );
			fn_setText = ( BiFunction< T, String, Object > ) methods.get( SpecialistFunctions.SET_TEXT.getMethodName() );

			fn_getItemCount = ( Function< T, Integer > ) methods.get( SpecialistFunctions.GET_ITEM_COUNT.getMethodName() );

			fn_getSelectionIndex = ( Function< T, Integer > ) methods.get( SpecialistFunctions.GET_SELECTED_INDEX.getMethodName() );
			fn_setSelection = ( BiFunction< T, Integer, Object > ) methods.get( SpecialistFunctions.SET_SELECTED_INDEX.getMethodName() );

			fn_getPath = ( BiFunction< T, String, String > ) methods.get( SpecialistFunctions.GET_PATH.getMethodName() );
			fn_selectPath = ( BiFunction< T, String, String > ) methods.get( SpecialistFunctions.SELECT_PATH.getMethodName() );
		}

		spec = (extraAttr == null)
				? new ArrayList<>()
				: extraAttr
						.entrySet()
						.stream()
						.map( attr -> attr.getValue() )
						.collect( Collectors.toList() );

		if ( fn_getText != null )
		{
			spec.add( SpecialistAttributes.TEXT );
		}
		if ( fn_getItemCount != null )
		{
			spec.add( SpecialistAttributes.SIZE );
		}
		if ( fn_getSelectionIndex != null )
		{
			spec.add( SpecialistAttributes.SELECTED_INDEX );
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
		return true;
	}

	@Override
	public List< GuiObject< ? > > loadChildren()
	{
		if ( fn_loadChildren == null )
		{
			return super.loadChildren();
		}

		Object candidate = fn_loadChildren.apply( getObject() );

		if ( candidate == null )
		{
			return null;
		}

		if ( candidate.getClass().isArray() )
		{
			return Arrays
					.asList( ( Object[] ) candidate )
					.stream()
					.map( child -> getManager().adapt( child, this ) )
					.collect( Collectors.toList() );
		}

		return Arrays
				.asList( candidate )
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

	@Override
	public String getPath( String path )
	{
		if ( fn_getPath == null )
		{
			throw new UnsupportedOperationException();
		}
		return fn_getPath.apply( getObject(), path );
	}

	@Override
	public void selectPath( String path )
	{
		if ( fn_selectPath == null )
		{
			throw new UnsupportedOperationException();
		}
		fn_selectPath.apply( getObject(), path );
	}

	public enum SpecialistAttributes implements AttrSpec< GuiObject< ? > >
	{
		SIZE( "size", go -> go.asIndex().getItemCount() ),
		SELECTED_INDEX( "selected-index", go -> go.asIndex().getSelectedIndex() ),
		TEXT( "text", go -> go.asText().getText() ),
		;

		final String n;
		final Function< GuiObject< ? >, Object > f;
		private final Class< ? >[] args;

		SpecialistAttributes( String name, Function< GuiObject< ? >, Object > f, Class< ? >... args )
		{
			this.n = name;
			this.f = f;
			this.args = args;
		}

		@Override
		public String getName()
		{
			return n;
		}

		@Override
		public String getSpecialAttribute( GuiObject< ? > go )
		{
			return stringOrNull( f.apply( go ) );
		}

		public static Map< String, AttrSpec< GuiObject< ? > > > getAttributes( Object go )
		{
			return Arrays
					.asList( values() )
					.stream()
					.collect( Collectors.toMap( rm -> rm.getName(), Function.identity() ) );
		}

	}

	public enum SpecialistFunctions implements SpecialistMethod
	{
		LOAD_CHILDREN( "loadChildren" ),
		GET_ITEM_COUNT( "getItemCount" ),
		GET_TEXT( "getText" ),
		SET_TEXT( "setText", String.class ),
		GET_SELECTED_INDEX( "getSelectedIndex" ),
		SET_SELECTED_INDEX( "setSelectedIndex", Integer.class ),
		GET_PATH( "getPath", String.class ),
		SELECT_PATH( "selectPath", String.class ),
		;

		private final String methodName;
		private final Class< ? >[] args;

		SpecialistFunctions( String name, Class< ? >... args )
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
			return getFunction( owner );
		}

		/**
		 * Provides a map of named functions, as specified by this enum, on a "go".
		 * <p/>
		 * 
		 * Essentially this calls <code>getFunctionFrom( go )</code> on each enum
		 * member.
		 * 
		 * @param go
		 *            the sample object (providing a class for method mining)
		 * @return
		 */
		public static Map< String, Object > getSpecialistFunctions( Object go )
		{
			return Specialist.extractFunctions( go, Arrays.asList( values() ) );
		}
	}

}