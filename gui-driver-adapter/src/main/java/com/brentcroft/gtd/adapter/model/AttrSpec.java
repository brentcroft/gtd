package com.brentcroft.gtd.adapter.model;

public interface AttrSpec< T >
{
	String getName();

	String getAttribute( T go );
}
