package com.brentcroft.util.xpath.gob;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public enum Axis
{
    ANCESTOR( "ancestor" )
            {
                @Override
                public Selection getSelection( Gob gob, String nameTest )
                {
                    if ( nameTest == null )
                    {
                        return null;
                    }

                    return newSelection()
                            .withGobs(
                                    getAncestors( gob, false )
                                            .stream()
                                            .filter( a -> "*".equals( nameTest ) || nameTest.equals( a.getComponentTag() ) )
                                            .collect( Collectors.toList() )
                            );
                }
            },
    ANCESTOR_OR_SELF( "ancestor-or-self" )
            {
                @Override
                public Selection getSelection( Gob gob, String nameTest )
                {
                    if ( nameTest == null )
                    {
                        return null;
                    }

                    return newSelection()
                            .withGobs(
                                    getAncestors( gob, true )
                                            .stream()
                                            .filter( a -> "*".equals( nameTest ) || nameTest.equals( a.getComponentTag() ) )
                                            .collect( Collectors.toList() )
                            );
                }
            },
    ATTRIBUTE( "attribute" )
            {
                public Selection getSelection( Gob gob, String nameTest )
                {
                    // TODO: gob attributes back to list of text
                    List< Attribute > attributes = gob.getAttributes();


                    if ( "*".equals( nameTest ) )
                    {
                        return ( attributes == null || attributes.isEmpty() )
                                ? null
                                : newSelection()
                                        .withText( attributes
                                                .stream()
                                                .map( Attribute::getValue )
                                                .collect( Collectors.joining() ) );
                    }
//                    else if ( gob.hasAttribute( nameTest ) )
//                    {
//                        return newSelection()
//                                .withText( "" + gob.getAttribute( nameTest ) );
//                    }
                    else
                    {
                        Optional<Attribute> att = attributes
                                .stream()
                                .filter( attribute -> nameTest.equals( attribute.getName() ) )
                                .findFirst();

                        if (att.isPresent())
                        {
                            return newSelection()
                                .withText( "" + att.get().getValue() );

                        }
                    }

                    return null;
                }
            },
    CHILD( "child" )
            {
                @Override
                public Selection getSelection( Gob gob, String nameTest )
                {
                    if ( nameTest == null )
                    {
                        return null;
                    }

                    return gob.hasChildren()
                            ? newSelection()
                            .withGobs(
                                    gob
                                            .getChildren()
                                            .stream()
                                            .filter( c -> "*".equals( nameTest ) || nameTest.equals( c.getComponentTag() ) )
                                            .filter( Objects::nonNull )
                                            .collect( Collectors.toList() ) )
                            : null;
                }
            },
    DESCENDANT( "descendant" )
            {
                @Override
                public Selection getSelection( Gob gob, String nameTest )
                {
                    if ( nameTest == null )
                    {
                        return null;
                    }

                    return ( gob.hasChildren() )
                            ? newSelection()
                            .withGobs(
                                    getDescendants( gob, false )
                                            .stream()
                                            .filter( childGob -> "*".equals( nameTest ) || nameTest.equals( childGob.getComponentTag() ) )
                                            .filter( Objects::nonNull )
                                            .collect( Collectors.toList() )
                            )
                            : null;
                }
            },
    DESCENDANT_OR_SELF( "descendant-or-self" )
            {
                @Override
                public Selection getSelection( Gob gob, String nameTest )
                {
                    if ( nameTest == null )
                    {
                        return null;
                    }

                    return newSelection()
                            .withGobs(
                                    getDescendants( gob, true )
                                            .stream()
                                            .filter( childGob -> "*".equals( nameTest ) || nameTest.equals( childGob.getComponentTag() ) )
                                            .filter( Objects::nonNull )
                                            .collect( Collectors.toList() ) );
                }
            },
    FOLLOWING( "following" )
            {
                @Override
                public Selection getSelection( Gob gob, String nameTest )
                {
                    return null;
                }
            },
    FOLLOWING_SIBLING( "following-sibling" )
            {
                @Override
                public Selection getSelection( Gob gob, String nameTest )
                {
                    if ( nameTest == null )
                    {
                        return null;
                    }

                    List< Gob > ps = new ArrayList<>();

                    Optional
                            .ofNullable( gob.getParent() )
                            .ifPresent( parent -> {
                                if ( parent.hasChildren() )
                                {
                                    boolean adding = false;
                                    for ( Gob sibling : parent.getChildren() )
                                    {
                                        if ( sibling == gob )
                                        {
                                            adding = true;
                                            continue;
                                        }
                                        else if ( ! adding )
                                        {
                                            continue;
                                        }
                                        else if ( "*".equals( nameTest ) || nameTest.equals( sibling.getComponentTag() ) )
                                        {
                                            ps.add( sibling );
                                        }

                                    }
                                }
                            } );

                    return newSelection()
                            .withGobs( ps );

                }
            },
    NAMESPACE( "namespace" )
            {
                @Override
                public Selection getSelection( Gob gob, String nameTest )
                {
                    return null;
                }
            },
    PARENT( "parent" )
            {
                @Override
                public Selection getSelection( Gob gob, String nameTest )
                {
                    if ( nameTest == null )
                    {
                        return null;
                    }

                    Gob parent = gob.getParent();

                    if ( parent == null )
                    {
                        return null;
                    }
                    else if ( "*".equals( nameTest ) || nameTest.equals( parent.getComponentTag() ) )
                    {
                        return newSelection()
                                .withGob( parent );
                    }
                    return null;
                }
            },
    PRECEDING( "preceding" )
            {
                @Override
                public Selection getSelection( Gob gob, String nameTest )
                {
                    return null;
                }
            },
    PRECEDING_SIBLING( "preceding-sibling" )
            {
                @Override
                public Selection getSelection( Gob gob, String nameTest )
                {
                    if ( nameTest == null )
                    {
                        return null;
                    }

                    List< Gob > ps = new ArrayList<>();

                    Optional
                            .ofNullable( gob.getParent() )
                            .ifPresent( parent -> {
                                if ( parent.hasChildren() )
                                {
                                    for ( Gob sibling : parent.getChildren() )
                                    {
                                        if ( sibling == gob )
                                        {
                                            break;
                                        }
                                        else if ( "*".equals( nameTest ) || nameTest.equals( sibling.getComponentTag() ) )
                                        {
                                            ps.add( 0, sibling );
                                        }
                                    }
                                }
                            } );

                    return newSelection()
                            .withGobs( ps );

                }
            },
    SELF( "self" )
            {
                @Override
                public Selection getSelection( Gob gob, String nameTest )
                {
                    if ( nameTest == null )
                    {
                        return null;
                    }

                    if ( "*".equals( nameTest ) || nameTest.equals( gob.getComponentTag() ) )
                    {
                        return newSelection()
                                .withGob( gob );
                    }
                    return null;
                }
            };

    final String name;

    Axis( String name )
    {
        this.name = name;
    }

    public static Axis forName( String name )
    {
        if ( ( name == null || name.isEmpty() ) )
        {
            return CHILD;
        }

        // because this is what ( ForwardAxis | ReverseAxis ).getPath return
        if ( name.endsWith( "::" ) )
        {
            name = name.substring( 0, name.length() - 2 );
        }

        if ( "@".equals( name ) )
        {
            return ATTRIBUTE;
        }
        else if ( "//".equals( name ) )
        {
            return DESCENDANT_OR_SELF;
        }

        for ( Axis a : values() )
        {
            if ( a.name.equals( name ) )
            {
                return a;
            }
        }

        throw new IllegalArgumentException( "Unexpected Axis name: " + name );
    }

    public Selection newSelection()
    {
        return new Selection().withAxis( this );
    }


    public abstract Selection getSelection( Gob gob, String nameTest );


    private static List< ? extends Gob > getDescendants( Gob gob, boolean self )
    {
        //List< Gob > descendants = new ArrayList<>();
        Map< Integer, Gob > descendants = new LinkedHashMap<>();

        if ( self )
        {
            descendants.put( gob.hashCode(), gob );
        }

        if ( gob.hasChildren() )
        {
            buildDescendants( gob, descendants );
        }
        return new ArrayList<>( descendants.values() );
    }

    private static void buildDescendants( Gob gob, Map< Integer, Gob > descendants )
    {
        descendants.put( gob.hashCode(), gob );

        if ( gob.hasChildren() )
        {
            gob.getChildren()
                    .stream()
                    .filter( c -> ! descendants.containsKey( c.hashCode() ) )
                    .forEach( c -> {
                        buildDescendants( c, descendants );
                    } );
        }
    }


    private static List< ? extends Gob > getAncestors( Gob gob, boolean self )
    {
        Map< Integer, Gob > ancestors = new LinkedHashMap<>();

        if ( self )
        {
            ancestors.put( gob.hashCode(), gob );
        }

        Gob ancestor = gob.getParent();

        while ( ancestor != null )
        {
            ancestors.put( ancestor.hashCode(), ancestor );
            ancestor = ancestor.getParent();
        }

        return new ArrayList<>( ancestors.values() );
    }
}
