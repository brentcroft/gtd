package com.brentcroft.util.xpath.gob;

import com.brentcroft.util.xpath.Node;
import com.brentcroft.util.xpath.SimpleNode;
import com.brentcroft.util.xpath.XParserTreeConstants;
import com.brentcroft.util.xpath.XParserVisitor;
import com.brentcroft.util.xpath.ast.AbbrevForwardStep;
import com.brentcroft.util.xpath.ast.AbbrevReverseStep;
import com.brentcroft.util.xpath.ast.AdditiveExpr;
import com.brentcroft.util.xpath.ast.AndExpr;
import com.brentcroft.util.xpath.ast.AnyKindTest;
import com.brentcroft.util.xpath.ast.AxisStep;
import com.brentcroft.util.xpath.ast.CommentTest;
import com.brentcroft.util.xpath.ast.ContextItemExpr;
import com.brentcroft.util.xpath.ast.DecimalLiteral;
import com.brentcroft.util.xpath.ast.EqualityExpr;
import com.brentcroft.util.xpath.ast.FilterExpr;
import com.brentcroft.util.xpath.ast.ForwardAxis;
import com.brentcroft.util.xpath.ast.FunctionCall;
import com.brentcroft.util.xpath.ast.FunctionQName;
import com.brentcroft.util.xpath.ast.IntegerLiteral;
import com.brentcroft.util.xpath.ast.Minus;
import com.brentcroft.util.xpath.ast.MultiplicativeExpr;
import com.brentcroft.util.xpath.ast.NCName;
import com.brentcroft.util.xpath.ast.NCNameColonStar;
import com.brentcroft.util.xpath.ast.NodeTest;
import com.brentcroft.util.xpath.ast.OrExpr;
import com.brentcroft.util.xpath.ast.PITest;
import com.brentcroft.util.xpath.ast.ParenthesizedExpr;
import com.brentcroft.util.xpath.ast.PathExpr;
import com.brentcroft.util.xpath.ast.Predicate;
import com.brentcroft.util.xpath.ast.PredicateList;
import com.brentcroft.util.xpath.ast.QName;
import com.brentcroft.util.xpath.ast.RelationalExpr;
import com.brentcroft.util.xpath.ast.ReverseAxis;
import com.brentcroft.util.xpath.ast.START;
import com.brentcroft.util.xpath.ast.Slash;
import com.brentcroft.util.xpath.ast.SlashSlash;
import com.brentcroft.util.xpath.ast.StringLiteral;
import com.brentcroft.util.xpath.ast.TextTest;
import com.brentcroft.util.xpath.ast.UnaryExpr;
import com.brentcroft.util.xpath.ast.UnionExpr1;
import com.brentcroft.util.xpath.ast.VarName;
import com.brentcroft.util.xpath.ast.Wildcard;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class GobVisitor implements XParserVisitor
{

    public Selection visit( SimpleNode node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( START node, Gob gob, Selection axis )
    {
        SimpleNode child = node.getChild( 0 );

        switch ( child.getId() )
        {
            case XParserTreeConstants.JJTEQUALITYEXPR:
                return child.accept( this, gob, axis );

            default:
                throw new IllegalStateException( format( "Unexpected child [%s] of %s: %s", 0, node, child ) );
        }

    }

    @Override
    public Selection visit( OrExpr node, Gob gob, Selection axis )
    {
        for ( Node childNode : node.getChildren() )
        {
            switch ( childNode.getId() )
            {
                case XParserTreeConstants.JJTEQUALITYEXPR:
                case XParserTreeConstants.JJTOREXPR:
                case XParserTreeConstants.JJTANDEXPR:
                    break;

                default:
                    throw new IllegalStateException( format( "Unexpected child of %s: %s", node, childNode ) );
            }

            Selection nextResult = ( ( SimpleNode ) childNode ).accept( this, gob, axis );

            // first that is not empty satisfies the or
            if ( nextResult != null && nextResult.toBoolean() )
            {
                return nextResult;
            }
        }

        return null;
    }

    @Override
    public Selection visit( AndExpr node, Gob gob, Selection axis )
    {
        Selection result = null;

        for ( Node childNode : node.getChildren() )
        {
            switch ( childNode.getId() )
            {
                case XParserTreeConstants.JJTEQUALITYEXPR:
                    break;

                default:
                    throw new IllegalStateException( format( "Unexpected child of %s: %s", node, childNode ) );
            }

            Selection nextResult = ( ( SimpleNode ) childNode ).accept( this, gob, axis );

            // can't  satisfy AndExpr
            if ( nextResult == null || nextResult.isEmpty() || ! nextResult.toBoolean() )
            {
                return null;
            }

            result = nextResult;
        }

        return result;
    }


    /**
     * Returns a boolean result.
     *
     * @param node a EqualityExpr
     * @param gob a Gob
     * @param axis an Axis
     * @return a boolean result.
     */
    @Override
    public Selection visit( EqualityExpr node, Gob gob, Selection axis )
    {
        Selection result = null;

        for ( Node childNode : node.getChildren() )
        {
            switch ( childNode.getId() )
            {
                case XParserTreeConstants.JJTEQUALITYEXPR:
                case XParserTreeConstants.JJTUNIONEXPR1:
                case XParserTreeConstants.JJTADDITIVEEXPR:
                case XParserTreeConstants.JJTMULTIPLICATIVEEXPR:
                case XParserTreeConstants.JJTRELATIONALEXPR:
                    break;

                default:
                    throw new IllegalStateException( format( "Unexpected child of %s: %s", node, childNode ) );
            }


            // any component failing to exist
            SimpleNode child = ( SimpleNode ) childNode;

            Selection nextResult = child.accept( this, gob, axis );

            if ( nextResult == null )
            {
                return null;
            }


            if ( result == null )
            {
                result = nextResult;
            }
            else if ( ! opEqualityResult( node.getValue(), result, nextResult ) )
            {
                return null;
            }
            else
            {
                result = Selection.TRUE;
            }
        }

        return result;
    }

    @Override
    public Selection visit( RelationalExpr node, Gob gob, Selection axis )
    {
        Selection result = null;

        for ( Node childNode : node.getChildren() )
        {
            switch ( childNode.getId() )
            {
                case XParserTreeConstants.JJTUNIONEXPR1:
                case XParserTreeConstants.JJTADDITIVEEXPR:
                case XParserTreeConstants.JJTMULTIPLICATIVEEXPR:
                    break;

                default:
                    throw new IllegalStateException( format( "Unexpected child of %s: %s", node, childNode ) );
            }


            // any component failing to exist
            SimpleNode child = ( SimpleNode ) childNode;

            Selection nextResult = child.accept( this, gob, axis );

            if ( nextResult == null )
            {
                return null;
            }

            if ( result == null )
            {
                result = nextResult;
            }
            else if ( ! opRelationalResult( node.getValue(), result, nextResult ) )
            {
                return null;
            }
            else
            {
                result = Selection.TRUE;
            }
        }

        return result;
    }


    @Override
    public Selection visit( AdditiveExpr node, Gob gob, Selection axis )
    {
        Selection result = null;

        for ( Node childNode : node.getChildren() )
        {
            switch ( childNode.getId() )
            {
                case XParserTreeConstants.JJTUNIONEXPR1:
                case XParserTreeConstants.JJTADDITIVEEXPR:
                case XParserTreeConstants.JJTMULTIPLICATIVEEXPR:
                    break;

                default:
                    throw new IllegalStateException( format( "Unexpected child in %s: %s", node, childNode ) );
            }


            // any component failing to exist
            SimpleNode child = ( SimpleNode ) childNode;

            Selection nextResult = child.accept( this, gob, axis );

            if ( nextResult == null )
            {
                return null;
            }

            if ( result == null )
            {
                result = nextResult;
            }
            else
            {
                result.withNumber( opAdditativeResult( node.getValue(), result, nextResult ) );
            }
        }

        return result;
    }


    @Override
    public Selection visit( MultiplicativeExpr node, Gob gob, Selection axis )
    {
        Selection result = null;

        for ( Node childNode : node.getChildren() )
        {
            switch ( childNode.getId() )
            {
                case XParserTreeConstants.JJTUNIONEXPR1:
                case XParserTreeConstants.JJTADDITIVEEXPR:
                case XParserTreeConstants.JJTMULTIPLICATIVEEXPR:
                    break;

                default:
                    throw new IllegalStateException( format( "Unexpected child of %s: %s", node, childNode ) );
            }

            // any component failing to exist
            SimpleNode child = ( SimpleNode ) childNode;

            Selection nextResult = child.accept( this, gob, axis );

            if ( nextResult == null )
            {
                return null;
            }

            if ( result == null )
            {
                result = nextResult;
            }
            else
            {
                result.withNumber( opMultiplicativeResult( node.getValue(), result, nextResult ) );
            }
        }

        return result;
    }


    @Override
    public Selection visit( UnionExpr1 node, Gob gob, Selection axis )
    {
        Selection result = null;

        for ( Node childNode : node.getChildren() )
        {
            switch ( childNode.getId() )
            {
                case XParserTreeConstants.JJTPATHEXPR:
                case XParserTreeConstants.JJTFILTEREXPR:
                    break;

                default:
                    throw new IllegalStateException( format( "Unexpected child of %s: %s", node, childNode ) );
            }


            SimpleNode child = ( SimpleNode ) childNode;

            Selection nextResult = child.accept( this, gob, axis );

            // union semantics
            if ( result == null )
            {
                result = nextResult;
            }
            else if ( nextResult != null )
            {
                result.withResult( nextResult );
            }
        }

        return result;
    }

    /**
     * PathExpr	::=	( ( <Slash> ( RelativePathExpr )? ) | ( <SlashSlash> RelativePathExpr ) | RelativePathExpr )
     *
     * @param pathExpr
     * @param gob
     * @return
     */
    @Override
    public Selection visit( PathExpr pathExpr, Gob gob, Selection axis )
    {
        int index = 0;

        final Selection[] result = { null };

        for ( Node childNode : pathExpr.getChildren() )
        {
            SimpleNode child = ( SimpleNode ) childNode;

            switch ( child.getId() )
            {
                case XParserTreeConstants.JJTSLASHSLASH:
                    if ( result[ 0 ] == null )
                    {
                        result[ 0 ] = Axis.DESCENDANT_OR_SELF.getSelection( gob, "*" );
                    }
                    else
                    {
                        if ( ! result[ 0 ].isGobs() )
                        {
                            throw new IllegalStateException( "Selection is not Gobs in PathExpr/JJTSLASHSLASH: " + result[ 0 ] );
                        }

                        Selection accumulatedResult = new Selection();

                        // old gobs are replaced with a subset
                        result[ 0 ].getGobs()
                                .stream()
                                .map( childGob -> Axis.DESCENDANT_OR_SELF.getSelection( childGob, "*" ) )
                                .filter( Objects::nonNull )
                                .forEach( accumulatedResult::withResult );

                        // step
                        result[ 0 ] = accumulatedResult;
                    }
                    break;

                case XParserTreeConstants.JJTSLASH:
                    // equivalent to: /child::node()/
                    break;

                case XParserTreeConstants.JJTABBREVREVERSESTEP:
                    if ( result[ 0 ] == null )
                    {
                        gob = gob.getParent();

                        if ( gob == null )
                        {
                            throw new IllegalStateException( "Cannot reverse step: parent is null." );
                        }
                    }
                    else
                    {
                        if ( ! result[ 0 ].isGobs() )
                        {
                            throw new IllegalStateException( "Selection is not Gobs in PathExpr/AbbrevReverseStep: " + result[ 0 ] );
                        }

                        // TODO: they may not all have the same same parent
                        result[ 0 ] = new Selection().withGob( result[ 0 ].getGobs().get( 0 ).getParent() );
                    }
                    break;

                case XParserTreeConstants.JJTCONTEXTITEMEXPR:
                    if ( result[ 0 ] == null )
                    {
                        result[ 0 ] = new Selection().withGob( gob );
                    }
                    break;


                case XParserTreeConstants.JJTAXISSTEP:

                    if ( result[ 0 ] == null )
                    {
                        result[ 0 ] = child.accept( this, gob, axis );

                        if ( result[ 0 ] == null || result[ 0 ].isEmpty() )
                        {
                            // i.e. nothing
                            return result[ 0 ];
                        }
                    }
                    else
                    {
                        if ( result[ 0 ].isEmpty() )
                        {
                            // i.e. nothing
                            return result[ 0 ];
                        }
                        else if ( ! result[ 0 ].isGobs() )
                        {
                            throw new IllegalStateException( "Selection is not Gobs in PathExpr/AxisStep: " + result[ 0 ] );
                        }
                        else
                        {
                            Selection accumulatedResult = new Selection();

                            // old gobs are replaced with a subset
                            result[ 0 ].getGobs()
                                    .stream()
                                    .map( childGob -> child.accept( this, childGob, result[ 0 ] ) )
                                    .filter( Objects::nonNull )
                                    .forEach( accumulatedResult::withResult );

                            // step
                            result[ 0 ] = accumulatedResult;
                        }
                    }

                    break;

                default:
                    throw new IllegalStateException( format( "Unexpected child [%s] of %s: %s", index, pathExpr, child ) );
            }
        }

        return result[ 0 ];
    }


    @Override
    public Selection visit( AxisStep axisStep, Gob gob, Selection axis )
    {
        // always new axis
        Selection newAxis = Gob.RESULT_CHILD.withGob( gob );


        Selection candidates = null;

        for ( Node childNode : axisStep.getChildren() )
        {
            SimpleNode child = ( SimpleNode ) childNode;

            switch ( child.getId() )
            {
                case XParserTreeConstants.JJTREVERSEAXIS:
                case XParserTreeConstants.JJTFORWARDAXIS:

                    Selection axisSpecifier = child.accept( this, gob, newAxis );

                    newAxis = new Selection().withAxis( Axis.forName( axisSpecifier.toText() ) );

                    break;

                case XParserTreeConstants.JJTNODETEST:

                    candidates = child.accept( this, gob, newAxis );

                    if ( candidates == null || candidates.isEmpty() || axisStep.size() < 3 )
                    {
                        return candidates;
                    }
                    break;

                case XParserTreeConstants.JJTABBREVFORWARDSTEP:

                    // step will return none or more members of gos
                    AbbrevForwardStep abbrevForwardStep = ( AbbrevForwardStep ) child;

                    candidates = abbrevForwardStep.accept( this, gob, newAxis );

                    if ( candidates == null || candidates.isEmpty() )
                    {
                        return candidates;
                    }
                    break;


                case XParserTreeConstants.JJTPREDICATELIST:

                    if ( candidates == null || ! candidates.isGobs() )
                    {
                        throw new IllegalStateException( "Cannot apply predicates to non-gobs Selection!" );
                    }

                    Selection candidates2 = candidates;

                    candidates = new Selection()
                            .withGobs(
                                    candidates
                                            .getGobs()
                                            .stream()
                                            .filter( g -> {
                                                Selection predicates = child.accept( this, g, candidates2 );

                                                return predicates != null && predicates.toBoolean();
                                            } )
                                            .collect( Collectors.toList() ) );

                    return candidates;

                default:
                    throw new IllegalStateException( format( "Unexpected child [%s] of %s: %s", 0, axisStep, child ) );

            }
        }
        return candidates;
    }

    @Override
    public Selection visit( ReverseAxis node, Gob gob, Selection axis )
    {
        return new Selection().withText( node.getValue() );
    }


    @Override
    public Selection visit( AbbrevForwardStep abbrevForwardStep, Gob gob, Selection axis )
    {
        SimpleNode child = abbrevForwardStep.getChild( 0 );

        switch ( child.getId() )
        {
            case XParserTreeConstants.JJTNODETEST:
                return child.accept(
                        this,
                        gob,
                        abbrevForwardStep.isAttribute()
                                ? Gob.RESULT_ATTRIBUTE
                                : Gob.RESULT_CHILD );

            default:
                throw new IllegalStateException( format( "Unexpected child [%s] of %s: %s", 0, abbrevForwardStep, child ) );
        }
    }


    @Override
    public Selection visit( NodeTest nodeTest, Gob gob, Selection axis )
    {
        SimpleNode child = nodeTest.getChild( 0 );

        switch ( child.getId() )
        {
            case XParserTreeConstants.JJTNAMETEST:
                return child.accept( this, gob, axis );

            default:
                throw new IllegalStateException( format( "Unexpected child [%s] of %s: %s", 0, nodeTest, child ) );
        }
    }

//    @Override
//    public Selection visit( NameTest nameTest, Gob gob, Selection axis )
//    {
//        SimpleNode child = nameTest.getChild( 0 );
//
//        switch ( child.getId() )
//        {
//            case XParserTreeConstants.JJTWILDCARD:
//
//                Selection candidates = null;
//
//                switch ( axis.getAxis() )
//                {
//                    case ATTRIBUTE:
//                    {
//                        // TODO: gob attributes back to list of text
//                        List< Attribute > attributes = gob.getAttributes();
//                        if ( attributes != null && ! attributes.isEmpty() )
//                        {
//                            return new Selection().withText( attributes
//                                    .stream()
//                                    .map( Attribute::getValue )
//                                    .collect( Collectors.joining() ) );
//                        }
//                    }
//                    break;
//
//                    case CHILD:
//                        if ( gob.hasChildren() )
//                        {
//                            return new Selection().withGobs( gob.getChildren() );
//                        }
//                        break;
//
//
//                    case SELF:
//                        return new Selection().withGob( gob );
//
//
//                    case PRECEDING_SIBLING:
//                    {
//                        List< Gob > ps = new ArrayList<>();
//
//                        Optional
//                                .ofNullable( gob.getParent() )
//                                .ifPresent( parent -> {
//                                    if ( parent.hasChildren() )
//                                    {
//                                        for ( Gob sibling : parent.getChildren() )
//                                        {
//                                            if ( sibling == gob )
//                                            {
//                                                break;
//                                            }
//
//                                            ps.add( 0, sibling );
//                                        }
//                                    }
//                                } );
//
//                        return new Selection().withGobs( ps );
//                    }
//
//
//                    case FOLLOWING_SIBLING:
//                    {
//                        List< Gob > ps = new ArrayList<>();
//
//                        Optional
//                                .ofNullable( gob.getParent() )
//                                .ifPresent( parent -> {
//                                    if ( parent.hasChildren() )
//                                    {
//                                        boolean adding = false;
//                                        for ( Gob sibling : parent.getChildren() )
//                                        {
//                                            if ( sibling == gob )
//                                            {
//                                                adding = true;
//                                                continue;
//                                            }
//                                            else if ( ! adding )
//                                            {
//                                                continue;
//                                            }
//
//                                            ps.add( sibling );
//                                        }
//                                    }
//                                } );
//
//                        return new Selection().withGobs( ps );
//                    }
//                }
//
//                return candidates;
//
//            case XParserTreeConstants.JJTQNAME:
//
//                final String name = child.getValue();
//
//                switch ( axis.getAxis() )
//                {
//                    case CHILD:
//                        if ( gob.hasChildren() )
//                        {
//                            Selection accumulatedResult = new Selection();
//
//                            gob
//                                    .getChildren()
//                                    .stream()
//                                    .filter( childGob -> name.equals( childGob.getComponentTag() ) )
//                                    .filter( Objects::nonNull )
//                                    .forEach( r -> accumulatedResult.withGob( r ) );
//
//                            return accumulatedResult.isEmpty()
//                                    ? null
//                                    : accumulatedResult;
//                        }
//                        break;
//
//                    case SELF:
//                        if ( name.equals( gob.getComponentTag() ) )
//                        {
//                            return new Selection()
//                                    .withAxis( Axis.SELF )
//                                    .withGob( gob );
//                        }
//                        break;
//
//
//                    case ATTRIBUTE:
//                        if ( gob.hasAttribute( name ) )
//                        {
//                            return new Selection().withText( "" + gob.getAttribute( name ) );
//                        }
//                        break;
//
//
//                    case PRECEDING_SIBLING:
//                    {
//                        List< Gob > ps = new ArrayList<>();
//
//                        Optional
//                                .ofNullable( gob.getParent() )
//                                .ifPresent( parent -> {
//                                    if ( parent.hasChildren() )
//                                    {
//                                        for ( Gob sibling : parent.getChildren() )
//                                        {
//                                            if ( sibling == gob )
//                                            {
//                                                break;
//                                            }
//                                            else if ( name.equals( sibling.getComponentTag() ) )
//                                            {
//                                                ps.add( 0, sibling );
//                                            }
//                                        }
//                                    }
//                                } );
//
//                        return new Selection().withGobs( ps );
//                    }
//
//
//                    case FOLLOWING_SIBLING:
//                    {
//                        List< Gob > ps = new ArrayList<>();
//
//                        Optional
//                                .ofNullable( gob.getParent() )
//                                .ifPresent( parent -> {
//                                    if ( parent.hasChildren() )
//                                    {
//                                        boolean adding = false;
//                                        for ( Gob sibling : parent.getChildren() )
//                                        {
//                                            if ( sibling == gob )
//                                            {
//                                                adding = true;
//                                                continue;
//                                            }
//                                            else if ( ! adding )
//                                            {
//                                                continue;
//                                            }
//                                            else if ( name.equals( sibling.getComponentTag() ) )
//                                            {
//                                                ps.add( sibling );
//                                            }
//                                        }
//                                    }
//                                } );
//
//                        return new Selection().withGobs( ps );
//                    }
//
//
//                    default:
//                        throw new IllegalStateException( format( "Unexpected axis [%s] of %s: %s", axis, nameTest, child ) );
//
//                }
//                break;
//
//
//            default:
//                throw new IllegalStateException( format( "Unexpected child [%s] of %s: %s", 0, nameTest, child ) );
//        }
//
//        return null;
//    }


    @Override
    public Selection visit( FilterExpr filterExpr, Gob gob, Selection axis )
    {
        SimpleNode child = filterExpr.getChild( 0 );

        switch ( child.getId() )
        {
            case XParserTreeConstants.JJTSTRINGLITERAL:

                final String text = child.getPath();

                // trim off the quotes
                return new Selection().withText( text.substring( 1, text.length() - 1 ) );

            case XParserTreeConstants.JJTINTEGERLITERAL:
                return new Selection().withNumber( Integer.valueOf( child.getPath() ) );


            case XParserTreeConstants.JJTFUNCTIONCALL:
            case XParserTreeConstants.JJTPARENTHESIZEDEXPR:
                return child.accept( this, gob, axis );


            default:
                throw new IllegalStateException( format( "Unexpected child [%s] of %s: %s", 0, filterExpr, child ) );
        }
    }


    @Override
    public Selection visit( PredicateList node, Gob candidate, Selection axis )
    {
        if ( node.size() == 0 )
        {
            return null;
        }

        Selection result = new Selection();

        for ( Node childNode : node.getChildren() )
        {
            Predicate predicate = ( Predicate ) childNode;

            if ( ! result.isEmpty() && ! result.isGobs() )
            {
                throw new IllegalStateException( "Cannot apply subsequent predicate to non-Gobs." );
            }

            if ( result.isGobs() )
            {
                List< Gob > gobs = result.getGobs();

                result.clear();

                gobs
                        .stream()
                        .map( g -> predicate.accept( this, candidate, axis ) )
                        .filter( Objects::nonNull )
                        .forEach( result::withResult );
            }
            else
            {
                Selection selected = predicate.accept( this, candidate, axis );

                if ( selected == null || selected.isEmpty() )
                {
                    return null;
                }
                else if ( selected.isBoolean() && ! selected.toBoolean() )
                {
                    return null;

                }
                else if ( selected.isNumber() )
                {
                    List< Gob > gobs = result.isEmpty() ?
                            axis.getGobs() :
                            result.getGobs();

                    //
                    if ( gobs.size() < selected.toNumber().intValue() )
                    {
                        return null;
                    }

                    // this is not the gob you are looking for
                    if ( gobs.get( selected.toNumber().intValue() - 1 ) != candidate )
                    {
                        return null;
                    }
                }

                result.withGob( candidate );
            }
        }

        return result;
    }


    @Override
    public Selection visit( Predicate predicate, Gob gob, Selection axis )
    {
        SimpleNode child = predicate.getChild( 0 );

        switch ( child.getId() )
        {
            case XParserTreeConstants.JJTEQUALITYEXPR:
            case XParserTreeConstants.JJTOREXPR:
            case XParserTreeConstants.JJTANDEXPR:
                return child.accept( this, gob, axis );

            default:
                throw new IllegalStateException( format( "Unexpected child [%s] of %s: %s", 0, predicate, child ) );
        }
    }


    @Override
    public Selection visit( ParenthesizedExpr parenthesizedExpr, Gob gob, Selection axis )
    {
        SimpleNode child = parenthesizedExpr.getChild( 0 );

        switch ( child.getId() )
        {
            case XParserTreeConstants.JJTANDEXPR:
            case XParserTreeConstants.JJTOREXPR:
            case XParserTreeConstants.JJTEQUALITYEXPR:
                return child.accept( this, gob, axis );

            default:
                throw new IllegalStateException( format( "Unexpected child [%s] of %s: %s", 0, parenthesizedExpr, child ) );
        }
    }

    @Override
    public Selection visit( FunctionCall functionCall, Gob gob, Selection axis )
    {
        String fName = null;

        List< Selection > results = null;

        for ( Node childNode : functionCall.getChildren() )
        {
            SimpleNode child = ( SimpleNode ) childNode;

            switch ( child.getId() )
            {
                case XParserTreeConstants.JJTFUNCTIONQNAME:
                    fName = child.getValue();
                    break;

                case XParserTreeConstants.JJTEQUALITYEXPR:

                    Selection result = child.accept( this, gob, axis );

                    if ( results == null )
                    {
                        results = new ArrayList<>();
                        results.add( result );
                    }
                    else
                    {
                        results.add( child.accept( this, gob, axis ) );
                    }
                    break;

                default:
                    throw new IllegalStateException( format( "Unexpected child [%s] of %s: %s", 0, functionCall, child ) );
            }
        }

        if ( fName == null || fName.isEmpty() )
        {
            throw new IllegalStateException( format( "Require name for function [%s] of %s", fName, functionCall ) );
        }


        switch ( fName )
        {

            case "name":
                if ( results == null || results.isEmpty() )
                {
                    throw new IllegalStateException( format( "Require one argument for function [%s] of %s", fName, functionCall ) );
                }

                if ( results.get( 0 ) == null || results.get( 0 ).isEmpty() )
                {
                    throw new IllegalStateException( format( "Require one non-null non-empty argument for function [%s] of %s", fName, functionCall ) );
                }

                return new Selection()
                        .withText(
                                results
                                        .get( 0 )
                                        .getGobs()
                                        .stream()
                                        .map( Gob::getComponentTag )
                                        .collect( Collectors.joining() ) );

            case "position":

                Integer index = axis.indexForGob( gob );

                if ( index == null )
                {
                    return null;
                }

                return new Selection().withNumber( index );


            case "last":
                return new Selection().withNumber( axis.getGobs().size() );


            case "contains":

                if ( results == null || results.size() < 2 )
                {
                    throw new IllegalStateException( format( "Require two arguments for function [%s] of %s", fName, functionCall ) );
                }

                if ( results.get( 0 ) == null || results.get( 1 ) == null )
                {
                    throw new IllegalStateException( format( "Require two non-null arguments for function [%s] of %s", fName, functionCall ) );
                }

                String container = results.get( 0 ).toText();

                String candidate = results.get( 1 ).toText();

                return new Selection().withBoolean( container.contains( candidate ) );

            default:
                throw new IllegalStateException( format( "Unexpected function name [%s] of %s", fName, functionCall ) );
        }
    }


    private static boolean opEqualityResult( String op, Selection firstResult, Selection secondResult )
    {
        Boolean equal = null;

        if ( firstResult.isText() )
        {
            equal = firstResult.getText().equals( secondResult.toText() );
        }
        else if ( firstResult.isBoolean() )
        {
            equal = firstResult.getBoolean().equals( secondResult.toBoolean() );
        }
        else if ( firstResult.isNumber() )
        {
            equal = ( firstResult.getNumber() instanceof Integer )
                    ? ( firstResult.getNumber().intValue() == secondResult.toNumber().intValue() )
                    : ( firstResult.getNumber().doubleValue() == secondResult.toNumber().doubleValue() );
        }
        else
        {
            equal = false;
            //throw new IllegalStateException( format( "First result is not: text, boolean or number; first=[%s], second=[%s]", firstResult, secondResult ) );
        }

        switch ( op )
        {
            case "=":
                return equal;

            case "!=":
                return ! equal;

            default:
                throw new IllegalStateException( format( "Unexpected equality op [%s]", op ) );
        }
    }


    private static boolean opRelationalResult( String op, Selection firstResult, Selection secondResult )
    {
        final Number first = firstResult.toNumber();
        final Number second = secondResult.toNumber();

        if ( first == null || second == null )
        {
            throw new IllegalStateException( format( "One or both results cannot be coerced to a number: first=[%s], second=[%s]", firstResult, secondResult ) );
        }

        switch ( op )
        {
            case ">":
                return ( first.doubleValue() > second.doubleValue() );

            case ">=":
                return ( first.doubleValue() >= second.doubleValue() );

            case "<":
                return ( first.doubleValue() < second.doubleValue() );

            case "<=":
                return ( first.doubleValue() <= second.doubleValue() );

            default:
                throw new IllegalStateException( format( "Unexpected relational op [%s]", op ) );
        }
    }


    private static Double opAdditativeResult( String op, Selection firstResult, Selection secondResult )
    {
        final Number first = firstResult.toNumber();
        final Number second = secondResult.toNumber();

        if ( first == null || second == null )
        {
            throw new IllegalStateException( format( "One or both results cannot be coerced to a number: first=[%s], second=[%s]", firstResult, secondResult ) );
        }

        switch ( op )
        {
            case "+":
                return ( first.doubleValue() + second.doubleValue() );

            case "-":
                return ( first.doubleValue() - second.doubleValue() );

            default:
                throw new IllegalStateException( format( "Unexpected additive op [%s]", op ) );
        }
    }


    private static Double opMultiplicativeResult( String op, Selection firstResult, Selection secondResult )
    {
        final Number first = firstResult.toNumber();
        final Number second = secondResult.toNumber();

        if ( first == null || second == null )
        {
            throw new IllegalStateException( format( "One or both results cannot be coerced to a number: first=[%s], second=[%s]", firstResult, secondResult ) );
        }

        switch ( op )
        {
            case "*":
                return ( first.doubleValue() * second.doubleValue() );

            case "div":
                return ( first.doubleValue() / second.doubleValue() );

            case "mod":
                return ( first.doubleValue() % second.doubleValue() );

            default:
                throw new IllegalStateException( format( "Unexpected multiplicative op [%s]", op ) );
        }
    }


    @Override
    public Selection visit( UnaryExpr node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( Minus node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( Slash node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( SlashSlash node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( ForwardAxis node, Gob gob, Selection axis )
    {
        return new Selection().withText( node.getValue() );
    }


    @Override
    public Selection visit( AbbrevReverseStep node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( Wildcard node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( NCNameColonStar node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( StringLiteral node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( IntegerLiteral node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( DecimalLiteral node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( VarName node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( AnyKindTest node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( TextTest node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( CommentTest node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( PITest node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( NCName node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( QName node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( FunctionQName node, Gob gob, Selection axis )
    {
        return null;
    }

    @Override
    public Selection visit( ContextItemExpr node, Gob gob, Selection axis )
    {
        return null;
    }

}
