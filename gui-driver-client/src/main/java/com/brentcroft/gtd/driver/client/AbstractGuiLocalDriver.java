package com.brentcroft.gtd.driver.client;

import com.brentcroft.gtd.driver.GuiControllerMBean;
import com.brentcroft.util.DateUtils;
import com.brentcroft.util.Waiter8;
import com.brentcroft.util.buffer.AsynchBuffer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.JMX;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.apache.log4j.Logger;

import static java.lang.String.format;

public abstract class AbstractGuiLocalDriver implements GuiDriver
{
	private final static Logger logger = Logger.getLogger( AbstractGuiLocalDriver.class );

	// provider a serial number for each driver
	private static int nextSerial = 0;
	protected final int serial = nextSerial++;

	protected String jmxRmiUrl = "service:jmx:rmi:///jndi/rmi://:9999/jmxrmi";

	protected double defaultRelaxSeconds = 1.0;
	protected double defaultPollDelaySeconds = 1.0;
	protected double defaultTimeoutSeconds = 5.0;

	// TODO; where from???
	protected String mBeanRef = "XXXX";
	protected ObjectName mbeanName;

	// buffer and asynchronously notify harness Notifications
	private AsynchBuffer< Notification > remoteNotificationBuffer = createBuffer();
	protected Map< Integer, NotificationListener > notificationListeners = new HashMap< Integer, NotificationListener >();

	//
	private MBeanServerConnection serverConnection = null;
	private GuiControllerMBean controller = null;

	public void setMBeanRef( String mBeanRef )
	{
		this.mBeanRef = mBeanRef;
	}

	//
	private final NotificationListener remoteNotificationListener = ( notification, handback ) -> {

		// make a copy to decouple from rmi
		Notification copy = new Notification(
				notification.getType(),
				"" + notification.getSource(),
				notification.getSequenceNumber(),
				notification.getTimeStamp(),
				notification.getMessage() );

		remoteNotificationBuffer.add( copy );
	};

	public String toString()
	{
		return format( "[%s]:# driver%n" +
				"jmxrmi.url=[%s]%n" +
				"mbean-ref=[%s]%n" +
				"default.pollDelay.seconds=[%s]%n" +
				"default.timeout.seconds=[%s]%n" +
				"default.relax.seconds=[%s]",
				this.serial,
				this.jmxRmiUrl,
				this.mBeanRef,
				this.defaultPollDelaySeconds,
				this.defaultTimeoutSeconds,
				this.defaultRelaxSeconds );
	}

	/**
	 * Pause for an amount of time <code>defaultRelaxSeconds</code> so that the
	 * target GUI can digest recent commands.
	 * <p/>
	 * This should be called after each and every harness invocation.
	 */
	protected void relax()
	{
		if ( defaultRelaxSeconds > 0 )
		{
			Waiter8.delay( DateUtils.secondsToMillis( defaultRelaxSeconds ) );
		}
	}

	/**
	 * Obtain a JMX Bean to manipulate and interrogate the Gui.
	 *
	 * @return a GuiControllerMBean instance
	 * @throws IOException
	 * @throws MalformedObjectNameException
	 * @throws InstanceNotFoundException
	 * @throws Exception
	 *             if the GuiControllerMBean instance can't be obtained.
	 */
	protected synchronized GuiControllerMBean remote()
	{
		if ( controller != null )
		{
			try
			{
				controller.echo( "echo" );

				// good bean
				return controller;
			}
			catch ( Exception e )
			{
				// bad bean
				controller = null;
			}
		}

		try
		{
			JMXServiceURL url = new JMXServiceURL( jmxRmiUrl );
			JMXConnector jmxc = JMXConnectorFactory.connect( url, null );

			serverConnection = jmxc.getMBeanServerConnection();

			mbeanName = new ObjectName( mBeanRef );

			final GuiControllerMBean mbeanProxy = JMX.newMBeanProxy(
					serverConnection,
					mbeanName,
					GuiControllerMBean.class,
					true );

			// register listener with the driver
			// to receive relayed notifications
			// allow retries - as can fail first time
			new Waiter8()
					.withTimeoutMillis( 5000 )
					.withDelayMillis( 100 )
					.onTimeout( t -> {
						String msg = format( "[%s] Giving up trying to attach remote notification listener: %s", serial,
								jmxRmiUrl );
						logger.warn( msg );
						// throw new RuntimeException( msg );
					} )
					.until( this::attachRemoteNotificationListener );

			controller = mbeanProxy;

			if ( logger.isDebugEnabled() )
			{
				logger.debug( format( "[%s] Created new MBeanProxy: %s", serial, controller ) );
			}

			return mbeanProxy;
		}
		catch ( Exception e )
		{
			// so on next call
			controller = null;
			serverConnection = null;

			throw new GuiDriverException(
					format( "[%s] Failed to obtain MBean: url=[%s], id=[%s], cause=[%s].",
							serial,
							jmxRmiUrl,
							mBeanRef, e ),
					e );
		}
	}

	public void cleanup()
	{
		removeAllNotificationListeners();
		detachRemoteNotificationListener();
	}

	public boolean attachRemoteNotificationListener()
	{
		if ( serverConnection != null )
		{
			try
			{
				serverConnection.addNotificationListener( mbeanName, remoteNotificationListener, null, null );

				logger.debug( format( "[%s] Attached harness listener: [%s], listener=[%s].", serial, mBeanRef,
						remoteNotificationListener ) );

				return true;
			}
			catch ( Exception e )
			{
				logger.warn( format( "[%s] Error attaching harness listener: [%s]; error=[%s]", serial, mBeanRef, e ) );
			}
		}

		return false;
	}

	public void detachRemoteNotificationListener()
	{
		if ( serverConnection != null )
		{
			try
			{
				serverConnection.removeNotificationListener(
						mbeanName,
						remoteNotificationListener );

				logger.debug( format( "[%s] Detached harness listener: [%s], listener=[%s].",
						serial,
						mBeanRef,
						remoteNotificationListener ) );
			}
			catch ( IOException | ListenerNotFoundException ignored )
			{
				// terminated
			}
			catch ( Exception e )
			{
				logger.warn( format( "[%s] Error detaching harness listener: [%s], listener=[%s]; %",
						serial,
						mBeanRef,
						remoteNotificationListener,
						e ) );
			}
		}
	}

	public String getJmxRmiUrl()
	{
		return jmxRmiUrl;
	}

	public void setJmxRmiUrl( String jmxRmiUrl )
	{
		this.jmxRmiUrl = jmxRmiUrl;
	}

	public double getDefaultPollDelaySeconds()
	{
		return defaultPollDelaySeconds;
	}

	public void setDefaultPollDelaySeconds( double defaultPollDelaySeconds )
	{
		this.defaultPollDelaySeconds = defaultPollDelaySeconds;
	}

	public double getDefaultTimeout()
	{
		return defaultTimeoutSeconds;
	}

	public void setDefaultTimeout( double defaultTimeout )
	{
		this.defaultTimeoutSeconds = defaultTimeout;
	}

	public void setDefaultRelaxSeconds( double defaultRelaxSeconds )
	{
		this.defaultRelaxSeconds = defaultRelaxSeconds;
	}

	public double getDefaultRelaxSeconds()
	{
		return defaultRelaxSeconds;
	}

	// JMX Notification

	public void addNotificationListener( NotificationListener nl )
	{
		if ( logger.isDebugEnabled() )
		{
			logger.debug( format( "[%s] Added NotificationListener [%s].", serial, nl ) );
		}

		notificationListeners.put( nl.hashCode(), nl );
	}

	public void removeNotificationListener( NotificationListener nl )
	{
		if ( !notificationListeners.containsKey( nl.hashCode() ) )
		{
			return;
		}

		notificationListeners.remove( nl.hashCode() );

		if ( logger.isDebugEnabled() )
		{
			logger.debug( format( "[%s] Dropped NotificationListener [%s].", serial, nl ) );
		}
	}

	public void removeAllNotificationListeners()
	{
		if ( notificationListeners.isEmpty() )
		{
			return;
		}

		notificationListeners.clear();

		if ( logger.isDebugEnabled() )
		{
			logger.debug( format( "[%s] Dropped all NotificationListeners.", serial ) );
		}
	}

	private void notifyListeners( Notification notification )
	{
		if ( notificationListeners.isEmpty() )
		{
			return;
		}

		for ( NotificationListener nl : notificationListeners.values() )
		{
			if ( nl != null )
			{
				nl.handleNotification( notification, null );
			}
		}
	}

	private AsynchBuffer< Notification > createBuffer()
	{
		return new AsynchBuffer< Notification >( format( "[%s] Notification Buffer", serial ) )
		{
			public void process( Notification notification )
			{
				if ( logger.isTraceEnabled() )
				{
					logger.trace( format( "[%s] Processing Notification: type=[%s], seq[%s], timestamp=[%s].",
							serial,
							notification.getType(),
							notification.getSequenceNumber(),
							notification.getTimeStamp() ) );
				}

				notifyListeners( notification );
			}
		};
	}
}