package com.brentcroft.gtd.adapter.utils;

public interface SpecialistMethod
{
	String getMethodName();

	Class< ? >[] getArgs();
	
	Object getFunctionFrom( Object owner );
}