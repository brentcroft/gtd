package com.brentcroft.util.tools;

import com.brentcroft.util.CommentedProperties;
import com.brentcroft.util.Configurator;
import com.brentcroft.util.TriConsumer;
import com.brentcroft.util.XmlUtils;
import com.brentcroft.util.templates.JstlTemplateManager;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.xml.transform.Templates;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import static com.brentcroft.util.StringUpcaster.downcastCollection;
import static com.brentcroft.util.StringUpcaster.upcast;
import static com.brentcroft.util.StringUpcaster.upcastSet;

/**
 * Wraps a template XSL stylesheet that has JSTL expressions referring back to an instance of this class.
 * <p/>
 * An instance of this class is configured by a properties file.
 * <p/>
 * The template is expanded and then the resultant XSL stylesheet is used to transform some given input Node.
 */
public class Reducer
{
    private static final Logger logger = Logger.getLogger( Reducer.class );

    private JstlTemplateManager jstl = new JstlTemplateManager();

    private String stylesheet;
    private Templates templates;
    private Map< String, Object > parameters = new HashMap<>();


    private String propertyPrefix;

    private boolean logNewXslt = false;
    private Integer cycles = 1;


    public Reducer( String propertyPrefix, Map< String, String > tx )
    {
        this.propertyPrefix = propertyPrefix;
        parameters.put( "reducer", this );
        parameters.put( "tx", tx );
    }


    private Set< String > include = new HashSet<>();
    private Set< String > exclude = new HashSet<>();
    private Set< String > elide = new HashSet<>();

    private String copyOfAttributes = "@*";

    private final static Set< String > NOTHING = Collections.EMPTY_SET;
    private final static Set< String > ANYTHING = new HashSet<>();
    private final static String STAR = "*";


    static
    {
        ANYTHING.add( STAR );
    }

    public Reducer withStylesheet( String stylesheet )
    {
        this.stylesheet = stylesheet;
        return this;
    }


    public Set< String > getInclude()
    {
        return include;
    }

    public Set< String > getExclude()
    {
        return exclude;
    }

    public Set< String > getElide()
    {
        return elide;
    }

    public String getCopyOfAttributes()
    {
        return copyOfAttributes;
    }


    private Templates getTemplates()
    {
        if ( templates == null )
        {
            String xslt = jstl.expandText( stylesheet, parameters );

            if ( logNewXslt )
            {
                logger.info( "Calculated new xslt: \n" + xslt );
            }

            try
            {
                templates = XmlUtils.newTemplates( new StringReader( xslt ), null );
            }
            catch ( Exception e )
            {
                throw e instanceof RuntimeException
                        ? ( RuntimeException ) e
                        : new RuntimeException( "Bad xslt: " + xslt, e );
            }
        }
        return templates;
    }

    public Node reduce( Node node )
    {
        AtomicReference< Node > ar = new AtomicReference<>( node );

        for ( int i = 0; i < cycles; i++ )
        {
            parameters.put( "cycle", i );
            ar.set( XmlUtils.transform( getTemplates(), ar.get(), parameters ) );
        }

//        IntStream
//                .range( 0, cycles )
//                .forEach( i -> ar.set( XmlUtils.transform( getTemplates(), ar.get(), parameters ) ) );

        return ar.get();
    }


    public Reducer configure( CommentedProperties p )
    {
        Arrays
                .asList( Property.values() )
                .forEach( property -> {
                    property.configure( this, p );
                } );

        templates = null;

        return this;
    }

    public Reducer export( CommentedProperties p )
    {
        Arrays
                .asList( Property.values() )
                .forEach( property -> {
                    property.export( this, p );
                } );
        return this;
    }

    private String expandAttributeName( String attribute )
    {
        return propertyPrefix + "." + attribute;
    }

    public enum Property implements Configurator< Reducer, CommentedProperties >
    {
        REDUCER_CYCLES(
                "cycles",
                "How many times to apply the transform [0..10].",
                ( attribute, reducer, p ) -> reducer.cycles = Math.min( 10, upcast( p.getProperty( reducer.expandAttributeName( attribute ) ), reducer.cycles ) ),
                ( attribute, reducer, p ) -> p.setProperty(
                        reducer.expandAttributeName( attribute ),
                        "" + reducer.cycles )
        ),

        REDUCER_LOG_XSL(
                "logXsl",
                "Whether to log the expanded reducer xslt text.",
                ( attribute, reducer, p ) -> reducer.logNewXslt = Boolean.valueOf( p.getProperty( reducer.expandAttributeName( attribute ) ) ),
                ( attribute, reducer, p ) -> p.setProperty(
                        reducer.expandAttributeName( attribute ),
                        "" + reducer.logNewXslt )
        ),

        REDUCER_INCLUDE(
                "include",
                "tag test (or expressions) that must be included, implicitly included ancestors.",
                ( attribute, reducer, p ) -> {
                    Set s = upcastSet( p.getProperty( reducer.expandAttributeName( attribute ) ) );
                    reducer.include.clear();
                    reducer.include.addAll( s.isEmpty() ? ANYTHING : s );
                },
                ( attribute, reducer, p ) -> p.setProperty(
                        reducer.expandAttributeName( attribute ),
                        downcastCollection( reducer.include ) )
        ),


        REDUCER_EXCLUDE(
                "exclude",
                "tag test (or expressions) that must be excluded, irrespective of included descendants.",
                ( attribute, reducer, p ) -> {
                    Set s = upcastSet( p.getProperty( reducer.expandAttributeName( attribute ) ) );
                    reducer.exclude.clear();
                    reducer.exclude.addAll( s.isEmpty() ? NOTHING : s );
                },
                ( attribute, reducer, p ) -> p.setProperty(
                        reducer.expandAttributeName( attribute ),
                        downcastCollection( reducer.exclude ) )
        ),

        REDUCER_ELIDE(
                "elide",
                "tag test (or expressions) that must be elided, irrespective of included descendants.",
                ( attribute, reducer, p ) -> {
                    Set s = upcastSet( p.getProperty( reducer.expandAttributeName( attribute ) ) );

                    reducer.elide.clear();
                    reducer.elide.addAll( s.isEmpty() ? NOTHING : s );
                },
                ( attribute, reducer, p ) -> p.setProperty(
                        reducer.expandAttributeName( attribute ),
                        downcastCollection( reducer.elide ) )
        ),

        REDUCER_COPY_OF_ATTRIBUTES(
                "copyOf",
                "select expression determining which attributes are copied to new elements.",
                ( attribute, reducer, p ) -> {
                    reducer.copyOfAttributes = upcast( p.getProperty( reducer.expandAttributeName( attribute ) ), "@*" );
                },
                ( attribute, reducer, p ) -> p.setProperty(
                        reducer.expandAttributeName( attribute ),
                        reducer.copyOfAttributes )
        ),
//        REDUCER_ELIDE_IF_ONLY_CHILD(
//                "elideIfOnlyChild",
//                "tag test (or expressions) that must be elided if the context node only has one child.",
//                ( attribute, reducer, p ) -> {
//                    Set s = upcastSet( p.getProperty( reducer.expandAttributeName( attribute ) ) );
//
//                    reducer.elideIfOnlyChild.clear();
//                    reducer.elideIfOnlyChild.addAll( s.isEmpty() ? NOTHING : s );
//                },
//                ( attribute, reducer, p ) -> p.setProperty(
//                        reducer.expandAttributeName( attribute ),
//                        downcastCollection( reducer.elideIfOnlyChild ) )
//        ),
//
//        REDUCER_ELIDE_IF_ONLY_ONE_CHILD(
//                "elideIfOnlyOneChild",
//                "tag test (or expressions) that must be elided if the context node only has one child."
//                ,
//                ( attribute, reducer, p ) -> {
//                    Set s = upcastSet( p.getProperty( reducer.expandAttributeName( attribute ) ) );
//
//                    reducer.elideIfOnlyOneChild.clear();
//                    reducer.elideIfOnlyOneChild.addAll( s.isEmpty() ? NOTHING : s );
//                },
//                ( attribute, reducer, p ) -> p.setProperty(
//                        reducer.expandAttributeName( attribute ),
//                        downcastCollection( reducer.elideIfOnlyOneChild ) )
//        )

        ;

        private final Configurator.PropertiesConfigurator< Reducer > configurator;

        Property( String attribute, String comment, TriConsumer< String, Reducer, CommentedProperties > config, TriConsumer< String, Reducer, CommentedProperties > exporter )
        {
            this.configurator = Configurator.create( attribute, comment, config, exporter );
        }

        public void configure( Reducer reducer, CommentedProperties p )
        {
            configurator.configure( reducer, p );
        }

        public void export( Reducer reducer, CommentedProperties p )
        {
            configurator.export( reducer, p );
        }
    }
}
