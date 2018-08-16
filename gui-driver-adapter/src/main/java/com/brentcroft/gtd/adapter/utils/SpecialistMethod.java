package com.brentcroft.gtd.adapter.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.TriFunction;

public interface SpecialistMethod
{
	String getMethodName();

	Class< ? >[] getArgs();

	Object getFunctionFrom( Object owner );

	default int compareTo( SpecialistMethod other )
	{
		return (other == null)
				? 1
				: getMethodName().compareTo( other.getMethodName() );
	}

	default Object getFunction( Object owner, String methodName )
	{
		return Optional
				.ofNullable( ReflectionUtils.findMethod(
						owner.getClass(),
						methodName,
						getArgs()
				) )
				.filter( Objects::nonNull )
				.map( m -> {
					switch ( getArgs().length )
					{
						case 0:
							return ( Function< Object, Object > ) t -> CameraObjectManager.valueOrRuntimeException( t, m );

						case 1:
							return ( BiFunction< Object, Object, Object > ) ( t, a ) -> CameraObjectManager.valueOrRuntimeException( t, m, a );

						case 2:
							return ( TriFunction< Object, Object, Object, Object > ) ( t, a, b ) -> CameraObjectManager.valueOrRuntimeException( t, m, a, b );

						default:
							return null;
					}
				} )
				.orElse( null );

	}

	default Object getFunction( Object owner )
	{
		return getFunction( owner, getMethodName() );
	}

}