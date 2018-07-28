package com.brentcroft.util;

import java.util.stream.IntStream;
import org.junit.Test;

import static java.lang.String.format;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

/**
 * Created by Alaric on 17/07/2017.
 */
public class TimeKeySequenceTest
{
    int maxLoops = 100 ^ 3;

    int maxLines = 5;

    @Test
    public void SimpleTimeKeySequence() throws Exception
    {
        TimeKeySequence seq = TimeKeySequence.newSimpleTimeKeySequence();

        String[] lastKey = { null };

        int printEvery = maxLoops / maxLines;

        IntStream.range( 0, maxLoops )
                .forEach( i ->
                {
                    String key = seq.nextValue();

                    assertNotNull( "new key must not be null", key );

                    assertNotSame(
                            format( "last key [%s] must not equal new key [%s]", lastKey[ 0 ], key ), lastKey[ 0 ], key );

                    if ( lastKey[ 0 ] != null )
                    {
                        String[] lastKeyParts = lastKey[ 0 ].split( ":" );
                        String[] keyParts = key.split( ":" );

                        assertTrue(
                                "key splits on colon into two parts",
                                keyParts.length == 2 );

                        //
                        long[] lastKeyValues = {
                                Long.valueOf( lastKeyParts[ 0 ] ),
                                Long.valueOf( lastKeyParts[ 1 ] )
                        };
                        long[] keyValues = {
                                Long.valueOf( keyParts[ 0 ] ),
                                Long.valueOf( keyParts[ 1 ] )
                        };


                        assertTrue(
                                format( "last key first part [%d] must be less than or equal to new key first part [%d]", lastKeyValues[ 0 ], keyValues[ 0 ] ),
                                lastKeyValues[ 0 ] <= keyValues[ 0 ] );


                        if ( keyValues[ 0 ] == lastKeyValues[ 0 ] )
                        {
                            assertTrue(
                                    format( "if first parts are equal then last key second part [%d] must be strictly less than new key second part [%d]", lastKeyValues[ 1 ], keyValues[ 1 ] ),
                                    lastKeyValues[ 1 ] < keyValues[ 1 ] );
                        }
                        else
                        {
                            assertTrue(
                                    format( "if first parts are not equal then new key first part [%d] must equal 0", keyValues[ 1 ] ),
                                    0 == keyValues[ 1 ] );
                        }
                    }

                    lastKey[ 0 ] = key;

//                    if ( i % printEvery == 0 )
//                    {
//                        System.out.println( format( "key: [%010d] %s", i, key ) );
//                    }
                } );

//        System.out.println( format( "seq: serial=[%d], max-index=[%d].", seq.getSerial(), seq.getMaximumIndex() ) );
    }

}