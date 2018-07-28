package com.brentcroft.gtd.swingset2;

import com.brentcroft.gtd.driver.client.DefaultGuiAdapter;
import com.brentcroft.util.Waiter8;

import static com.brentcroft.util.DateUtils.secondsToMillis;

/**
 * Created by Alaric on 21/10/2016.
 */

public class SwingSet2GuiAdapter extends DefaultGuiAdapter
{
    private final static String PASSWORD_DIALOG_PREDICATE = "[ ancestor::SwingSet2-PassWordDialog ]";

    private String swingApp = "//SwingSet2";

    private String usernameField = "//JTextField" + PASSWORD_DIALOG_PREDICATE;
    private String passwordField = "//JPasswordField" + PASSWORD_DIALOG_PREDICATE;

    private String loginButton = "//JButton[ @text='Login' ]" + PASSWORD_DIALOG_PREDICATE;
    private String cancelButton = "//JButton[ @text='Cancel' ]" + PASSWORD_DIALOG_PREDICATE;

    @Override
    public void login( double timeoutSeconds )
    {
        long started = System.currentTimeMillis();

        if ( driver.exists( usernameField, timeoutSeconds ) )
        {
            String username = getUsername();
            char[] password = getPassword();

            driver.setText( usernameField, username == null ? "" : username );
            driver.setText( passwordField, password == null ? "" : new String( password ) );

            driver.click( loginButton );

            // in decimal seconds
            double remainingTimeout = Math.max( 5.0, timeoutSeconds - ( started / 1000 ) );

            if ( !driver.exists( swingApp, remainingTimeout ) )
            {
                driver.click( cancelButton );
            }
        }
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

        new Waiter8()
                .withDelayMillis( secondsToMillis( 1.0 ) )
                .withTimeoutMillis( secondsToMillis( 5.0 ) )
                .until( () -> {
                    try
                    {
                        return !"hello".equals( driver.echo( "hello" ) );
                    }
                    catch ( Exception e )
                    {
                        System.out.println( "Assuming logged out: " + e );

                        // we're logged out
                        return true;
                    }
                } );
    }

}
