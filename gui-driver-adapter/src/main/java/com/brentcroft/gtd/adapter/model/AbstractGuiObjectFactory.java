package com.brentcroft.gtd.adapter.model;

/**
 * Created by Alaric on 17/07/2017.
 */
public abstract class AbstractGuiObjectFactory< T > implements GuiObjectFactory< T >
{
	private Class< T > clazz = null;
	private GuiObjectFactory< ? super T > superAdapter = null;
	private GuiObjectConsultant< ? super T > consultant;

	public AbstractGuiObjectFactory( Class< T > clazz )
	{
		this.clazz = clazz;
	}

	@Override
	@SuppressWarnings( "unchecked" )
	public void setConsultant( GuiObjectConsultant< ? > guiObjectConsultant )
	{
		// TODO: would like to enforce < ? super T >
		// but leads to problems with adapter.setConsultant( candidate );
		this.consultant = ( GuiObjectConsultant< ? super T > ) guiObjectConsultant;
	}

	@Override
	public GuiObjectConsultant< ? super T > getConsultant()
	{
		return consultant;
	}

	@SuppressWarnings( "unchecked" )
	@Override
	public void setSuperAdapter( GuiObjectFactory< ? > adapter )
	{
		// TODO: would like to enforce < ? super T >
		// but leads to problems with adapter.setSuperAdapter( candidate );
		this.superAdapter = ( GuiObjectFactory< ? super T > ) adapter;
	}

	@Override
	public GuiObjectFactory< ? super T > getSuperFactory()
	{
		return this.superAdapter;
	}

	@Override
	public Class< T > handler()
	{
		return clazz;
	}

	@Override
	public boolean handles( Object t )
	{
		return clazz.isInstance( t );
	}
	

	@Override
	public String toString()
	{
		return getFactoryClass().getSimpleName();
	}
}
