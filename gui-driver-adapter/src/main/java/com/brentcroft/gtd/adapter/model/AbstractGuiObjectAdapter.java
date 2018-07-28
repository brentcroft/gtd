package com.brentcroft.gtd.adapter.model;

/**
 * Created by Alaric on 17/07/2017.
 */
public abstract class AbstractGuiObjectAdapter< T > implements GuiObjectAdapter< T >
{
    private Class< T > clazz = null;
    private GuiObjectAdapter< ? super T > superAdapter = null;
    private GuiObjectConsultant< T > consultant;

    public AbstractGuiObjectAdapter( Class< T > clazz )
    {
        this.clazz = clazz;
    }

    public void setConsultant( GuiObjectConsultant< T > guiObjectConsultant )
    {
        this.consultant = guiObjectConsultant;
    }

    public GuiObjectConsultant< T > getConsultant()
    {
        return consultant;
    }

    @Override
    public void setSuperAdapter( GuiObjectAdapter< ? > adapter )
    {
        // TODO: would like to enforce < ? super T >
        // but leads to problems with adapter.setSuperAdapter( candidate ); in AdaptingGuiObjectManager
        this.superAdapter = ( GuiObjectAdapter< ? super T > ) adapter;
    }

    @Override
    public GuiObjectAdapter< ? super T > getSuperAdapter()
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
}
