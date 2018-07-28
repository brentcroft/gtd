package cucumber.runtime.formatter;

import java.io.IOException;
import java.net.URL;

public class CucumberJUnitFormatter extends JUnitFormatter
{

    public CucumberJUnitFormatter( URL out ) throws IOException
    {
        super( out );
    }

}
