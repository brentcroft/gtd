package com.brentcroft.gtd.camera;

import static java.lang.String.format;

import java.io.StringReader;
import java.util.Map;
import java.util.Properties;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.brentcroft.gtd.adapter.model.GuiObject;
import com.brentcroft.gtd.adapter.model.GuiObjectVisitor;
import com.brentcroft.gtd.driver.GuiControllerMBean;
import com.brentcroft.gtd.driver.GuiObjectLocator;
import com.brentcroft.gtd.driver.GuiObjectService;
import com.brentcroft.util.XmlUtils;

/**
 * Created by Alaric on 14/07/2017.
 */
public class Camera implements GuiObjectLocator< GuiObject< ? > >, GuiObjectService< GuiObject< ? > >
{
	private final CameraObjectService service;

	public Camera()
	{
		service = createService();
		service.install( new Properties() );
	}

	protected CameraObjectService createService()
	{
		return new CameraObjectService();
	}

	@Override
	public void shutdown()
	{
	}

	@Override
	public Object configure( String script, GuiControllerMBean driver )
	{
		try
		{
			ScriptEngine engine = new ScriptEngineManager().getEngineByName( "js" );

			Bindings b = engine.createBindings();

			b.put( "camera", this );
			b.put( "driver", driver );
			b.put( "service", service );

			return "" + engine.eval( new StringReader( script ), b );
		}
		catch ( ScriptException e )
		{
			throw new RuntimeException( format( "ScriptException file=[%s], line=[%s,%s]: %s",
					e.getFileName(),
					e.getLineNumber(),
					e.getColumnNumber(),
					e.getMessage() ) );
		}
	}

	@Override
	public void setProperties( Properties properties )
	{
		service.install( properties );
	}

	@Override
	public GuiObjectLocator< GuiObject< ? > > getGuiObjectLocator()
	{
		return this;
	}

	@Override
	public CameraObjectManager getObjectManager()
	{
		return service.getManager();
	}

	@Override
	public String getReport()
	{
		return service.getManager().toString();
	}

	@Override
	public CameraControllerMBean getController()
	{
		return new CameraController( this );
	}

	public Object getOrigin()
	{
		return new Snapshot();
	}

	@Override
	public GuiObject<?> getGob( Object gobee )
	{
		return service
				.getManager()
				.adapt(
						gobee == null ? getOrigin() : gobee,
						null );
	}

	public void snapshot( Node node, Map< String, Object > options )
	{
		snapshot( null, node, options );
	}

	public void snapshot( Object origin, Node node, Map< String, Object > options )
	{
		getGob( origin ).accept( new GuiObjectVisitor( node, options ) );
	}

	@Override
	public Document takeSnapshot()
	{
		return takeSnapshot( null );
	}

	@Override
	public synchronized Document takeSnapshot( Map< String, Object > options )
	{
		return takeSnapshot( null, options );
	}

	@Override
	public void takeSnapshot( Object origin, Node node, Map< String, Object > options )
	{
		snapshot( origin, node, options );
	}

	public synchronized Document takeSnapshot( Object origin, Map< String, Object > options )
	{
		Document document = XmlUtils.newDocument();

		long started = System.currentTimeMillis();

		snapshot( origin, document, options );

		long finished = System.currentTimeMillis();

		long duration = (finished - started);

		Element element = document.getDocumentElement();

		if ( element != null )
		{
			element.setAttribute( "duration", "" + duration );
		}

		return document;
	}

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();

		b.append( getClass().getSimpleName() );

		b.append( getObjectManager() );

		return b.toString();
	}
}