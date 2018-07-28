package com.brentcroft.gtd.js.context.model;



public interface ModelMember
{
    boolean isFunction();

    boolean isProperty();

    boolean isObject();

    String name();

}