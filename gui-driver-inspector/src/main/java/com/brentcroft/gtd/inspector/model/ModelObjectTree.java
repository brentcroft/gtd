package com.brentcroft.gtd.inspector.model;

import com.brentcroft.gtd.js.context.model.ModelBuilder;
import com.brentcroft.gtd.js.context.model.ModelMember;
import com.brentcroft.gtd.js.context.model.ModelObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

@SuppressWarnings( "restriction" )
public class ModelObjectTree
{
    final static Image containerImage = new Image( ModelObjectTree.class.getResourceAsStream( "/container.png" ) );
    final static Image objectImage = new Image( ModelObjectTree.class.getResourceAsStream( "/object.png" ) );
    final static Image functionImage = new Image( ModelObjectTree.class.getResourceAsStream( "/function.png" ) );
    final static Image propertyImage = new Image( ModelObjectTree.class.getResourceAsStream( "/property.png" ) );


    public interface Include< T >
    {
        boolean include( T t );
    }


    public static TreeItem< ModelMember > buildTree( String key, ScriptObjectMirror mirror, Include< ModelMember > modelMemberIncluder )
    {
        ModelMember rootModelObject = new ModelObject( key, mirror );

        TreeItem< ModelMember > rootItem = new TreeItem< ModelMember >( rootModelObject );

        if ( mirror != null )
        {
            buildTree( rootItem, rootModelObject, modelMemberIncluder );
        }

        return rootItem;
    }


    private static int buildTree( TreeItem< ModelMember > parent, ModelMember m, Include< ModelMember > modelMemberIncluder )
    {
        if ( modelMemberIncluder != null && ! modelMemberIncluder.include( m ) )
        {
            return 0;
        }

        if ( m.isProperty() )
        {
            return 0;
        }
        if ( m.isFunction() )
        {
            return 0;
        }


        ModelObject model = ( ModelObject ) m;

        List< ModelMember > members = new ArrayList<>();

        for ( Entry< String, Object > entry : model.getMirror().entrySet() )
        {
            if ( entry.getKey().startsWith( "$" ) )
            {
                continue;
            }

            ModelMember mm = ModelBuilder
                    .newMember(
                            entry.getKey(),
                            entry.getValue(),
                            model );

            if ( modelMemberIncluder.include( mm ) )
            {
                members.add( mm );
            }
        }


        {
            // objects first
            // then functions
            // then properties
            Collections.sort( members, ( o1, o2 ) ->

                    o1.isProperty()
                            ? ( ! o2.isProperty()
                            ? 1
                            : o1.name().compareTo( o2.name() ) )
                            : o1.isObject()
                                    ? ( ! o2.isObject()
                                    ? - 1
                                    : o1.name().compareTo( o2.name() ) )
                                    : o2.isFunction()
                                            ? o1.name().compareTo( o2.name() )
                                            : ! o2.isProperty()
                                                    ? 1
                                                    : - 1
            );
        }

        int numChildObjects = 0;

        for ( ModelMember mm : members )
        {
            TreeItem< ModelMember > item = new TreeItem< ModelMember >( mm );


            parent
                    .getChildren()
                    .add( item );

            int numChildren = 0;

            if ( mm.isObject() )
            {
                numChildren = buildTree( item, mm, modelMemberIncluder );

                numChildObjects++;
            }

            item.setGraphic( new ImageView( mm.isProperty()
                    ? propertyImage
                    : mm.isFunction()
                            ? functionImage
                            : numChildren < 1
                                    ? objectImage
                                    : containerImage ) );

        }

        return numChildObjects;
    }
}
