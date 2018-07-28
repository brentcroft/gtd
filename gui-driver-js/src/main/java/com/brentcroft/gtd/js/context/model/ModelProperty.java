package com.brentcroft.gtd.js.context.model;

public class ModelProperty implements ModelMember
{
    private final String name;
    private final Object value;

    public ModelProperty(String name, Object value)
    {
        this.name = name;
        this.value = value;
    }


    @Override
    public boolean isFunction()
    {
        return false;
    }

    @Override
    public boolean isProperty()
    {
        return true;
    }

    @Override
    public boolean isObject()
    {
        return false;
    }



    public String name()
    {
        return name;
    }

    public Object getValue()
    {
        return value;
    }

    public String toString()
    {
        return name + ": " + value;
    }
}
