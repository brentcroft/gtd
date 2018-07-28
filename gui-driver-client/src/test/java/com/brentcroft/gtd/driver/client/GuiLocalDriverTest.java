package com.brentcroft.gtd.driver.client;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import static org.junit.Assert.*;

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