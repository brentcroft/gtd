package com.brentcroft.gtd.adapter.model;

import static com.brentcroft.util.StringUpcaster.upcast;
import static java.lang.String.format;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;

public class SnapshotGuiObjectConsultant implements GuiObjectConsultant< SnapshotGuiObject >
{
	// reference the interface
	protected final static transient Logger logger = Logger.getLogger( GuiObjectConsultant.class );

	public final static String name = "Snapshot";

	public static final String START_SEQUENCE = "camera.%s.sequence.start";
	public static final String MODEL_NAME = "camera.%s.model.name";
	public static final String TIMESTAMP_FORMAT = "camera.%s.timestamp.format";

	private long seq = 0;
	private String modelName = "snapshot";
	private String timestampFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS";

	public SnapshotGuiObjectConsultant( Properties properties )
	{
		configure( properties, name );
	}

	@Override
	public void configure( Properties properties, String name )
	{
		seq = upcast( properties.getProperty( format( START_SEQUENCE, name ) ), seq );
		modelName = upcast( properties.getProperty( format( MODEL_NAME, name ) ), modelName );
		timestampFormat = upcast( properties.getProperty( format( TIMESTAMP_FORMAT, name ) ), timestampFormat );

		logger.debug( format(
				"Configured consultant for [%s]:%n start-seq=%s%n model-name=%s%n timestamp-format=%s",
				name,
				seq,
				modelName,
				timestampFormat ) );
	}

	@Override
	public boolean ignore( GuiObject< SnapshotGuiObject > t )
	{
		return false;
	}

	@Override
	public boolean ignoreAttribute( String name )
	{
		return false;
	}

	@Override
	public void extendElement( GuiObject< SnapshotGuiObject > t, Element element )
	{
		if ( modelName != null )
		{
			element.getOwnerDocument().renameNode( element, null, modelName );
		}

		element.setAttribute( "seq", "" + (seq++) );
		element.setAttribute(
				"timestamp",
				DateTimeFormatter
						.ofPattern( timestampFormat )
						.format( LocalDateTime.now() ) );
	}
}
