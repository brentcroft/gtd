package com.brentcroft.gtd.driver.utils;

public interface HashCache< T >
{
	int getCacheSize();

	default boolean hasCachedObject( Integer hash )
	{
		return getCachedObject( hash ) != null;
	}

	T getCachedObject( Integer hash );

	void cacheObject( T cachee );

	boolean isEnabled();

	void setEnabled( boolean enabled );

	String getReport();

	void gc();
}
