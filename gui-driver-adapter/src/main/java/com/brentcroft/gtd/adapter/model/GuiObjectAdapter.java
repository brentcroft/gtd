package com.brentcroft.gtd.adapter.model;

import static java.lang.String.format;

import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 17/07/2017.
 *
 *
 *
 */
public interface GuiObjectAdapter< T >
{
	GuiObjectAdapter< ? super T > getSuperAdapter();

	// TODO: would like to enforce < ? super T >
	// but leads to problems with adapter.setSuperAdapter( candidate ); in
	// GuiCameraObjectManager
	void setSuperAdapter( GuiObjectAdapter< ? > adapter );

	GuiObjectConsultant< ? super T > getConsultant();

	void setConsultant( GuiObjectConsultant< ? > guiObjectConsultant );

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
	 * How many super-adaptersByClass does this adapter have.
	 * <p>
	 * Determines the ordering.
	 *
	 * @return the number of super-adaptersByClass
	 */
	default int getOrder()
	{
		return (getSuperAdapter() == null)
				? 0
				: getSuperAdapter().getOrder() + 1;
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
