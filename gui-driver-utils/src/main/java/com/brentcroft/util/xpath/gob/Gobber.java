package com.brentcroft.util.xpath.gob;

import com.brentcroft.util.xpath.XParser;
import com.brentcroft.util.xpath.ast.START;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

public class Gobber
{
    public static Gobber newGobber( String xpath )
    {
        return new Gobber( xpath );
    }

    private START node = null;

    public Gobber( String xpath )
    {
        try
        {
            node = new XParser(
                    new ByteArrayInputStream( xpath.getBytes( StandardCharsets.UTF_8.name() ) )
            ).START();
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }


    public String getPath()
    {
        return node == null ? null : node.getPath();
    }

    public Selection execute()
    {
        return execute( null );
    }

    public Selection execute( Gob gob )
    {
        GobVisitor v = new GobVisitor();

        Selection selection = new Selection().withGob( gob );

        return v.visit( node, gob, selection );
    }

    public void dump()
    {
        node.dump( "  " );
    }
}
