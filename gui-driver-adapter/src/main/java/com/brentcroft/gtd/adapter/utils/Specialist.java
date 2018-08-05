package com.brentcroft.gtd.adapter.utils;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface Specialist
{
	static Map< SpecialistMethod, Object > getSpecialistMethods( Object go, Collection< SpecialistMethod > requiredMethods )
	{
		Map< SpecialistMethod, Object > methods = new HashMap<>();

		requiredMethods.forEach( sm -> {

			Method m = ReflectionUtils.findMethod(
					go.getClass(),
					sm.getMethodName(),
					sm.getArgs()
			);

			if ( m != null )
			{
				methods.put( sm, m );
			}
		} );

		return methods;
	}

	static Map< SpecialistMethod, Object > getSpecialistFunctions( Object go, Collection< SpecialistMethod > requiredMethods )
	{
		return requiredMethods
				.stream()
				.collect( Collectors.toMap( Function.identity(), rm -> Optional.ofNullable( rm.getFunctionFrom( go ) ) ) )
				.entrySet()
				.stream()
				.filter( e -> e.getValue().isPresent() )
				.collect( Collectors.toMap( e -> e.getKey(), e -> e.getValue().get() ) );
	}

}