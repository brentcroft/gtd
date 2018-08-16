package com.brentcroft.gtd.camera;

import static com.brentcroft.util.XmlUtils.getClassIdentifier;
import static java.lang.String.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.brentcroft.gtd.adapter.model.AbstractGuiObjectFactory;
import com.brentcroft.gtd.adapter.model.AttrSpec;
import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectFactory;
import com.brentcroft.gtd.adapter.model.GuiObjectConsultant;
import com.brentcroft.gtd.adapter.utils.HashCacheImpl;
import com.brentcroft.gtd.adapter.utils.ReflectionUtils;
import com.brentcroft.gtd.driver.GuiObjectManager;
import com.brentcroft.gtd.driver.utils.HashCache;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 14/07/2017.
 */
public class CameraObjectManager implements GuiObjectManager< GuiObject< ? > >
{
	private static final Logger logger = Logger.getLogger( CameraObjectManager.class );

	private final HashCache< GuiObject< ? > > hashCache = new HashCacheImpl<>();

	private final static Comparator< GuiObjectFactory< ? > > COMPARATOR = ( h1, h2 ) -> {
		try
		{
			return h1.getOrderKey().compareTo( h2.getOrderKey() );
		}
		catch ( Exception e )
		{
			throw new IllegalStateException( format( "Error comparing keys: h1=[%s], h2=[%s].", h1, h2 ), e );
		}
	};

	private final Map< Class< ? >, GuiObjectFactory< ? > > adaptersByClass = new LinkedHashMap<>();
	private final Map< Class< ? >, GuiObjectFactory< ? > > usedAdaptersByClass = new HashMap<>();

	private final List< GuiObjectFactory< ? > > adaptersByRank = new ArrayList<>();

	public void configure( Properties properties )
	{
	}

	@SuppressWarnings( "unchecked" )
	public < T, H extends GuiObject< T > > Map< Class< T >, Class< H > > getAdapterMap()
	{
		Map< Class< T >, Class< H > > adapterMap = new HashMap<>();

		adaptersByRank
				.stream()
				.forEach( adapter -> adapterMap.put( ( Class< T > ) adapter.handler(), ( Class< H > ) adapter.getFactoryClass() ) );

		return adapterMap;
	}

	public void clear()
	{
		usedAdaptersByClass.clear();
		adaptersByClass.clear();
		adaptersByRank.clear();
		hashCache.gc();
	}

	public void clean()
	{
		usedAdaptersByClass.clear();
		hashCache.gc();
	}

	@Override
	public HashCache< GuiObject< ? > > getHashCache()
	{
		return hashCache;
	}

	@Override
	public GuiObject< ? > adapt( Object object, Gob parent )
	{
		return findAdapter( object, parent ).adapt( object, parent );
	}

	public void addAdapter( GuiObjectFactory< ? > adapter )
	{
		adaptersByClass.put( adapter.handler(), adapter );

		linkSuperAdapters();
	}

	public void addAdapters( Collection< ? extends GuiObjectFactory< ? > > newAdapters )
	{
		newAdapters.forEach( adapter -> {
			GuiObjectFactory< ? > replaced = adaptersByClass.put( adapter.handler(), adapter );

			if ( replaced != null )
			{
				logger.info( format( "Replacing adapter: new=[%s], old=[%s], handler=[%s].", adapter, replaced,
						adapter.handler() ) );
			}

		} );

		linkSuperAdapters();
	}

	/**
	 * For each adapter, iterates over all the other adapters, excluding an already
	 * assigned super adapter, or any candidate who's handler is not assignable from
	 * the adapter's handler, resolving the least super candidate which is assigned
	 * to the adapter.
	 */
	private void linkSuperAdapters()
	{
		// distribute consultants

		// iterates over adapters * adapters
		adaptersByClass
				.values()
				.stream()
				.forEach( adapter -> {
					adaptersByClass
							.values()
							.stream()
							.filter( candidate -> !candidate.equals( adapter ) )
							.filter( candidate -> candidate != adapter.getSuperFactory() )
							.filter( candidate -> candidate.handler().isAssignableFrom( adapter.handler() ) )
							.forEach( candidate -> {
								// must be sequential
								// any candidate might make an improvement - less super
								if ( (adapter.getSuperFactory() == null)
										// switcheroo - we want the least super
										|| adapter.getSuperFactory().handler().isAssignableFrom( candidate.handler() ) )
								{
									adapter.setSuperAdapter( candidate );
								}
							} );
				} );

		adaptersByRank.clear();

		adaptersByRank.addAll( adaptersByClass.values() );

		adaptersByRank.sort( COMPARATOR );

		// ripple up consultants
		// in reverse order
		adaptersByRank
				.stream()
				.collect(
						Collector.of( ArrayDeque::new, ( deq, t ) -> deq.addFirst( t ),
								( d1, d2 ) -> {
									d2.addAll( d1 );
									return d2;
								} ) )
				.stream()
				.map( adapter -> ( GuiObjectFactory< ? > ) adapter )
				.filter( adapter -> adapter.getConsultant() == null )
				.filter( adapter -> adapter.getSuperFactory() != null )
				// since sorted
				.forEach( adapter -> adapter.setConsultant( adapter.getSuperFactory().getConsultant() ) );

		// TODO: half the previous code is meant to isolate this array from being
		// resorted
		adaptersByRank.sort( COMPARATOR );

		usedAdaptersByClass.clear();
	}

	@SuppressWarnings( "unchecked" )
	private < T > GuiObjectFactory< ? super T > findAdapter( T t, Gob parent )
	{
		GuiObjectFactory< T > specificHandler = ( GuiObjectFactory< T > ) usedAdaptersByClass.get( t.getClass() );

		if ( specificHandler != null )
		{
			return specificHandler;
		}

		// then walk through the handlers by rank
		for ( GuiObjectFactory< ? > adapter : adaptersByRank )
		{
			if ( adapter.handles( t ) )
			{
				// maybe adapter can provide a specialised version
				// and we've established it's of T
				GuiObjectFactory< T > specialist = (( GuiObjectFactory< T > ) adapter).getSpecialist( t, parent );

				if ( specialist != null )
				{
					adapter = specialist;
				}

				// late entry - avoid walk again
				usedAdaptersByClass.put( t.getClass(), adapter );

				return ( GuiObjectFactory< T > ) adapter;
			}
		}

		throw new RuntimeException( format(
				"Cannot adapt type [%s]; %s",
				t == null ? null : t.getClass().getName(), t ) );
	}

	public < C, H extends GuiObject< C > > void install( List< FactorySpecification< C, H > > adapters )
	{
		// addAdapters(
		// adapters
		// .entrySet()
		// .stream()
		// .map( entry -> newAdapter( entry.getKey(), entry.getValue() ) )
		// .collect( Collectors.toList() ) );

		// 1.8
		adapters.stream()
				.map( spec -> ( GuiObjectFactory< ? > ) newHardFactory( spec.adapteeClass, spec.adapterClass, spec.adapterGuiObjectConsultant ) )
				// .forEach( adapter -> {
				// adaptersByClass.put( adapter.handler(), adapter );
				// } );
				.forEach( adapter -> addAdapter( adapter ) );
		// 1.9
		// .forEach( this::addAdapter );

		linkSuperAdapters();
	}

	public < C, H extends GuiObject< C > > FactorySpecification< C, H > newFactorySpecification(
			Class< C > adapteeClass,
			Class< H > adapterClass )
	{
		return new FactorySpecification< C, H >( adapteeClass, adapterClass, null );
	}

	public < C, H extends GuiObject< ? super C > > FactorySpecification< C, H > newFactorySpecification(
			Class< C > adapteeClass,
			Class< H > adapterClass,
			GuiObjectConsultant< C > adapterGuiObjectConsultant )
	{
		return new FactorySpecification< C, H >( adapteeClass, adapterClass, adapterGuiObjectConsultant );
	}

	public class FactorySpecification< C, H extends GuiObject< ? super C > >
	{
		public Class< C > adapteeClass;
		public Class< H > adapterClass;
		public GuiObjectConsultant< C > adapterGuiObjectConsultant;

		public FactorySpecification( Class< C > adapteeClass, Class< H > adapterClass,
				GuiObjectConsultant< C > adapterGuiObjectConsultant )
		{
			this.adapteeClass = adapteeClass;
			this.adapterClass = adapterClass;
			this.adapterGuiObjectConsultant = adapterGuiObjectConsultant;
		}
	}


	public < C, H extends GuiObject< C > > GuiObjectFactory< C > newHardFactory(
			Class< C > adapteeClass,
			Class< H > adapterClass,
			GuiObjectConsultant< C > adapterGuiObjectConsultant )
	{
		return new AbstractGuiObjectFactory< C >( adapteeClass )
		{
			private Constructor< H > constructor;

			{
				constructor = ReflectionUtils.findConstructor(
						adapterClass,
						adapteeClass,
						Gob.class,
						GuiObjectConsultant.class,
						CameraObjectManager.class );

				if ( constructor == null )
				{
					throw new RuntimeException( format( "No constructor found for args: class=%s", adapterClass ) );
				}

				setConsultant( adapterGuiObjectConsultant );
			}

			@Override
			public H adapt( C c, Gob parent )
			{
				try
				{
					return constructor.newInstance( c, parent, getConsultant(), CameraObjectManager.this );
				}
				catch ( IllegalAccessException | InstantiationException | InvocationTargetException e )
				{
					throw new RuntimeException( format( "Constructor [%s]", constructor ), e );
				}
			}

			@SuppressWarnings( "unchecked" )
			@Override
			public GuiObjectFactory< C > getSpecialist( C t, Gob parent )
			{
				// looks for declared static getSpecialist on adapterClass
				try
				{
					// Method method = ReflectionUtils.findMethodWithArgs( adapterClass,
					// "getSpecialist", t, parent, getConsultant(), CameraObjectManager.this );

					/*
					 * Only want a specialist of this type (and not of a supertype)
					 */
					Optional< Method > method = Arrays
							.asList( adapterClass.getDeclaredMethods() )
							.stream()
							.filter( m -> m.getName().equals( "getSpecialist" ) )
							.findAny();

					if ( method.isPresent() )
					{
						return ( GuiObjectFactory< C > ) valueOrRuntimeException(
								this, // i.e. the adapter; ignored if static method
								method.get(),
								t,
								parent,
								getConsultant(),
								CameraObjectManager.this
								);
					}
				}
				catch ( Exception e )
				{
					logger.warn( format("Error creating specialist for class [%s]", adapteeClass ), e );
				}
				return null;
			}

			@SuppressWarnings( "unchecked" )
			@Override
			public Class< H > getFactoryClass()
			{
				return adapterClass;
			}

			@Override
			public String toString()
			{
				return adapterClass.getSimpleName();
			}
		};
	}
	

	/**
	 * 
	 * @param adapteeClass
	 * @param adapterClass
	 * @param guiObjectConsultant
	 * @param candidateMethods
	 * @param candidateAttributes
	 * @return
	 */
	public < C, H extends GuiObject< C > > GuiObjectFactory< C > newSoftFactory(
			Class< C > adapteeClass,
			Class< H > adapterClass,
			GuiObjectConsultant< C > guiObjectConsultant,
			Map< String, Object > candidateMethods,
			Map< String, AttrSpec< GuiObject< ? > > > candidateAttributes )
	{
		return new AbstractGuiObjectFactory< C >( adapteeClass )
		{
			private Constructor< H > constructor;
			
			{
				constructor = ReflectionUtils.findConstructor(
						adapterClass,
						adapteeClass,
						Gob.class,
						GuiObjectConsultant.class,
						CameraObjectManager.class,
						Map.class,
						Map.class );

				if ( constructor == null )
				{
					throw new RuntimeException( format( "No constructor found for args: class=%s", adapterClass ) );
				}

				setConsultant( guiObjectConsultant );
			}

			@Override
			public H adapt( C c, Gob parent )
			{
				try
				{
					return constructor.newInstance( c, parent, getConsultant(), CameraObjectManager.this, candidateMethods, candidateAttributes );
				}
				catch ( IllegalAccessException | InstantiationException | InvocationTargetException e )
				{
					throw new RuntimeException( format( "Constructor [%s]", constructor ), e );
				}
			}

			@SuppressWarnings( "unchecked" )
			@Override
			public Class< H > getFactoryClass()
			{
				return adapterClass;
			}
		};
	}	
	

	public static Object voidOrRuntimeException( Object go, Method method, Object... args )
	{
		try
		{
			method.invoke( go, args );
			return null;
		}
		catch ( Exception e )
		{
			throw new RuntimeException( format( "Error calling [%s] with args [%s]", method.getName(), args ), e );
		}
	}

	public static Object valueOrRuntimeException( Object go, Method method, Object... args )
	{
		try
		{
			return method.invoke( go, args );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( format( "Error calling [%s] with args [%s]", method.getName(), args ), e );
		}
	}

	public static Object[] arrayOrRuntimeException( Object go, Method method, Object... args )
	{
		try
		{
			return ( Object[] ) method.invoke( go, args );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( format( "Error calling [%s] with args [%s]", method.getName(), args ), e );
		}
	}

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();

		b.append( format( "%n  %-40s %-40s %-40s %s", "[ adapter ]", "[ super adapter ]", "[ consultant ]",
				"[ handles ]" ) );

		b.append( adaptersByRank.stream()
				.map( adapter -> format( "%n  %-40s %-40s %-40s %s", "(" + adapter.getOrder() + ")" + adapter,
						adapter.getSuperFactory(),
						adapter.getConsultant() == null ? "null"
								: getClassIdentifier( adapter.getConsultant().getClass() ),
						adapter.handler().getName() ) )
				.collect( Collectors.joining() ) );

		return b.toString();
	}

}
