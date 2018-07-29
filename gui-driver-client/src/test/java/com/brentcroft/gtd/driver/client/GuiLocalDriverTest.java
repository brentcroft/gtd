package com.brentcroft.gtd.driver.client;

import org.junit.Test;

/**
 * Created by Alaric on 21/10/2016.
 */
public class GuiLocalDriverTest
{
    @Test
    public void configure() throws Exception
    {
        GuiLocalDriver driver = new GuiLocalDriver(  );

        System.out.println( driver );
    }

    @Test
    public void configureFromFile() throws Exception
    {
        GuiLocalDriver driver = new GuiLocalDriver(  );

        System.out.println( driver );
    }
}