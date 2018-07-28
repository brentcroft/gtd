package com.brentcroft.gtd.js.context;

import com.brentcroft.gtd.driver.client.GuiAdapter;
import com.brentcroft.gtd.driver.client.GuiLauncher;
import com.brentcroft.gtd.js.driver.JSGuiLocalDriver;
import com.brentcroft.gtd.js.driver.JSGuiSession;
import com.brentcroft.util.CommentedProperties;
import com.brentcroft.util.TextUtils;
import com.brentcroft.util.templates.JstlTemplateManager;
import java.io.FileReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.stream.Collectors;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import static com.brentcroft.util.FileUtils.resolvePath;
import static com.brentcroft.util.StringUpcaster.upcast;
import static java.lang.String.format;

//import com.brentcroft.gtd.driver.GuiController;

/**
 * Created by Alaric on 18/03/2017.
 */
public class ContextUnitBuilder extends DefaultHandler
{
    private ContextUnit unit;

    // there is no external map of objects
    // rather, propertiesStack in context are made available
    private JstlTemplateManager jstl = new JstlTemplateManager()
            .withStripComments( true );

    boolean inContext = false;
    boolean inJvmOptions = false;
    boolean inClassPath = false;
    boolean inCucumber = false;
    boolean inProperties = false;

    private JSGuiSession session;

    private StringBuilder characters = null;
    private String propertyEntryKey = null;

    private final Stack< CommentedProperties > propertiesStack = new Stack<>();

    private GuiLauncher launcher;
    private GuiAdapter guiAdapter;
    private Model model;

    public ContextUnitBuilder( ContextUnit unit )
    {
        this.unit = unit;
    }

    private String expand( Attributes attributes, String name)
    {
    	return expand( attributes.getValue( name ) );    	
    }
    

    private String expand( String value )
    {
        if ( propertiesStack.empty() || value == null )
        {
            return value;
        }

        Properties p = propertiesStack.peek();

        // this will also check defaults
        final Map< String, Object > m = p
                .stringPropertyNames()
                .stream()
                .collect( Collectors.toMap(
                        ( k ) ->
                        {
                            return k;
                        },
                        p::getProperty
                ) );

        String relUri = null;

        try
        {
            relUri = unit
                    .getRoot()
                    .toURI()
                    .toURL()
                    .toExternalForm();
        }
        catch ( MalformedURLException ignored )
        {
        }

        return jstl.expandText( value, relUri, m );
    }

    private String trimAllWhitespace( String text )
    {
        return text == null
                ? null
                : text
                        .replaceAll( "\\s+", " " )
                        .trim();
    }


    interface ContextValue
    {
        String name();

        String defaultValue();

        default String getValue( Attributes attributes )
        {
            String candidate = attributes.getValue( name() );
            return ( candidate == null || candidate.isEmpty() )
                    ? defaultValue()
                    : candidate;
        }
    }

    enum I18nAttr
    {
        languageTag,
        bundle;
    }

    enum AttrModeller implements ContextValue
    {
        xsltUri( "xslt/generate-model.xslt" ),
        adapterBase( "js/template.model-base.js" ),
        adapter( "js/template.model.js" );

        final String path;

        AttrModeller( String path )
        {
            this.path = path;
        }

        public String defaultValue()
        {
            return path;
        }
    }


    public void startElement( String uri, String localName, String qName, Attributes attributes ) throws SAXException
    {
        String key = qName;

        try
        {
            if ( "context".equalsIgnoreCase( key ) )
            {
                inContext = true;

                // unit
                propertiesStack.push( unit.getProperties() );


                // create default modeller now
                // may get overwritten
                unit
                        .setModeller(
                                new Modeller(
                                        unit.getRoot(),
                                        expand( AttrModeller.xsltUri.defaultValue() ),
                                        expand( AttrModeller.adapterBase.defaultValue() ),
                                        expand( AttrModeller.adapter.defaultValue() )
                                ) );


            }
            else if ( "i18n".equalsIgnoreCase( key ) )
            {
                final String i18nTag = expand( attributes, "languageTag" );
                final String i18nBundle = expand( attributes, "bundle" );

                if ( i18nBundle != null )
                {
                    TextUtils
                            .setBundle(
                                    expand( i18nBundle ),
                                    expand( i18nTag ) );
                }
            }
            else if ( "modeller".equalsIgnoreCase( key ) )
            {
                unit
                        .setModeller(
                                new Modeller(
                                        unit.getRoot(),
                                        expand( AttrModeller.xsltUri.getValue( attributes ) ),
                                        expand( AttrModeller.adapterBase.getValue( attributes ) ),
                                        expand( AttrModeller.adapter.getValue( attributes ) )
                                ) );
            }
            else if ( "session".equalsIgnoreCase( key ) )
            {
                if ( session != null )
                {
                    throw new SAXException( format( "Sequence Error: element [%s]; current session already exists.", key ) );
                }

                // assign session in scope
                session = new JSGuiSession( expand( attributes, "name" ) );

                // compose the session
                session.setDriver( new JSGuiLocalDriver() );
                session.setLauncher( new GuiLauncher( session.getDriver() ) );


                String mBeanRef = expand( attributes, "mbean-ref" );

                if ( mBeanRef == null || mBeanRef.isEmpty() )
                {
                    throw new SAXException( format( "Missing mandatory attribute [%s]: element [%s].", "mbean-ref", key ) );
                }


                session.getDriver().setMBeanRef( mBeanRef );


                // each session has a working directory
                // to load its files from
                String wd = expand( attributes, "dir" );

                session.setWorkingDirectory(
                        wd == null || wd.isEmpty()
                                ? unit.getRoot()
                                : resolvePath( unit.getRoot(), wd )
                );

                // new propertiesStack but inherits from unit
                propertiesStack.push( new CommentedProperties( unit.getProperties() ) );

                session.setProperties( propertiesStack.peek() );

                String onStartedScript = expand( attributes,  "onStarted" );

                if ( onStartedScript != null && ! onStartedScript.isEmpty() )
                {
                    session.setOnStarted( onStartedScript );
                }

                // maybe validate session
                unit.addSession( session.getName(), session );
            }
            else if ( "properties".equalsIgnoreCase( key ) )
            {
                inProperties = true;

                final String propertiesSrc = expand( attributes, "src" );

                if ( propertiesSrc != null && ! propertiesSrc.isEmpty() )
                {
                    try ( Reader reader = new FileReader(
                            resolvePath(
                                    unit.getRoot(),
                                    propertiesSrc ) ) )
                    {
                        CommentedProperties p = propertiesStack
                                .peek();

                        p.load( reader );

                        p.setProperty( "$src", propertiesSrc );
                    }
                }
            }
            else if ( "entry".equalsIgnoreCase( key ) )
            {
                propertyEntryKey = expand( attributes, "key" );

                characters = new StringBuilder();
            }
            else if ( "login".equalsIgnoreCase( key ) )
            {
                if ( session == null )
                {
                    throw new SAXException( format( "Sequence Error: element [%s]; no current session.", key ) );
                }

                // login timeout is stored in session
                session.setLoginTimeoutSeconds(
                        upcast(
                                expand( attributes, "timeout" ),
                                session.getLoginTimeoutSeconds() )
                );

                // echo timeout is stored in session
                session.setEchoTimeoutSeconds(
                        upcast(
                                expand( attributes, "echo-timeout" ),
                                session.getEchoTimeoutSeconds() )
                );


                String guiAdapterClass = expand( attributes, "adapter" );

                if ( guiAdapterClass != null )
                {
                    try
                    {
                        guiAdapter = ( GuiAdapter ) Class.forName( guiAdapterClass ).newInstance();

                        guiAdapter.setDriver( session.getDriver() );

                        session.setGuiAdapter( guiAdapter );

                        // create a new propertiesStack node
                        propertiesStack.push( new CommentedProperties( session.getProperties() ) );

                        // and set as new credentials on adapter
                        guiAdapter.newCredentials( propertiesStack.peek() );
                    }
                    catch ( Exception e )
                    {
                        throw new RuntimeException(
                                format( "Failed to install GuiAdapter [%s].", guiAdapterClass ), e );
                    }
                }
            }
            else if ( "driver".equalsIgnoreCase( key ) )
            {
                if ( session == null )
                {
                    throw new SAXException( format( "Sequence Error: element [%s]; no current session.", key ) );
                }

                // configure existing driver
                JSGuiLocalDriver driver = session.getDriver();

                driver.setJmxRmiUrl(
                        upcast(
                                expand( attributes, "rmxUri" ),
                                "service:jmx:rmi:///jndi/rmi://:9999/jmxrmi" )
                );

                driver.setDefaultPollDelaySeconds(
                        upcast(
                                expand( attributes, "pollDelay" ),
                                1.0 )
                );

                driver.setDefaultTimeout(
                        upcast(
                                expand( attributes, "timeout" ),
                                3.0 )
                );

                driver.setDefaultRelaxSeconds(
                        upcast(
                                expand( attributes, "relax" ),
                                0.5 ) );
            }
            else if ( "adapter".equalsIgnoreCase( key ) || "model".equalsIgnoreCase( key ) )
            {
                if ( session == null )
                {
                    throw new SAXException( format( "Sequence Error: element [%s]; no current session.", key ) );
                }

                // assign adapter in scope
                model = new Model(
                        session.getWorkingDirectory(),
                        expand( attributes, "name" ),
                        expand( attributes, "xml" ),
                        expand( attributes, "json" )
                );

                session.setModel( model );

                // configures SessionGuiObjectConsultant to set name of root element
                // this will overwrite any existing value
                session.getProperties().put( "camera.Snapshot.model.name", expand( attributes, "name" ) );


                // optional modelling attributes

                String xml2jsonModelUri = expand( attributes, "xml2json" );

                if ( xml2jsonModelUri != null && ! xml2jsonModelUri.isEmpty() )
                {
                    model.withXsl2JsonModelUri( xml2jsonModelUri );
                }

                String preReducerUri = expand( attributes, "pre-reducer" );

                if ( preReducerUri != null && ! preReducerUri.isEmpty() )
                {
                    model.withXslPreReducerUri( preReducerUri );
                }

                String reducerUri = expand( attributes, "reducer" );

                if ( reducerUri != null && ! reducerUri.isEmpty() )
                {
                    model.withXslReducerUri( reducerUri );
                }

                propertiesStack.push( new CommentedProperties( session.getProperties() ) );

                model.setProperties( propertiesStack.peek() );
            }
            else if ( "launcher".equalsIgnoreCase( key ) )
            {
                if ( session == null )
                {
                    throw new SAXException( format( "Sequence Error: element [%s]; no current session.", key ) );
                }

                // assign launcher in scope
                launcher = session.getLauncher();

                if ( launcher == null )
                {
                    throw new SAXException( format( "Sequence Error: element [%s]; no session launcher.", key ) );
                }

                // where an application is launched from
                String wd = expand( attributes, "dir" );

                launcher.setWorkingDirectory(
                        wd == null || wd.isEmpty()
                                ? session.getWorkingDirectory()
                                : resolvePath( session.getWorkingDirectory(), wd )
                );


                String fechoTS = expand( attributes, "first-echo-timeout" );

                if ( fechoTS != null && ! fechoTS.isEmpty() )
                {
                    launcher.setFirstEchoTimeout(
                            upcast( fechoTS, 1.0 ) );
                }

                launcher.setJavaCommand( expand( attributes, "command" ) );
            }
            else if ( "javaws".equalsIgnoreCase( key ) )
            {
                if ( launcher == null )
                {
                    throw new SAXException( format( "Sequence Error: element [%s]; no current launcher.", key ) );
                }

                launcher.setApplicationUri(
                        expand( attributes, "uri" ) );
            }
            else if ( "java".equalsIgnoreCase( key ) )
            {
                if ( launcher == null )
                {
                    throw new SAXException( format( "Sequence Error: element [%s]; no current launcher.", key ) );
                }


                launcher.setAddShutdownHook(
                        upcast(
                                expand( attributes, "shutdownHook" ),
                                false ) );
            }
            else if ( "classpath".equalsIgnoreCase( key ) )
            {
                launcher.setJavaClassPathRoot(
                        expand( attributes, "root" ) );

                characters = new StringBuilder();

                inClassPath = true;
            }
            else if ( "vmOptions".equalsIgnoreCase( key ) )
            {
                characters = new StringBuilder();

                inJvmOptions = true;
            }
            else if ( "cucumber".equalsIgnoreCase( key ) )
            {
                characters = new StringBuilder();

                inCucumber = true;
            }
            else if ( "application".equalsIgnoreCase( key ) )
            {
                if ( launcher == null )
                {
                    throw new SAXException( format( "Sequence Error: element [%s]; no current launcher.", key ) );
                }

                launcher.setApplicationMainClass( expand( attributes, "main" ) );
                launcher.setApplicationServiceClass(
                        upcast(
                                expand( attributes, "service" ),
                                "com.brentcroft.gtd.handler.HandlerBasedGuiObjectService" )
                );
                launcher.setApplicationNotifyAWTMask(
                        upcast( expand( attributes, "notify" ),
                                664 )
                );

                launcher.setApplicationHashCache(
                        upcast( expand( attributes, "hashCache" ),
                                0 )
                );

                launcher.setApplicationNotifySnapshotDelay(
                        upcast( expand( attributes, "snapshotDelay" ),
                                2.0 )
                );
            }
        }
        catch ( Exception e )
        {
            throw new SAXException( format( "Error processing element [%s].", key ), e );
        }
    }

    public final static String CUCUMBER_GLUE_KEY = "cucumber.glue";

    public void characters( char ch[], int start, int length ) throws SAXException
    {
        if ( characters != null )
        {
            characters.append( ch, start, length );
        }
    }

    public void endElement( String uri, String localName, String qName ) throws SAXException
    {
        String key = qName;

        if ( "context".equalsIgnoreCase( key ) )
        {
            inContext = false;

            propertiesStack.pop();
        }
        else if ( "session".equalsIgnoreCase( key ) )
        {
            session = null;
            propertiesStack.pop();
        }
        else if ( "application".equalsIgnoreCase( key ) )
        {
        }
        else if ( "login".equalsIgnoreCase( key ) )
        {
            if ( guiAdapter != null )
            {
                propertiesStack.pop();
            }
        }
        else if ( "propertiesStack".equalsIgnoreCase( key ) )
        {
            inProperties = false;
        }
        else if ( "entry".equalsIgnoreCase( key ) )
        {
            if ( propertiesStack.empty() )
            {
                throw new SAXException( format( "Sequence Error: element [%s]; no propertiesStack.", key ) );
            }
            if ( propertyEntryKey == null )
            {
                throw new SAXException( format( "Sequence Error: element [%s]; no propertyEntryKey.", key ) );
            }

            propertiesStack
                    .peek()
                    .put(
                            propertyEntryKey,
                            trimAllWhitespace( expand( characters.toString() ) ) );

            characters = null;
        }
        else if ( "adapter".equalsIgnoreCase( key ) )
        {
            model = null;
            propertiesStack.pop();
        }
        else if ( "launcher".equalsIgnoreCase( key ) )
        {
            launcher = null;
        }
        else if ( "vmOptions".equalsIgnoreCase( key ) )
        {
            launcher.setJavaVmOptions( trimAllWhitespace( expand( characters.toString() ) ) );

            characters = null;
            inJvmOptions = false;
        }
        else if ( "cucumber".equalsIgnoreCase( key ) )
        {
            String newGlue = trimAllWhitespace( characters.toString() );

            // append as comma separated list
            Properties p = unit.getProperties();

            String o = p.getProperty( CUCUMBER_GLUE_KEY );
            if ( o == null )
            {
                o = newGlue;
            }
            else
            {
                o = o + ", " + newGlue;
            }

            p.put( CUCUMBER_GLUE_KEY, expand( o ) );

            characters = null;
            inCucumber = false;
        }
        else if ( "classpath".equalsIgnoreCase( key ) )
        {
            if ( launcher == null )
            {
                throw new SAXException( format( "Sequence Error: element [%s]; no current launcher.", key ) );
            }

            launcher.setJavaClassPath( trimAllWhitespace( expand( characters.toString() ) ) );

            characters = null;
            inClassPath = false;
        }
    }
}
