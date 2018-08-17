package com.brentcroft.gtd.adapter.utils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Specialist
{
	static Map< String, Object > extractFunctions( Object go, List< SpecialistMethod > methods )
	{
		Map< String, Object > functions = new LinkedHashMap<>();

		methods
				.stream()
				.forEachOrdered( m -> Optional
						.ofNullable( m.getFunction( go ) )
						.ifPresent( functionFrom -> {
							functions.remove( m.getMethodName() );
							functions.put( m.getMethodName(), functionFrom );
						} ) );

		return functions;
	}

}