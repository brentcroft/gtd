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
public class XParserArithmeticTest
{

    @Parameterized.Parameters
    public static Collection< Object[] > data()
    {
        return Arrays.asList( new Object[][]{
                { "( 1 + 2 + 3 )", ( 1 + 2 + 3 ) },
                { "( 1 - 2 + 3 - 4 + 5 )", ( 1 - 2 + 3 - 4 + 5 ) },
                { "( 1 * 2 * 3 * 4 * 5 )", ( 1 * 2 * 3 * 4 * 5 ) },
                { "( 52 div 13 )", ( 52 / 13 ) },
                { "( 52 mod 7 )", ( 52 % 7 ) },
                { "( ( 52 mod 7 ) * ( 52 div 13 ) + 2 ) div 7", ( ( 52 % 7 ) * ( 52 / 13 ) + 2 ) / 7 }
        } );
    }

    private String xpath;

    private Object expected;

    public XParserArithmeticTest( String xpath, Number expected )
    {
        this.xpath = xpath;
        this.expected = expected.doubleValue();
    }


    @Test
    public void processParenthesesNumberAdd() throws Exception
    {
        Gobber gobber = Gobber.newGobber( xpath );

        Selection result = gobber.execute();

        assertNotNull( "No result", result );
        assertTrue( "Selection not a number", result.isNumber() );

        System.out.println( format( "Test: %s = %s", xpath, result.toNumber() ) );

        assertEquals( expected, result.toNumber() );
    }
}
