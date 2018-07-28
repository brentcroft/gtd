package com.brentcroft.gtd.js.context.model;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import static java.lang.String.format;

public class ModelObject implements ModelMember
{
    protected final String name;
    protected final ScriptObjectMirror mirror;

    public ModelObject( String name, ScriptObjectMirror mirror )
    {
        this.name = name;
        this.mirror = mirror;
    }

    public String name()
    {
        final String key = "$name";
        return "" + ( has( key ) ? get( key ) : name );
    }

    public String fullname()
    {
        final String key = "$fullname";
        return "" + ( has( key ) ? mirror.callMember( key ) : name() );
    }

    public ModelObject ancestor()
    {
        final String key = "$ancestor";
        return ( has( key ) ? new ModelObject(key, (ScriptObjectMirror) mirror.get( key ) ) : null );
    }

    public int getDepth()
    {
        int d = 0;
        ModelObject a = ancestor();
        while ( a != null)
        {
            d++;
            a = ancestor();
        }
        return d;
    }

    public String path()
    {
        final String key = "toString";
        return "" + ( has( key ) ? mirror.callMember( key ) : name );
    }

    public String xpath()
    {
        final String key = "$path";
        return "" + ( has( key ) ? mirror.callMember( key ) : name );
    }

    public String toString()
    {
        return name;
    }


    public Object get( Object key )
    {
        return mirror.get( key );
    }

    public boolean has( Object key )
    {
        return mirror.containsKey( key );
    }

    public void set( String key, Object value )
    {
        mirror.setMember( key, value );
    }

    public void remove( String key )
    {
        mirror.removeMember( key );
    }

    public boolean hasXPath( )
    {
        return has( "$xpath" );
    }

    @Override
    public boolean isProperty()
    {
        return false;
    }

    @Override
    public boolean isFunction()
    {
        return false;
    }


    @Override
    public boolean isObject()
    {
        return true;
    }

    public ScriptObjectMirror getMirror()
    {
        return mirror;
    }


}