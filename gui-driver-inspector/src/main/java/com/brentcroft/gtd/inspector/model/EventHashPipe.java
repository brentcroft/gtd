package com.brentcroft.gtd.inspector.model;

import static java.lang.String.format;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.brentcroft.gtd.driver.client.GuiSession;
import com.brentcroft.util.Pipes;
import com.brentcroft.util.StringUpcaster;
import com.brentcroft.util.XPathUtils;
import com.brentcroft.util.XmlUtils;

/**
 * Created by Alaric on 30/12/2016.
 */
public class EventHashPipe extends Pipes.Pipe< Notification, EventRecord >
{
	private final static Logger logger = Logger.getLogger( EventHashPipe.class );

	private final static String HASH_PATH = "//*/*[ @hash ][ 1 ]/@hash";

	private Long[] masks = {

			// key
			400l, // typed
			401l, // pressed
			402l, // released

			// mouse
			500l, // clicked
			501l, // pressed
			502l // released

	};

	// I want to evict events with duplicate seq attributes
	private final int numToRemember = 10;

	private final LinkedHashMap< Long, String > seqDupeEvictor = new LinkedHashMap< Long, String >()
	{
		private static final long serialVersionUID = 1226616915481179113L;

		@Override
		protected boolean removeEldestEntry( Map.Entry< Long, String > eldest )
		{
			return this.size() > numToRemember;
		}
	};

	// I want to adapt NotificationListener to feed into my receive port
	private final NotificationListener notificationListener = new NotificationListener()
	{
		@Override
		public void handleNotification( Notification notification, Object handback )
		{
			EventHashPipe.this.receive( notification );
		}
	};

	private Pipes.Processor< Notification, EventRecord > processor = new Pipes.Processor< Notification, EventRecord >()
	{
		@Override
		public EventRecord process( Notification notification )
		{
			// dupes happen at the same time!
			synchronized ( seqDupeEvictor )
			{
				if ( seqDupeEvictor
						.containsKey(
								notification
										.getSequenceNumber() ) )
				{
					// evicting duplicate
					return null;
				}

				seqDupeEvictor.put(
						notification.getSequenceNumber(),
						"archetype" );
			}

			String message = notification.getMessage();

			try
			{
				final Document document = XmlUtils.parse( message );

				Element de = document.getDocumentElement();

				long seq = Long.valueOf( de.getAttribute( "seq" ) );

				String param = de.getAttribute( "param" );

				String eventId = de.getAttribute( "id" );

				// long eventId = Long.valueOf( de.getAttribute( "id" ) );
				//
				// if ( ! matches( eventId ) )
				// {
				// if ( logger.isTraceEnabled() )
				// {
				// logger.trace( format( "Rejected event: event-id=[%s]; seq=[%s], param=[%s].",
				// eventId, seq, param ) );
				// }
				//
				// return null;
				// }

				final String targetHash = XPathUtils
						.getCompiledPath( HASH_PATH )
						.evaluate( document );

				if ( (targetHash == null) || targetHash.isEmpty() )
				{
					if ( logger.isTraceEnabled() )
					{
						logger.trace( format( "No hash on event: id=[%s]; seq=[%s], param=[%s].", eventId, seq, param ) );
					}

					return null;
				}

				return new EventRecord( seq, eventId, param, targetHash, message );
			}
			catch ( Exception e )
			{
				throw new RuntimeException( e );
			}
		}
	};

	/*
	 * install the pipe
	 */
	{
		withConditionIn( notification -> ((notification != null) && ("awt-event".equals( notification.getType() )
				|| "dom-event".equals( notification.getType() )
				|| "fx-event".equals( notification.getType() ))) );

		withProcessor( processor );

		withConditionOut( hash -> hash != null );

		withName( "Notification Event Parser" );

		// withLogMessages( true );
	}

	public void setSessionListener( GuiSession session, boolean attached )
	{
		if ( attached )
		{
			session
					.getDriver()
					.addNotificationListener( notificationListener );

			if ( logger.isDebugEnabled() )
			{
				logger.debug( format( "Attached session notification listener [%s].", notificationListener ) );
			}
		}
		else
		{
			session
					.getDriver()
					.removeNotificationListener( notificationListener );

			if ( logger.isDebugEnabled() )
			{
				logger.debug( format( "Detached session notification listener [%s].", notificationListener ) );
			}
		}
	}

	public void setEventIdFilterText( String eventIdFilterText )
	{
		if ( (eventIdFilterText == null) || eventIdFilterText.isEmpty() )
		{
			masks = null;
		}
		else
		{
			masks = StringUpcaster.upcast( eventIdFilterText.split( "\\s*,\\s*" ), Long.class );
		}
	}

	public String getEventIdFilterText()
	{
		StringBuilder b = new StringBuilder();

		if ( masks != null )
		{
			for ( long m : masks )
			{
				if ( b.length() > 0 )
				{
					b.append( "," );
				}
				b.append( m );
			}
		}

		return b.toString();
	}

	// TODO: stash the results of the upcast and update on ui action
	// private boolean matches( long eventId )
	// {
	// for ( Long mask : masks )
	// {
	// if ( mask == null )
	// {
	// continue;
	// }
	//
	// if ( mask == eventId )
	// {
	// return true;
	// }
	// }
	//
	// return false;
	// }
}
