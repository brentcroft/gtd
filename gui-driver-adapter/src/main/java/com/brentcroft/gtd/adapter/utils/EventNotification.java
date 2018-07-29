package com.brentcroft.gtd.adapter.utils;

import static com.brentcroft.util.XmlUtils.addXmlnsPrefixNamespaceDeclaration;
import static com.brentcroft.util.XmlUtils.maybeSetElementAttribute;
import static com.brentcroft.util.XmlUtils.newDocument;
import static com.brentcroft.util.XmlUtils.removeTrimmedEmptyTextNodes;
import static com.brentcroft.util.XmlUtils.serialize;
import static java.lang.String.format;

import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.Notification;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.brentcroft.gtd.driver.Backend;
import com.brentcroft.gtd.driver.GuiObjectService;
import com.brentcroft.gtd.driver.utils.DataLimit;
import com.brentcroft.gtd.events.AWTEventUtils;
import com.brentcroft.gtd.events.DOMEventHandler;
import com.brentcroft.gtd.events.DOMEventUtils;
import com.brentcroft.gtd.events.FXEventUtils;
import com.brentcroft.gtd.events.JMXNotifier;
import com.brentcroft.util.DateUtils;
import com.sun.javafx.stage.StageHelper;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;

/**
 * Created by Alaric on 25/05/2017.
 */
public class EventNotification
{
	private final static Logger logger = Logger.getLogger( EventNotification.class );

	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private final AtomicLong notificationSequenceNumber = new AtomicLong( 0 );

	private final GuiObjectService guiObjectService;
	private final JMXNotifier jmxNotifier;

	private final AWTEventUtils awtEventUtils = new AWTEventUtils();
	private final FXEventUtils fxEventUtils = new FXEventUtils();
	private final DOMEventUtils domEventUtils = new DOMEventUtils();

	private DOMEventHandler domEventHandler = null;
	private EventHandler< Event > fxEventHandler = null;
	private AWTEventListener awtEventListener = null;

	private boolean logNotifications = false;

	public EventNotification( GuiObjectService guiObjectService, JMXNotifier jmxNotifier )
	{
		this.guiObjectService = guiObjectService;
		this.jmxNotifier = jmxNotifier;
	}

	public void shutdownNow( long howLongMillis )
	{
		try
		{
			logger.info( "Shutting down executor." );

			executor.shutdown();
			executor.awaitTermination( howLongMillis, TimeUnit.MILLISECONDS );
		}
		catch ( InterruptedException e )
		{
			logger.info( "Timed out waiting for shutdown: " + e );
		}
		finally
		{
			if ( !executor.isTerminated() )
			{
				logger.info( "Remaining task will die." );
			}

			executor.shutdownNow();

			logger.info( "Shutdown complete." );
		}
	}

	public Notification buildNotification( String eventTag, String id, long seq, String params, Object target )
	{
		Document document = newDocument();

		Element element = document.createElement( eventTag );
		document.appendChild( element );

		addXmlnsPrefixNamespaceDeclaration( element, Backend.XML_NAMESPACE_TAG, Backend.XML_NAMESPACE_URI );

		maybeSetElementAttribute( element, "id", id );
		maybeSetElementAttribute( element, "seq", seq );
		maybeSetElementAttribute( element, "params", params );
		maybeSetElementAttribute( element, "timestamp", DateUtils.timestamp() );

		// if target is null then will snapshot the entire gui
		if ( target != null )
		{
			try
			{
				guiObjectService
						.getGuiObjectLocator()
						.takeSnapshot( target, element, DataLimit.getShallowOptions() );
			}
			catch ( Exception e )
			{
				// not adaptable, whatever...
				logger.warn( format( "Snapshot error: event=[%s], id=[%s], target=[%s]: %s", eventTag, id, target, e ) );
			}
		}

		removeTrimmedEmptyTextNodes( document );

		return new Notification( eventTag, id, seq, serialize( document, true, true ) );
	}

	public void installAWTEventNotifications( long notificationEventMask )
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();

		if ( awtEventListener != null )
		{
			uninstallAWTEventNotifications( notificationEventMask );
		}

		// awtEventListener = getHandler( "awt-event" );
		awtEventListener = awtEventUtils.getHandler( awtEvent -> {
			executor.submit( () -> {
				Notification n = buildNotification(
						"awt-event",
						"" + awtEvent.getID(),
						notificationSequenceNumber.incrementAndGet(),
						awtEventUtils.getParams( awtEvent ),
						awtEvent.getSource() );

				if ( n != null )
				{
					jmxNotifier.sendNotification( n );

					if ( logNotifications )
					{
						logger.debug( format( "Notification: seq=[%s], time=[%s], type=[%s], message=[%s].",
								n.getSequenceNumber(),
								n.getTimeStamp(),
								n.getType(),
								n.getMessage()
						) );
					}
				}
			} );
		} );

		toolkit.addAWTEventListener( awtEventListener, notificationEventMask );

		logger.info( format( "Re-installed AWT Global Listener: mask=[%s].", notificationEventMask ) );
	}

	public void uninstallAWTEventNotifications( long notificationEventMask )
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();

		// always try
		toolkit.removeAWTEventListener( awtEventListener );

		awtEventListener = null;

		logger.info( format( "Uninstalled AWT Global Listener: mask=[%s].", notificationEventMask ) );
	}

	public void installFXEventNotifications( EventType eventType )
	{
		if ( fxEventHandler != null )
		{
			uninstallFXEventNotifications( eventType );
		}

		fxEventHandler = fxEventUtils.getHandler( fxEvent -> {
			executor.submit( () -> {
				if ( fxEventUtils.canIgnore( fxEvent ) )
				{
					return;
				}

				Notification n = buildNotification(
						"fx-event",
						fxEvent.getEventType().getName(),
						notificationSequenceNumber.incrementAndGet(),
						fxEventUtils.getParams( fxEvent ),
						fxEvent.getTarget() );

				if ( n != null )
				{
					jmxNotifier.sendNotification( n );

					if ( logNotifications )
					{
						logger.debug( format( "Notification: seq=[%s], time=[%s], type=[%s], message=[%s].",
								n.getSequenceNumber(),
								n.getTimeStamp(),
								n.getType(),
								n.getMessage()
						) );
					}
				}
			} );
		} );

		StageHelper
				.getStages()
				.forEach( stage -> stage.addEventFilter( eventType, fxEventHandler ) );

		logger.info( format( "Re-installed Stage FX Listeners: type=[%s].", eventType ) );
	}

	public void uninstallFXEventNotifications( EventType eventType )
	{
		if ( fxEventHandler != null )
		{
			StageHelper
					.getStages()
					.forEach( stage -> stage.removeEventFilter( eventType, fxEventHandler ) );
		}

		fxEventHandler = null;

		logger.info( format( "Uninstalled Stage FX Listeners: type=[%s].", eventType ) );
	}

	public void uninstallDOMEventNotifications( String eventTypes )
	{
		if ( domEventHandler != null )
		{
			domEventUtils.removeListener( domEventHandler );
		}

		logger.info( format( "Uninstalled DOM Event Listener: types=[%s].", eventTypes ) );

	}

	public void installDOMEventNotifications( String eventTypes )
	{
		if ( (eventTypes == null) || eventTypes.isEmpty() )
		{
			eventTypes = Backend.STAR;
		}

		if ( Backend.STAR.equals( eventTypes.trim() ) )
		{
			domEventUtils.setDefaultAllowedDOMEvents();
		}
		else
		{
			domEventUtils.setAllowedDOMEvents( new HashSet<>( Arrays.asList( eventTypes.split( "\\s*,\\s*" ) ) ) );
		}

		if ( domEventHandler == null )
		{
			domEventHandler = domEventUtils.getHandler( event -> {
				if ( domEventUtils.canIgnore( event ) )
				{
					return;
				}

				String eventType = event.getType();
				String params = domEventUtils.getParams( event );
				Object target = event.getTarget();

				// copy values
				Notification n = buildNotification(
						"dom-event",
						eventType,
						notificationSequenceNumber.incrementAndGet(),
						params,
						target );

				if ( n != null )
				{
					executor.submit( () -> {
						jmxNotifier.sendNotification( n );

						if ( logNotifications )
						{
							logger.debug( format( "Notification: seq=[%s], time=[%s], type=[%s], message=[%s].",
									n.getSequenceNumber(),
									n.getTimeStamp(),
									n.getType(),
									n.getMessage()
							) );
						}
					} );
				}
			} );
		}

		domEventUtils.addListener( domEventHandler );

		logger.info( format( "Re-installed DOM Event Listener: types=%s.", domEventUtils.getAllowedDOMEvents() ) );
	}

	public boolean isLogNotifications()
	{
		return logNotifications;
	}

	public void setLogNotifications( boolean logNotifications )
	{
		this.logNotifications = logNotifications;

		logger.info( format( "notification logging is %s.", this.logNotifications ? "on" : "off" ) );
	}

}
