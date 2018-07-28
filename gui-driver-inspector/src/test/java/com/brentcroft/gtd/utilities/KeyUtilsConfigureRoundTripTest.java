package com.brentcroft.gtd.utilities;

import com.brentcroft.util.CommentedProperties;
import org.junit.Test;

import static com.brentcroft.util.StringUpcaster.downcastProperties;
import static com.brentcroft.util.StringUpcaster.upcastProperties;
import static org.junit.Assert.assertEquals;

/**
 * StringUpcaster uses LinkedHashSet & LinkedHashMap to preserve order,
 * but downcastProperties always sorts on name
 */
public class KeyUtilsConfigureRoundTripTest
{
    String propertiesText = "modeller.naming.include=*=class|guid|icon|id|tab-title|title|tooltip,\\\n" +
                            "\ta=data-original-title|guid|icon|id|text|title|tooltip,\\\n" +
                            "\tbody=guid|id,\\\n" +
                            "\thead=guid|id,\\\n" +
                            "\ttd=class|guid|icon|id|text,\\\n" +
                            "\tth=class|guid|icon|id|text,\\\n" +
                            "\ttitle=text\n" +
                            "modeller.naming.tagMap=\n" +
                            "modeller.xpath.exclude=*=a:actions|disabled|duplicate|enabled|focus|for|for-hash|icon|selected|selected-index|size|visible\n" +
                            "modeller.xpath.lookupLabels=false\n" +
                            "modeller.xpath.nearestOrSiblingLabels=JLabel\n" +
                            "modeller.xpath.positionPredicateAlways=false\n" +
                            "modeller.xpath.positionPredicateIfNoOther=true\n" +
                            "modeller.xpath.primaryAttributes=guid,id\n" +
                            "modeller.xpath.tagAttributeUrls=a=href,\\\n" +
                            "\tform=action,\\\n" +
                            "\tscript=src\n" +
                            "modeller.xpath.tagsAllowedHashForLabels=JComboBox=JLabel,\\\n" +
                            "\tJDateChooser=JLabel,\\\n" +
                            "\tJSlider=JLabel,\\\n" +
                            "\tJTextField=JLabel,\\\n" +
                            "\tMetalFileChooserUI-1=JLabel,\\\n" +
                            "\tinput=label\n" +
                            "modeller.xpath.tagsAllowedHtmlForLabels=input=label\n" +
                            "modeller.xpath.tagsAllowedNamesFromNearestLabels=JComboBox,JDateChooser,JSpinner,JTextArea,JTextField,TreeCombo\n" +
                            "modeller.xpath.tagsAllowedNamesFromSiblingLabels=a1,a2,a3,a4\n" +
                            "modeller.xpath.tx.allowUntranslated=true\n" +
                            "modeller.xpath.tx.attributesToInclude=tab-title,tab-tooltip,text,title,tooltip\n" +
                            "modeller.xpath.tx.generateMissingEntries=false\n" +
                            "modeller.xpath.tx.refFormat=${ tx[ '%s' ] }";


    @Test
    public void roundTrip()
    {
        CommentedProperties expected = upcastProperties( propertiesText );

        CommentedProperties temp = new CommentedProperties();
        CommentedProperties actual = new CommentedProperties();

        new KeyUtils()
                .configure( expected )
                .export( temp );

        new KeyUtils()
                .configure( temp )
                .export( actual );

        System.out.println( downcastProperties( actual ) );

        expected
                .entrySet()
                .forEach( entry -> {
                    assertEquals( entry.getValue(), actual.getProperty( entry.getKey().toString() ) );
                } );
    }


}