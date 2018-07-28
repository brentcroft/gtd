package com.brentcroft.gtd.utilities;

import com.brentcroft.util.FileUtils;
import com.brentcroft.util.XmlUtils;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * Created by Alaric on 28/10/2016.
 */

public class XmlAccumulatorTest
{
    Element[] additions = {
            XmlUtils
                    .newDocument(
                            FileUtils
                                    .getFileOrResourceAsReader(
                                            null,
                                            "xml/snapshot-accumulate-001.xml" )
                    ).getDocumentElement(),

            XmlUtils
                    .newDocument(
                            FileUtils
                                    .getFileOrResourceAsReader(
                                            null,
                                            "xml/snapshot-accumulate-002.xml" )
                    ).getDocumentElement(),

            XmlUtils
                    .newDocument(
                            FileUtils
                                    .getFileOrResourceAsReader(
                                            null,
                                            "xml/snapshot-accumulate-003.xml" )
                    ).getDocumentElement()
    };


    @Test
    public void testMergeSequence()
    {
        Document base = XmlUtils.newDocument();


        XmlAccumulator acc = new XmlAccumulator();

        long serial = 1;

        for (Element addition : additions)
        {
            acc.withSerial( serial++ );

            acc.merge( base, addition );

        }

        //
        base.normalizeDocument();

        acc.expandAccData( base, XmlAccumulator.ElementAccDataBiC.ALL );


        System.out.println( XmlUtils.serialize( base ) );
    }

    @Test
    public void testMergeArray()
    {
        Document base = XmlUtils.newDocument();


        XmlAccumulator acc = new XmlAccumulator();

        acc.merge( base, additions );

        //
        base.normalizeDocument();

        acc.expandAccData( base, XmlAccumulator.ElementAccDataBiC.ALL );

        System.out.println( XmlUtils.serialize( base ) );
    }
}