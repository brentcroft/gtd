package com.brentcroft.gtd.adapter.model;

public interface AttrSpec< T >
{
	String getName();

	default String getAttribute( T go )
	{
		return null;
	}

	default String getSpecialAttribute( T go )
	{
		return null;
	}



	static Object trueOrNull( Boolean b )
	{
		return b != null && b ? b : null;
	}

	static String stringOrNull( Object b )
	{
		return b != null ? b.toString() : null;
	}
}
