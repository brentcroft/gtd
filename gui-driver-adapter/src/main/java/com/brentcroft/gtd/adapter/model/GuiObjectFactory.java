package com.brentcroft.gtd.adapter.model;

import static java.lang.String.format;

import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 17/07/2017.
 *
 *
 *
 */
public interface GuiObjectFactory< T >
{
	GuiObjectFactory< ? super T > getSuperFactory();

	// TODO: would like to enforce < ? super T >
	// but leads to problems with adapter.setSuperAdapter( candidate ); in
	// GuiCameraObjectManager
	void setSuperAdapter( GuiObjectFactory< ? > adapter );

	GuiObjectConsultant< ? super T > getConsultant();

	void setConsultant( GuiObjectConsultant< ? > guiObjectConsultant );
	
	/**
	 * An adapter may provide a specialised version (typically, but not necessarily,  of itself).
	 * 
	 * @param s
	 * @return
	 */
	default GuiObjectFactory< T > getSpecialist( T t, Gob parent )
	{
		return null;
	}
	
	
	/**
	 * The type of objects for which this factory provides adapters.
	 * 
	 * @return
	 */
	Class< T > handler();


	/**
	 * Is the object a T (handled by this adapter)?
	 *
	 * @param t
	 *            the object
	 * @return true if t is handled by this adapter.
	 */
	boolean handles( Object t );

	/**
	 * Provides a <code>GuiObject< ? super T ></code> adapter of <code>t</code>.
	 *
	 * @param t
	 * @return a <code>GuiObject< ? super T ></code> adapter of <code>t</code>.
	 */
	GuiObject< ? super T > adapt( T t, Gob parent );

	
	/**
	 * The type of adapter this factory provides
	 * 
	 * @return
	 */
	< H extends GuiObject< T > > Class< H > getFactoryClass();

	/**
	 * How many super-adaptersByClass does this adapter have.
	 * <p>
	 * Determines the ordering.
	 *
	 * @return the number of super-adaptersByClass
	 */
	default int getOrder()
	{
		return (getSuperFactory() == null)
				? 0
				: getSuperFactory().getOrder() + 1;
	}

	/**
	 * Generates a key based on the order and the GuiObject class name.
	 *
	 * @return
	 */
	default String getOrderKey()
	{
		return format(
				"%05d:%s",
				Integer.MAX_VALUE - getOrder(),
				handler().getName() );
	}

}
