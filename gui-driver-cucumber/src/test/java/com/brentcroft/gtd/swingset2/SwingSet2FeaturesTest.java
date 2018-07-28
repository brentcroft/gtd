package com.brentcroft.gtd.swingset2;

import com.brentcroft.gtd.cucumber.Cucumber;
import com.brentcroft.gtd.js.context.ContextUnit;
import com.brentcroft.util.FileUtils;

import org.junit.Test;

import java.io.PrintWriter;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Alaric on 17/03/2017.
 */
public class SwingSet2FeaturesTest
{
    String configFilename = "src/test/resources/config.xml";

    String featureDir = "src/test/resources/swingset2/features";
    String badFeatureDir = "src/test/resources/swingset2/features-bad";

    @Test
    public void testCucumberMain_featureDirectory()
    {
        Cucumber.main(
                new String[]{
                        configFilename,
                        featureDir
                } );
    }

    @Test
    public void testCucumberMain_oneFeature()
    {
        Cucumber.main(
                new String[]{
                        configFilename,
                        featureDir + "/OnlyOne.feature"
                } );
    }


    @Test()
    public void testCucumberMain_throwsException() throws Exception
    {
        String[] args = new String[]{
                configFilename,
                badFeatureDir + "/JTable.feature"
        };

        ContextUnit unit = new ContextUnit( FileUtils.resolvePath( null, args[ 0 ] ) );

        final String feature = FileUtils.getFileOrResourceAsString( null, args[ 1 ] );

        List< Cucumber.FeatureResult > featureResults = new Cucumber( args[ 1 ] )
                .processFeature( feature, unit.newContext(), new PrintWriter( System.out ) );

        String msg = featureResults
                .stream()
                .filter( Cucumber.FeatureResult::isFailure )
                .map( Cucumber.FeatureResult::toString )
                .collect( Collectors.joining( "\n" ) );


        assertEquals( "expected one feature", 1, featureResults.size() );

        Cucumber.FeatureResult result = featureResults.get( 0 );

        assertTrue( "Expected failure: " + msg, result.isFailure() );
    }
}