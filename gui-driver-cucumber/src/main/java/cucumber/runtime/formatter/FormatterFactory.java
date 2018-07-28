package cucumber.runtime.formatter;

import java.io.IOException;
import java.net.URL;

import gherkin.formatter.Reporter;
import gherkin.formatter.model.Match;
import gherkin.formatter.model.Result;

public class FormatterFactory
{
    public static JUnitFormatter getJUnitFormatter( URL url ) throws IOException
    {
        return new JUnitFormatter( url );
    }

    public static UsageFormatter getUsageFormatter( Appendable appendable )
    {
        return new UsageFormatter( appendable );
    }

    public static Reporter getNullReporter()
    {
        return new Reporter()
        {

            @Override
            public void before( Match match, Result result )
            {
            }

            @Override
            public void result( Result result )
            {
            }

            @Override
            public void after( Match match, Result result )
            {
            }

            @Override
            public void match( Match match )
            {
            }

            @Override
            public void embedding( String mimeType, byte[] data )
            {
            }

            @Override
            public void write( String text )
            {
            }
        };
    }
}
