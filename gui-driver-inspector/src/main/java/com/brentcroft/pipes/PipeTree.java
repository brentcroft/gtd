package com.brentcroft.pipes;


import com.brentcroft.util.Pipes;
import javafx.scene.control.TreeItem;

/**
 * Created by Alaric on 01/12/2016.
 */
public class PipeTree
{

    public static TreeItem< Pipes.Pipe > buildTree( String key, Pipes.Pipe mirror )
    {
//        ModelMember rootModelObject = new ModelObject( key, mirror );
//
//        TreeItem< ModelMember > rootItem = new TreeItem< ModelMember >( rootModelObject );
//
//        if ( mirror != null )
//        {
//            buildTree( rootItem, rootModelObject );
//        }
//
//        return rootItem;
        return null;
    }


    private static void buildTree( TreeItem< Pipes.Pipe > parent, Pipes.Pipe m )
    {

        //ModelObject expandModelToJson = (ModelObject) m;
//
//        for ( Entry< String, Object > entry : expandModelToJson.getMirror().entrySet() )
//        {
//            if ( entry.getKey().startsWith( "$" ) )
//            {
//                continue;
//            }
//            if ( !( entry.getValue() instanceof ScriptObjectMirror ) )
//            {
//                ModelMember mf = new ModelProperty( entry.getKey(), entry.getValue() );
//
//                TreeItem< ModelMember > item = new TreeItem< ModelMember >( mf );
//
//                parent
//                        .getChildren()
//                        .add( item );
//
//                continue;
//            }
//
//            ScriptObjectMirror mirror = (ScriptObjectMirror) entry.getValue();
//
//
//            if ( ( mirror.isFunction() || mirror.isStrictFunction() ) )
//            {
//
//                ModelFunction mf = new ModelFunction(
//                        entry.getKey(),
//                        mirror,
//                        expandModelToJson );
//
//                parent
//                        .getChildren()
//                        .add( new TreeItem< ModelMember >( mf ) );
//            }
//            else
//            {
//                ModelObject childModel = new ModelObject(
//                        entry.getKey(),
//                        mirror );
//
//                TreeItem< ModelMember > item = new TreeItem< ModelMember >( childModel );
//
//                parent
//                        .getChildren()
//                        .add( item );
//
//                buildTree( item, childModel );
//            }
//        }
    }

}
