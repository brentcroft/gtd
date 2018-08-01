package com.brentcroft.gtd.driver.client;

import static java.lang.String.format;

import java.util.Properties;

import org.apache.log4j.Logger;

import com.brentcroft.util.DateUtils;

/**
 * Created by Alaric on 21/10/2016.
 */
public class GuiLocalSession extends AbstractGuiSession
{
	private final static Logger logger = Logger.getLogger( GuiLocalSession.class );

	private Long started = null;
	private Long stopped = null;

	private String onStartedScript = null;

	public boolean isStarted()
	{
		return !isStopped();
	}

	public boolean isStopped()
	{
		return started == null;
	}

	@Override
	public void start()
	{
		try
		{
			changeState( State.STARTING );

			GuiDriver driver = getDriver();

			// inaccessible if Gui hasn't been started
			// fast timeout as we're checking if a JMX connection is already open
			if ( launcher.isHarnessAccessible( driver, getEchoTimeoutSeconds() ) && isLoggedIn() )
			{
				logger.info( format( "Harness accessible and adapter logged in; re-started %s", launcher ) );

				changeState( State.STARTED );

				started = System.currentTimeMillis();

				// re-send session properties to harness for configuration (might have reloaded
				// config)
				initialiseRemote();

				return;
			}

			// try stopping it first
			// we already tried to connect
			// so this is a speculative attempt
			// to kill a zombie

			// logger.info( "About to try speculative stop..." );
			//
			// stop();

			if ( !launcher.isLaunchable() )
			{
				throw new RuntimeException("Application not accessible and has no launcher!");
			}

			logger.info( "About to start the application..." );

			// ok - try starting it
			// will raise an exception if can't connect

			launcher.startApplication();

			changeState( State.STARTED );

			started = System.currentTimeMillis();

			// send session properties to harness for configuration
			initialiseRemote();

			logger.info( "About to login..." );

			login();

		}
		finally
		{
			if ( getState() == State.STARTING )
			{
				// there must have been an exception when starting the application
				changeState( State.FAILED );
			}

			logger.info( format( "Session: %s", this ) );
		}
	}

	public void initialiseRemote()
	{
		Properties p = getProperties();

		if ( logger.isDebugEnabled() )
		{
			logger.debug( "Setting harness properties: " + p );
		}

		getDriver().setProperties( p );

		if ( onStartedScript != null && !onStartedScript.isEmpty() )
		{
			getDriver().configure( onStartedScript );
		}
	}

	/**
	 * First tries to logout, then tries to stop the application, then tries to
	 * shutdown the harness harness.
	 */
	@Override
	public void stop()
	{
		changeState( State.STOPPING );

		try
		{
			logger.info( "About to logout..." );

			logout();

			logger.info( "Logged out." );
		}
		catch ( Exception e )
		{
			// it was a speculative attempt
			if ( logger.isTraceEnabled() )
			{
				logger.trace( "Speculative logout on stop failed: " + e.getMessage() );
			}
		}

		if ( launcher != null )
		{
			try
			{
				launcher.stopApplication();

				logger.info( "Application stop completed." );
			}
			catch ( Exception e )
			{
				// it was a speculative attempt
				logger.warn( "An exception was raised while stopping the application: " + e, e );
			}
		}
		else
		{
			logger.info( "No launcher!" );
		}

		try
		{
			getDriver().shutdown( 0 );
		}
		catch ( Exception e )
		{
			// it was a speculative attempt
			logger.warn( "An exception was raised while shutting down the harness: " + e, e );
		}
		finally
		{
			// primarily to drop its listeners
			getDriver().cleanup();

			started = null;
			stopped = System.currentTimeMillis();

			changeState( State.STOPPED );

			logger.info( format( "Session: %s", this ) );
		}
	}

	/**
	 * The adapter has to communicate with the harness using the driver
	 *
	 * @return
	 */
	@Override
	public GuiSession login()
	{
		GuiAdapter guiAdapter = getGuiAdapter();

		if ( guiAdapter == null )
		{
			return this;
		}

		if ( !guiAdapter.hasCredentials() )
		{
			guiAdapter.setCredentials( getProperties() );
		}

		guiAdapter.login( getLoginTimeoutSeconds() );

		return this;
	}

	/**
	 * This can get called often so should not modify the GUI.
	 * <p/>
	 * It should simply read.
	 *
	 * @return
	 */
	@Override
	public boolean isLoggedIn()
	{
		GuiAdapter guiAdapter = getGuiAdapter();

		// TODO should this have its own timeout??
		// the adapter is free to set a smaller one
		return guiAdapter == null
				|| guiAdapter.isLoggedIn( getLoginTimeoutSeconds() );
	}

	@Override
	public GuiSession logout()
	{
		GuiAdapter guiAdapter = getGuiAdapter();

		if ( guiAdapter != null )
		{
			guiAdapter.logout( getLoginTimeoutSeconds() );
		}
		return this;
	}

	@Override
	public GuiSession withCheckInstanceTimeout( double seconds )
	{
		setEchoTimeoutSeconds( seconds );
		return this;
	}

	@Override
	public void setOnStarted( String onStartedScript )
	{
		this.onStartedScript = onStartedScript;
	}

	public String toString()
	{
		return format(
				"started=[%s]%nstopped=[%s]%n%s",
				started == null ? "-" : DateUtils.timestamp( started ),
				stopped == null ? "-" : DateUtils.timestamp( stopped ),
				super.toString() );
	}
}
