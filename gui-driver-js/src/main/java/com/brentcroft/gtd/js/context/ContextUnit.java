package com.brentcroft.gtd.js.context;


import com.brentcroft.gtd.driver.client.GuiSession;
import com.brentcroft.gtd.js.context.model.ModelBuilder;
import com.brentcroft.gtd.js.driver.JSGuiSession;
import com.brentcroft.util.FileUtils;
import com.brentcroft.util.CommentedProperties;
import com.brentcroft.util.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.xml.parsers.SAXParserFactory;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.apache.log4j.Logger;

import static com.brentcroft.util.XmlUtils.parse;

/**
 * Created by Alaric on 17/03/2017.
 */
public class ContextUnit
{
    private final static Logger logger = Logger.getLogger( ContextUnit.class );

    private static ScriptEngine engine = null;

    private File root;
    private Modeller modeller = null;
    private final Map< String, JSGuiSession > sessions = new LinkedHashMap<>();
    private CommentedProperties properties = new CommentedProperties();

    public ContextUnit()
    {
        this.engine = new ScriptEngineManager()
                .getEngineByName( "js" );
    }

    public ContextUnit( File configFile ) throws FileNotFoundException
    {
        this(
                configFile.getParentFile(),
                new FileInputStream( configFile ) );
    }

    public ContextUnit( File root, InputStream config )
    {
        this();

        this.root = root;

        try
        {
            SAXParserFactory
                    .newInstance()
                    .newSAXParser()
                    .parse(
                            config,
                            new ContextUnitBuilder( this ) );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
        }
    }

    public File getRoot()
    {
        return root == null
                ? new File( "." )
                : root;
    }


    public void setModeller( Modeller modeller )
    {
        this.modeller = modeller;
    }

    public Modeller getModeller()
    {
        return modeller;
    }

    public void addSession( String sessionKey, JSGuiSession session )
    {
        sessions.put( sessionKey, session );
    }

    public CommentedProperties getProperties()
    {
        return properties;
    }

    public Collection< JSGuiSession > getSessions()
    {
        return sessions.values();
    }

    public JSGuiSession getSession( String key )
    {
        return sessions.get( key );
    }

    public Context newContext()
    {
        return new Context( properties )
        {
            private final ScriptContext scriptContext = new SimpleScriptContext();

            {
                scriptContext.setBindings( engine.createBindings(), ScriptContext.GLOBAL_SCOPE );
                scriptContext.setBindings( engine.createBindings(), ScriptContext.ENGINE_SCOPE );

                // provide myself in scriptContext!
                getBindings()
                        .put( "context", this );

                if ( modeller != null && modeller.getAdapterBase() != null )
                {
                    // rebuild the base
                    execute( modeller.getAdapterBase() );
                }

                // and the adapter from each session
                for ( JSGuiSession session : getSessions() )
                {
                    refreshSessionModel( session );
                }
            }

            public Bindings getBindings()
            {
                return scriptContext.getBindings( ScriptContext.ENGINE_SCOPE );
            }

            public Bindings getBindingsGlobal()
            {
                return scriptContext.getBindings( ScriptContext.GLOBAL_SCOPE );
            }

            @Override
            public void refreshSessionModel( JSGuiSession session )
            {
                // TODO: Remove this coupling!
                //
                // The adapter script needs to know which session is in focus
                // in order to assign the driver to each of the adapter's objects.
                //
                // See: resources/js/template.adapter.js
                //
                //      the anonymous container function is invoked
                //      with the provided session as the only argument
                //
                Optional
                        .ofNullable( session.getModel() )
                        .ifPresent( ( model ) -> {
                            getBindings().put( "session", session );
                            getBindings().put( model.getName(), execute( getModelScript( session ) ) );
                        } );
            }


            @Override
            public ScriptObjectMirror generateModel( JSGuiSession session, String modelJson )
            {
                getBindings().put( "session", session );

                return ( ScriptObjectMirror ) execute( modeller.adaptJsonToModelScript( modelJson ) );
            }


            @Override
            public void addSession( String key, JSGuiSession session )
            {
                ContextUnit.this.sessions.put( key, session );

                refreshSessionModel( session );
            }

            @Override
            public Collection< JSGuiSession > getSessions()
            {
                return ContextUnit.this.getSessions();
            }

            @Override
            public JSGuiSession getSession( String key )
            {
                return ContextUnit.this.getSession( key );
            }

            @Override
            public Modeller getModeller()
            {
                return ContextUnit.this.getModeller();
            }

            @Override
            public Object execute( String script )
            {
                try
                {
                    return engine.eval( script, scriptContext );
                }
                catch ( ScriptException e )
                {
                    throw new RuntimeException( e );
                }
            }

            public void activateModels()
            {
                getSessions()
                        .stream()
                        .filter( session -> session.getState() == GuiSession.State.STARTED )
                        .forEach( session ->
                        {
                            if ( session.getModel() != null )
                            {
                                // looking up from the scriptContext bindings
                                ScriptObjectMirror som = ( ScriptObjectMirror ) getBindings()
                                        .get(
                                                session
                                                        .getModel()
                                                        .getName() );

                                if ( som != null )
                                {
                                    try
                                    {
                                        ModelBuilder.activateObject(
                                                som,
                                                parse( session
                                                        .getDriver()
                                                        .getSnapshotXmlText() ),
                                                true
                                        );

                                        if ( logger.isDebugEnabled() )
                                        {
                                            logger.debug(
                                                    String.format( "Activated session [%s] adapter [%s].",
                                                            session.getName(),
                                                            session
                                                                    .getModel()
                                                                    .getName() ) );
                                        }
                                    }
                                    catch ( Exception e )
                                    {
                                        logger.warn(
                                                String.format( "Failed to activate session [%s] adapter [%s]: %s",
                                                        session.getName(),
                                                        session
                                                                .getModel()
                                                                .getName(),
                                                        e ) );
                                    }
                                }
                            }
                        } );
            }

            public File getRoot()
            {
                return ContextUnit.this.getRoot();
            }
        };
    }

    /**
     * Obtains the adapter XML text from the session
     * and use the modeller to expand it to javascript
     * providing a map of translations to the modeller
     * using the parameter key "tx".
     *
     * @param session
     * @return
     */
    public String getModelScript( JSGuiSession session )
    {
        Map< String, Object > parameters = new HashMap<>();

        parameters.put( "context", this );

        parameters.put( "tx", TextUtils.getTranslations() );

        // translations
        // push adapter name
        parameters.put( "adapter-name", session.getModel().getName() );


        // the initial adapter uri must be re-relativized
        // from the scriptContext root (i.e. the config file)
        // to the jvm startup root
        final File modelFile = FileUtils
                .resolvePath(
                        getRoot(),
                        session
                                .getModel()
                                .getXmlUri() );

        final String modelUri = FileUtils
                .relativizePath(
                        modelFile );

        return modeller
                .expandModel(
                        session
                                .getModel()
                                .getXml(),
                        modelUri,
                        parameters
                );
    }

    public String toString()
    {
        StringBuffer b = new StringBuffer();

        if ( root != null )
        {
            b
                    .append( "root: " )
                    .append( root )
                    .append( "\n" );
        }

        if ( modeller != null )
        {
            b
                    .append( "modeller: \n" )
                    .append( TextUtils.indent( modeller.toString(), "  " ) )
                    .append( "\n" );
        }

        for ( Map.Entry< String, JSGuiSession > entry : sessions.entrySet() )
        {
            b
                    .append( "session: [" )
                    .append( entry.getKey() )
                    .append( "]\n" );
            b
                    .append( TextUtils.indent( entry.getValue().toString(), "  " ) )
                    .append( "\n" );
        }

        return b.toString();
    }


    public void stopAllSessions()
    {
        for ( JSGuiSession s : sessions.values() )
        {
            s.stop();
        }
    }
}

