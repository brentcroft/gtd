package com.brentcroft.gtd.utilities;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Alaric on 18/11/2016.
 */

public class NameUtilsCleanTest
{
    @Test
    public void orderedTokens()
    {
        String s = NameUtils.NAME_EXCLUDED_REGEX_TOKENS;

        char lastChar = 0;

        for ( int i = 0, n = s.length(); i < n; i++ )
        {
            char c = s.charAt( i );

            if ( c == '\\' )
            {
                i++;
                c = s.charAt( i );
            }

            //System.out.println( (int)c + " = " + c );

            assertTrue( c > lastChar );

            lastChar = c;
        }
    }

    @Test
    public void cleanName()
    {
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black&Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black & Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black  &  &  Blue" ) );

        assertEquals( "Black_Blue_Yellow_Green", NameUtils.cleanName( "Black! =& Blue & Yellow, Green" ) );

        assertEquals( "d_1234", NameUtils.cleanName( "1234" ) );

        //
        assertEquals( "d_1234", NameUtils.cleanName( "1234&" ) );
    }


    @Test
    public void removesQuotes()
    {
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black&'Blue'" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black \"&\" Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black  '&'  '&'  Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black&`Blue`" ) );
    }


    @Test
    public void removesSymbols()
    {
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black#Blue'" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black%Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black^Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black@Blue" ) );

        assertEquals( "Black_Blue", NameUtils.cleanName( "Black.Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black:Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black;Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black/Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black\\Blue" ) );


        assertEquals( "Black_Blue", NameUtils.cleanName( "Black(Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black)Blue" ) );

        assertEquals( "Black_Blue", NameUtils.cleanName( "Black{Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black]Blue" ) );

        assertEquals( "Black_Blue", NameUtils.cleanName( "Black[Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black]Blue" ) );


    }


    @Test
    public void cleanName_NoTrailingUnderscores()
    {
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black&Blue&" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "& Black & Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "& Black  &  /  Blue !" ) );

        assertEquals( "Black_Blue_Yellow_Green", NameUtils.cleanName( "//Black! =& Blue & Yellow, Green / /" ) );

        assertEquals( "d_1234", NameUtils.cleanName( "=1234=" ) );

        //
        assertEquals( "d_1234", NameUtils.cleanName( "-1234&" ) );
    }


    @Test
    public void cleanName_NoTrailingEllipsis()
    {
        assertEquals( "Black_Blue", NameUtils.cleanName( "Black…Blue…" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "… Black …Blue" ) );
        assertEquals( "Black_Blue", NameUtils.cleanName( "… Black  …  …  Blue …" ) );

        assertEquals( "Black_Blue_Yellow_Green", NameUtils.cleanName( "…Black… …… Blue … Yellow, Green … …" ) );

        assertEquals( "d_1234", NameUtils.cleanName( "…1234…" ) );        //
        assertEquals( "d_1234", NameUtils.cleanName( "…1234…" ) );
    }


    @Test
    public void cleanName_NoLowBytes()
    {
        for ( char x = 0; x < ( int ) ' '; x++ )
        {
            assertEquals( "Black_Blue", NameUtils.cleanName( "Black" + x + "Blue" + x ) );
            assertEquals( "Black_Blue", NameUtils.cleanName( x + " Black " + x + "Blue" ) );
            assertEquals( "Black_Blue", NameUtils.cleanName( "" + x + " Black  " + x + "  " + x + "  Blue " + x + "" ) );

            assertEquals( "Black_Blue_Yellow_Green", NameUtils.cleanName( "" + x + "Black" + x + " " + x + "" + x + " Blue " + x + " Yellow, Green " + x + " " + x + "" ) );

            assertEquals( "d_1234", NameUtils.cleanName( "" + x + "1234" + x + "" ) );        //
            assertEquals( "d_1234", NameUtils.cleanName( "" + x + "1234" + x + "" ) );
        }
    }


    @Test
    public void cleanName_Samples()
    {
        assertEquals( "Grades_Reports_progress", NameUtils.cleanName( "Grades: Reports/progress" ) );
    }
}