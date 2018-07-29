package com.brentcroft.gtd.adapter.utils;

import static java.lang.String.format;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.brentcroft.gtd.driver.Backend;
import com.brentcroft.util.buffer.AsynchBuffer;

/**
 * Created by Alaric on 27/12/2016.
 */
public class DocumentNotificationBuffer
{
	private static final Logger logger = Logger.getLogger( DocumentNotificationBuffer.class );

	public interface DocumentListener
	{
		void receive( String source, Document document );
	}

	private static Set< DocumentListener > listeners = null;

	public static void shutdownNow()
	{
		if ( listeners != null )
		{
			listeners.clear();
		}
	}

	public static void setDelay( double delaySeconds )
	{
		documentBuffer.withDelaySeconds( delaySeconds );
	}

	private static AsynchBuffer< Document > documentBuffer = new AsynchBuffer< Document >( "Snapshot Buffer" )
	{
		@Override
		public void process( Document document )
		{
			if ( listeners != null )
			{
				for ( DocumentListener l : listeners )
				{
					if ( l != null )
					{
						l.receive( "locator", document );
					}
				}
			}
		}
	}
			.withDelaySeconds( 0.5 )
			.withLastInFirstOut( true )
			.withMaxEntries( 1 );

	public static void removeDocumentListener( DocumentListener l )
	{
		if ( listeners != null )
		{
			listeners.remove( l );
		}
	}

	public static void addDocumentListener( DocumentListener l )
	{
		if ( listeners == null )
		{
			listeners = new HashSet< DocumentListener >();
		}
		listeners.add( l );
	}

	public static void notifyDocument( Document document )
	{
		if ( (document != null) && (document.getDocumentElement() != null) )
		{
			Element de = document.getDocumentElement();

			boolean newHash = Boolean.valueOf( de.getAttribute( Backend.HASH_NEW_ATTRIBUTE ) );

			if ( logger.isDebugEnabled() )
			{
				logger.debug( format( "Snapshot: duration=[%s], new=[%s], timestamp=[%s].",
						de.getAttribute( "duration" ),
						newHash,
						de.getAttribute( "timestamp" )
				) );
			}

			// TODO: eh???
			// this idea to progressively remove all leaf nodes that are not new
			// and only notify what remains
//			if ( newHash && false )
//			{
//				Document notifyDoc = ( Document ) document.cloneNode( true );
//
//				// erode document
//				XmlUtils.erode( notifyDoc.getDocumentElement(), new XmlUtils.ElementFilter()
//				{
//					@Override
//					public boolean accept( Element element )
//					{
//						return Boolean.valueOf( element.getAttribute( GuiObject.HASH_NEW_ATTRIBUTE )
//						);
//					}
//				} );
//
//				if ( logger.isDebugEnabled() )
//				{
//					logger.debug( format( "Eroded snapshot." ) );
//				}
//
//				documentBuffer.add( notifyDoc );
//			}
		}
	}
}
