/* Generated By:JJTree: Do not edit this line. OrExpr.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.brentcroft.util.xpath.ast;

import com.brentcroft.util.xpath.gob.Gob;
import com.brentcroft.util.xpath.SimpleNode;
import com.brentcroft.util.xpath.XParser;
import com.brentcroft.util.xpath.XParserVisitor;
import com.brentcroft.util.xpath.gob.Selection;

public class OrExpr extends SimpleNode
{
    public OrExpr( int id )
    {
        super( id );
    }

    public OrExpr( XParser p, int id )
    {
        super( p, id );
    }

    public Selection accept( XParserVisitor visitor, Gob gob, Selection axis )
    {
        return visitor.visit( this, gob, axis );
    }

}
/* JavaCC - OriginalChecksum=f2976280f84e20dcdc89db5f747e810b (do not edit this line) */