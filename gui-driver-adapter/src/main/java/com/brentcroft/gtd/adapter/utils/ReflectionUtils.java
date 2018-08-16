package com.brentcroft.gtd.adapter.utils;

import static java.lang.String.format;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This finds the first, "least super", method that is compatible with the
 * arguments.
 * <p>
 * <p>
 * Created by adobson on 06/12/2016.
 */
public class ReflectionUtils
{
	private final static Logger logger = Logger.getLogger( ReflectionUtils.class );

	private final static boolean SUPPRESS_THROWABLE = true;

	private final static Object[] EMPTY_OBJECT_ARRAY = {};

	// represents the class of null values
	private class Anything
	{
	}

	private final static Class< ? >[] NO_PARAMS = {};

	public static void setLoggerlevel( Level level )
	{
		logger.setLevel( level );
	}

	public static Class< ? >[] getParamTypes( Object... args )
	{
		if ( args == null )
		{
			return NO_PARAMS;
		}

		Class< ? >[] pTypes = new Class[ args.length ];
		for ( int i = 0, n = args.length; i < n; i++ )
		{
			if ( args[ i ] == null )
			{
				// obviously can't actually be a parameter
				pTypes[ i ] = Anything.class;
			}
			else
			{
				pTypes[ i ] = args[ i ].getClass();
			}
		}

		return pTypes;
	}

	/**
	 * Note that although superficially similar to other ReflectionUtils classes
	 * (e.g. Spring, Apache),
	 * <p>
	 * This checks whether the target args are assignable to a candidate method's
	 * arguments.
	 * <p>
	 * Hence finds valid method candidates that other implementations might miss!
	 *
	 * @param clazz
	 * @param methodName
	 * @param args
	 * @return the Method if found otherwise null (i.e. does not throw
	 *         NoSuchMethodException)
	 */
	public static Method findMethodWithArgs( Class< ? > clazz, String methodName, Object... args )
	{
		if ( args == null )
		{
			args = EMPTY_OBJECT_ARRAY;
		}

		Class< ? >[] pTypes = getParamTypes( args );

		return findMethod( clazz, methodName, pTypes );
	}

	/**
	 * Note that although superficially similar to other ReflectionUtils classes
	 * (e.g. Spring, Apache),
	 * <p>
	 * This checks whether the target parameters are assignable to a candidate
	 * method's arguments.
	 * <p>
	 * Hence finds valid method candidates that other implementations might miss!
	 *
	 * @param clazz
	 * @param methodName
	 * @param paramTypes
	 * @return the Method if found otherwise null (i.e. does not throw
	 *         NoSuchMethodException)
	 */
	public static Method findMethod( Class< ? > clazz, String methodName, Class< ? >... paramTypes )
	{
		try
		{
			// try simple access first
			return clazz.getMethod( methodName, paramTypes );
		}
		catch ( NoSuchMethodException e )
		{
			// try for compatible param types
			final Method[] methods = clazz.isInterface()
					? clazz.getDeclaredMethods()
					: clazz.getMethods();

			if ( (methods != null) && (methods.length > 0) )
			{
				for ( Method m : methods )
				{
					if ( methodName.equals( m.getName() )
							&& compatibleParamTypes( paramTypes, m.getParameterTypes() ) )
					{
						if ( logger.isDebugEnabled() )
						{
							logger.debug( format( "Class [%s] has compatible method [%s]: %s.",
									clazz.getSimpleName(),
									methodName,
									m ) );
						}

						return m;
					}
				}
			}

			if ( logger.isEnabledFor( Level.DEBUG ) )
			{
				StringBuilder b = new StringBuilder();

				if ( paramTypes != null )
				{
					for ( int i = 0; i < paramTypes.length; i++ )
					{
						if ( i > 0 )
						{
							b.append( ", " );
						}
						Class< ? > c = paramTypes[ i ];
						b.append( (c == Anything.class) ? "?" : c.getName() );
					}
				}

				logger.debug( format( "Class [%s] has no compatible method named [%s] with args [%s].",
						clazz.getSimpleName(),
						methodName,
						b ) );
			}

			return null;
		}
		catch ( Throwable t )
		{
			if ( SUPPRESS_THROWABLE )
			{
				return null;
			}
			throw new RuntimeException( t );
		}
	}

	public static < T > Constructor< T > findConstructorWithArgs( Class< T > clazz, Object... args )
	{
		if ( args == null )
		{
			args = EMPTY_OBJECT_ARRAY;
		}

		Class< ? >[] pTypes = getParamTypes( args );

		return findConstructor( clazz, pTypes );
	}

	/**
	 * Note that although superficially similar to other ReflectionUtils classes
	 * (e.g. Spring, Apache),
	 * <p>
	 * This checks whether the target parameters are assignable to a candidate
	 * constructor's arguments.
	 * <p>
	 * Hence finds valid constructor candidates that other implementations might
	 * miss!
	 *
	 * @param clazz
	 * @param paramTypes
	 * @return the Constructor if found otherwise null (i.e. does not throw
	 *         NoSuchMethodException)
	 */
	public static < T > Constructor< T > findConstructor( Class< T > clazz, Class< ? >... paramTypes )
	{
		try
		{
			// try simple access first
			return clazz.getConstructor( paramTypes );
		}
		catch ( NoSuchMethodException e )
		{
			// try for compatible param types
			@SuppressWarnings( "unchecked" )
			final Constructor< T >[] methods = ( Constructor< T >[] ) (clazz.isInterface()
					? clazz.getDeclaredConstructors()
					: clazz.getConstructors());

			if ( (methods != null) && (methods.length > 0) )
			{
				for ( Constructor< T > m : methods )
				{
					if ( compatibleParamTypes( paramTypes, m.getParameterTypes() ) )
					{
						if ( logger.isDebugEnabled() )
						{
							logger.debug( format( "Class [%s] has compatible constructor: %s.",
									clazz.getSimpleName(),
									m ) );
						}

						return m;
					}
				}
			}

			if ( logger.isEnabledFor( Level.DEBUG ) )
			{
				StringBuilder b = new StringBuilder();

				if ( paramTypes != null )
				{
					for ( int i = 0; i < paramTypes.length; i++ )
					{
						if ( i > 0 )
						{
							b.append( ", " );
						}
						Class< ? > c = paramTypes[ i ];
						b.append( (c == Anything.class) ? "?" : c.getName() );
					}
				}

				logger.debug( format( "Class [%s] has no compatible constructor with args [%s].",
						clazz.getSimpleName(),
						b ) );
			}

			return null;
		}
		catch ( Throwable t )
		{
			if ( SUPPRESS_THROWABLE )
			{
				return null;
			}
			throw new RuntimeException( t );
		}
	}

	private static boolean compatibleParamTypes( Class< ? >[] paramTypes, Class< ? >[] candidateTypes )
	{
		// both empty - compatible
		if ( ((candidateTypes == null) || (candidateTypes.length == 0))
				&& ((paramTypes == null) || (paramTypes.length == 0)) )
		{
			return true;
		}

		// size difference - not compatible
		if ( !commensurateArrays( candidateTypes, paramTypes ) )
		{
			return false;
		}

		// both have same size - are the params assignable
		for ( int i = 0, n = candidateTypes.length; i < n; i++ )
		{
			// the arg was null
			// so we'll accept anything
			if ( (paramTypes[ i ] != Anything.class) && !candidateTypes[ i ].isAssignableFrom( paramTypes[ i ] ) )
			{
				// not compatible
				return false;
			}
		}

		return true;
	}

	private static boolean commensurateArrays( Object[] x, Object[] y )
	{
		// size difference - not compatible
		if ( (x == null) && (y != null) && (y.length > 0) )
		{
			return false;
		}

		if ( (x != null) && (x.length > 0) && (y == null) )
		{
			return false;
		}

		if ( x.length != y.length )
		{
			return false;
		}
		return true;
	}
}
