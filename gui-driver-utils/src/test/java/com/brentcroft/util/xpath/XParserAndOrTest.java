package com.brentcroft.util.xpath;

import com.brentcroft.util.xpath.gob.Gobber;
import com.brentcroft.util.xpath.gob.Selection;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static java.lang.String.format;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith( Parameterized.class )
public class XParserAndOrTest
{
    @Parameterized.Parameters
    public static Collection< Object[] > data()
    {
        return Arrays.asList( new Object[][]{
                { "( '1' or '2' )", true },
                { "( '1' and '2' )", true },
                { "( 1 or 2 )", true },
                { "( 1 and 2 )", true }
        } );
    }

    private String xpath;

    private Object expected;

    public XParserAndOrTest( String xpath, Boolean expected )
    {
        this.xpath = xpath;
        this.expected = expected;
    }


    @Test
    public void logic() throws Exception
    {
        Gobber gobber = Gobber.newGobber( xpath );

        Selection result = gobber.execute();

        assertNotNull( "No result", result );

        System.out.println( format( "Test: %s = %s", xpath, result ) );

        assertEquals( expected, result.toBoolean() );
    }
}
