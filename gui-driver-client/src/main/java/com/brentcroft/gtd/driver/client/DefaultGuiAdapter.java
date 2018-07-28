package com.brentcroft.gtd.driver.client;

import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * Created by adobson on 20/05/2016.
 */
public class DefaultGuiAdapter implements GuiAdapter
{
    protected final static Logger logger = Logger.getLogger( DefaultGuiAdapter.class );

    private Properties credentials = new Properties();

    protected interface Key
    {
        String key();
    }

    enum Credential implements Key
    {
        USERNAME( "username" ),
        PASSWORD( "password" );

        private final String key;

        Credential( String key )
        {
            this.key = key;
        }

        Credential()
        {
            this.key = name().toLowerCase();
        }

        public String key()
        {
            return key;
        }
    }


    protected GuiDriver driver = null;

    public void setDriver( GuiDriver driver )
    {
        this.driver = driver;
    }

    public void newCredentials( Properties newCredentials )
    {
        this.credentials = newCredentials;
    }

    public void setCredentials( Properties newCredentials )
    {
        for ( Credential c : Credential.values() )
        {
            if ( newCredentials.containsKey( c.key() ) )
            {
                credentials.put( c.key(), newCredentials.get( c.key() ) );
            }
        }
    }

    @Override
    public boolean hasCredentials()
    {
        return credentials != null;
    }

    public Properties getCredentials()
    {
        return credentials;
    }


    protected String getUsername()
    {
        return credentials.getProperty( Credential.USERNAME.key(), "anonymous" );
    }

    protected char[] getPassword()
    {
        return credentials.containsKey( Credential.PASSWORD.key() )
                ? credentials.getProperty( Credential.PASSWORD.key() ).toCharArray()
                : null;
    }

    public void login( double timeoutSeconds )
    {
        // overridden by sub classes
    }

    /**
     * Should only read the GUI to decide if logged in or not as this will get called every few seconds to maintain the
     * status label.
     */
    public boolean isLoggedIn( double loginTimeout )
    {
        return true;
    }


    /**
     * Sends a click to:<br/>
     * <p>
     * <code>//JMenu[ @text='File' ]</code><br/>
     * <code>//JMenuItem[ @text='Exit' ]</code><br/>
     */
    public void logout( double timeoutSeconds )
    {
        driver.click( "//JMenu[ @text='File' ]", timeoutSeconds );
        driver.click( "//JMenuItem[ @text='Exit' ]", timeoutSeconds );
    }
}
