package com.brentcroft.gtd.driver.client;

import java.util.Properties;

/**
 * A GuiAdapter is an optional component of a GuiSession.<p/>
 *
 * It provides: <br/>
 * <ul>
 * <li>
 *     A <code>login( double timeoutSeconds )</code> method that is called by the session's start method immediately after a new harness has been
 *     launched.<br/>
 *     An adapter's <code>login</code> method should manipulate the application under control (AUC)
 *     using the session's driver to achieve a desired starting state, e.g. login, set initial data etc.<br/>
 * </li>
 *
 * <li>
 *     An <code>isLoggedIn( double timeoutSeconds )</code> method to check if a harness is accessible.<br/>
 * </li>
 *
 * <li>
 *     A <code>logout( double timeoutSeconds )</code> method that is called by the session's stop method
 *     to allow the application under control (AUC) to be terminated gracefully.<br/>
 *     An adapter's <code>logout</code> method should manipulate the application under control (AUC)
 *     using the session's driver to achieve a desired ending state, e.g. save open documents etc.<br/><br/>
 *     Also, note that if the harness was launched with <code>shutdownHook=true</code> then,
 *     after calling the adapter's <code>logout</code> method, the session's stop method
 *     will terminate the launched JVM abruptly.<br/>
 * </li>
 * </ul>
 *
 * @author adobson 20/05/2016.
 */
public interface GuiAdapter
{
    void setDriver( GuiDriver driver );


    /**
     * Provide arbitrary properties to the adapter.<p/>
     *
     * E.g. username, encrypted-password, initial-uri.
     *
     * @param credentials
     */
    void setCredentials( Properties credentials );


    /**
     * Assign an entirely new credentials Properties.
     *
     * @param credentials
     */
    void newCredentials( Properties credentials );


    boolean hasCredentials();



    /**
     * Do some kind of login.<br/>
     *
     * Perhaps using the credentials.
     */
    void login( double timeoutSeconds );


    /**
     * Do some kind of logout.<br/>
     *
     * Perhaps using the credentials.
     */
    void logout( double timeoutSeconds );


    /**
     * Check if logged in as required.
     *
     *
     * @param timeoutSeconds
     * @return
     */
    boolean isLoggedIn( double timeoutSeconds );

}
