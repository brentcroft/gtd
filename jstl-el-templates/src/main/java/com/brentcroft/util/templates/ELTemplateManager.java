package com.brentcroft.util.templates;

import static java.lang.String.format;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.brentcroft.util.templates.el.ELContextFactory;
import com.brentcroft.util.templates.el.ELFilter;
import com.brentcroft.util.templates.el.SimpleELContextFactory;
import com.brentcroft.util.templates.el.StandardELFilter;
import com.brentcroft.util.tools.StreamTools;


/**
 * Maintains a set of ELTemplates (i.e. text containing EL expressions) and
 * provides two methods: <code>expandText( text, context )</code>, and
 * <code>expandUri( uri, context )</code>, to expand ELTemplates within the
 * context of a map of objects provided at expansion time.
 * <p/>
 *
 * ELTemplates are either anonymous (and not cached), or are loaded from a uri,
 * compiled and cached (using the uri as a key).
 * <p/>
 *
 * NB: non-standard el-expressions of the form ${*green} are expanded as though
 * "green" (or "green.tpl") is the name of a template.
 * <p/>
 *
 * By default, the implementation obtains an instance of an ExpressionFactory by
 * calling <code>javax.el.ExpressionFactory.newInstance()</code>.
 * <p/>
 * The default provider mechanism can be overridden by setting the
 * <code>expressionFactoryClass</code> property to the fully qualified name of a
 * class that extends <code>javax.el.ExpressionFactory.newInstance()</code>
 * <br/>
 * (immediately clearing any existing ExpressionFactory instance).
 * <p/>
 *
 * @author ADobson
 *
 * @see <a href="http://en.wikipedia.org/wiki/Unified_Expression_Language" >
 *      Unified_Expression_Language</a>
 * @see <a href="http://docs.oracle.com/cd/E19226-01/820-7627/gjddd/">The Java
 *      EE 6 Tutorial, Volume I: Chapter 6 Unified Expression Language</a>
 * @see <a href="http://juel.sourceforge.net/" >JUEL</a>
 *
 */
public class ELTemplateManager
{
    private static Logger logger = Logger.getLogger( ELTemplateManager.class.getCanonicalName() );

    private static ExpressionFactory expressionFactory = null;

    private static final ELContextFactory elContextFactory = new SimpleELContextFactory();

    public static final String DEFAULT_TEMPLATE_EXTENSION = ".tpl";

    public static final Pattern EL_EXPRESSION_PATTERN = Pattern.compile( "\\$\\{[^}]+\\}" );


    private final Map<String, ELTemplate> templates = new HashMap<>();

    // 2013-06-01: ACD: this is what I now would expect by default
    private ELFilter valueExpressionFilter = null;


    // default load mechanism fails if ELTemplateManager is not loaded by the
    // common class-loader
    private String expressionFactoryClass = null;


    public ELTemplateManager withELFilter(ELFilter elFilter)
    {
        this.setValueExpressionFilter( elFilter );
        return this;
    }


    public void setExpressionFactoryClass( String expressionFactoryClass )
    {
        this.expressionFactoryClass = expressionFactoryClass;

        // reset
        expressionFactory = null;
    }


    public ELContextFactory getELContextFactory()
    {
        return elContextFactory;
    }

    public ELContext getELContext( Map<?, ?> rootObjects )
    {
        return elContextFactory.getELContext( rootObjects );

    }

    public ValueExpression getValueExpression( String expression, Map<?, ?> rootObjects, Class<?> clazz )
    {
        return getExpressionFactory().createValueExpression(
                                                             getELContext( rootObjects ),
                                                             expression,
                                                             clazz );
    }


    private ExpressionFactory getExpressionFactory()
    {
        if ( expressionFactory != null )
        {
            return expressionFactory;
        }
        else if ( expressionFactoryClass == null )
        {
            expressionFactory = ExpressionFactory.newInstance();

            {
                final Level level = Level.DEBUG;

                if ( logger.isEnabledFor( level ) )
                {
                    logger.log( level, "Loaded default ExpressionFactory: " + expressionFactory );
                }
            }
        }
        else
        {
            try
            {
                expressionFactory = (ExpressionFactory) getClass().getClassLoader().loadClass( expressionFactoryClass )
                                                                  .newInstance();

                {
                    final Level level = Level.DEBUG;

                    if ( logger.isEnabledFor( level ) )
                    {
                        logger.log( level, "Loaded named ExpressionFactory: " + expressionFactory );
                    }
                }
            }
            catch ( Exception e )
            {
                throw new RuntimeException( "Unable to instantiate ExpressionFactory using classname: "
                                            + expressionFactoryClass,
                        e );
            }
        }

        return expressionFactory;
    }


    /**
     * Set an ELFilter to apply to the results of ValueExpressions.
     *
     * @param valueExpressionFilter
     */
    public void setValueExpressionFilter( ELFilter valueExpressionFilter )
    {
        this.valueExpressionFilter = valueExpressionFilter;
    }

    /**
     * Set a StandardELFilter to apply to the results of ValueExpressions.
     *
     * @param standardFilter
     */
    public void setValueExpressionStandardFilter( StandardELFilter standardFilter )
    {
        setValueExpressionFilter( standardFilter );
    }


    /**
     * Find a template and return it's rendering of a Map by the template.
     * <p/>
     * If a template is not already cached (with the key
     * <code>templateUri</code>) then a new template is built (and cached) by
     * opening, and parsing the stream from <code>templateUri</code>.
     * <p/>
     * Return the rendering of the Map <code>rootObjects</code> by the template.
     * <p/>
     *
     *
     * @param uri
     *            identifies a template
     * @param rootObjects
     *            a Map of root objects (to make accessible in EL expressions in
     *            the template)
     * @return the rendering of the Map <code>rootObjects</code> by the
     *         specified template <code>templateUri</code>
     */
    public String expandUri( final String uri, Map<String, ? super Object> rootObjects )
    {
        // if no period then tack a default extension on the end
        final int lastIndexOfPeriod = uri.lastIndexOf( '.' );
        final String newUri = ( lastIndexOfPeriod > -1 ) ? uri : ( uri + DEFAULT_TEMPLATE_EXTENSION );

        // find
        if ( !templates.containsKey( newUri ) )
        {
            loadTemplate( newUri );
        }

        // render
        return templates.get( newUri ).render( rootObjects );
    }


    /**
     * Only allow one thread to load a template at any one time.
     * <p/>
     * If the desired template has already been loaded then just return
     * otherwise load and cache the template.
     *
     * @param uri
     *            the uri of the template to load.
     */
    private synchronized void loadTemplate( final String uri )
    {
        // find
        if ( templates.containsKey( uri ) )
        {
            return;
        }

        // build and cache
        templates.put( uri, new ELTemplateBuilder().build( uri ) );

        {
            final Level level = Level.INFO;

            if ( logger.isEnabledFor( level ) )
            {
                logger.log( level, "Loaded template: " + uri );
            }
        }
    }


    /**
     * Expands the supplied <code>elText</code> so that all EL tags are replaced
     * with their values calculated with respect to the supplied map of root
     * objects.
     *
     * @param elText
     *            the elText to be expanded
     * @return the expanded elText
     */
    public String expandText( String elText, Map<String, Object> rootObjects )
    {
        return buildTemplate( elText ).render( rootObjects );
    }


    /**
     * Builds an anonymous <code>ELTemplate</code> from the supplied text.
     * <p/>
     * The template is not cached (it has no uri).
     *
     * @param elText
     *            the text to be decomposed into an ELTemplate
     * @return the new ELTemplate
     */
    public ELTemplate buildTemplate( String elText )
    {
        return new ELTemplateBuilder().parse( elText );
    }


    /**
     * Test if the candidate text contains any EL template expressions.
     *
     * @param candidate the text to detect EL expressions in
     * @return true if the candidate text has at least one EL expression otherwise false.
     */
    public static boolean hasTemplate(String candidate)
    {
        return EL_EXPRESSION_PATTERN.matcher( candidate ).find();
    }


    final ELContext context = elContextFactory.getELConfigContext();


    /**
     * A decomposition of a text stream into a list of
     * <code>ELTemplateElement</code> elements.
     * <p/>
     * Each <code>ELTemplateElement</code> has a type that is either:<br/>
     *
     * LITERAL, VALUE_EXPRESSION or TEMPLATE_REF.
     */
    public class ELTemplate implements Renderable
    {
        private final List<ELTemplateElement> elements = new ArrayList<>();

        private String localUri;


        public ELTemplate withUri( String uri )
        {
            this.localUri = uri;
            return this;
        }

        @Override
        public String toString()
        {
            StringBuilder b = new StringBuilder();

            if ( !elements.isEmpty() )
            {
                elements.forEach( b::append );
            }

            return b.toString();
        }

        /**
         * The key function of a template is to render itself (using a Map of
         * objects providing the EL namespace).
         *
         * @param rootObjects
         *            a Map of named objects
         * @return a String containing the rendering of the template
         */

        public String render( Map<String, ? super Object> rootObjects )
        {
            final ELContext context = elContextFactory.getELContext( rootObjects );

            final StringBuilder out = new StringBuilder();

            for ( ELTemplateElement element : elements )
            {
                switch ( element.type )
                {
                    case LITERAL:

                        out.append( element.element );

                        break;


                    case TEMPLATE_REF:

                        // mess with the template reference if necessary

                        // if no period then tack a ".tpl" on the end
                        final int lastIndexOfPeriod = element.element.lastIndexOf( '.' );
                        final String newFilename = ( lastIndexOfPeriod > -1 ) ? element.element
                                : ( element.element + ".tpl" );

                        if ( localUri == null )
                        {
                            // recurse
                            out.append( expandUri( newFilename, rootObjects ) );
                        }
                        else
                        {
                            // if have last slash in local uri then replace
                            // filename with newFilename
                            final int lastIndexOfSlash = localUri.lastIndexOf( '/' );
                            final String newUri = ( ( lastIndexOfSlash > -1 ) ? localUri.substring( 0, lastIndexOfSlash )
                                    : localUri ) + "/" + newFilename;

                            out.append( expandUri( newUri, rootObjects ) );
                        }

                        break;


                    case VALUE_EXPRESSION:

                        if ( element.valueExpression == null )
                        {
                            element.valueExpression = getExpressionFactory().createValueExpression( context,
                                                                                                    element.element,
                                                                                                    Object.class );
                        }

                        try
                        {
                            final Object value = element.valueExpression.getValue( context );

                            // maybe filter value expression
                            final Object filteredValue = ( valueExpressionFilter == null || value == null ) ? value
                                    : valueExpressionFilter.filter( value );

                            out.append( filteredValue == null ? "" : filteredValue );

                        }
                        catch ( RuntimeException e )
                        {
                            throw new ELTemplateException( format( "Failed to evaluate EL Expression [%s]: %s", element.valueExpression, e.getMessage() ), e );
                        }

                        break;

                    default:
                        throw new ELTemplateException( "Unexpected ELType: " + element.type );
                }
            }

            return out.toString();
        }


        void addLiteral( String text )
        {
            if ( text != null && text.length() > 0 )
            {
                elements.add( new ELTemplateElement( text, ELType.LITERAL ) );
            }
        }

        void addValueExpression( String text )
        {
            elements.add( new ELTemplateElement( text.trim(), ELType.VALUE_EXPRESSION ) );
        }

        void addTemplateRef( String text )
        {
            elements.add( new ELTemplateElement( text.trim(), ELType.TEMPLATE_REF ) );
        }


        class ELTemplateElement
        {
            private final String element;

            private final ELType type;

            private ValueExpression valueExpression;


            ELTemplateElement( String element, ELType type )
            {
                this.element = element;
                this.type = type;

                valueExpression = getExpressionFactory().createValueExpression( context,
                                                                                element,
                                                                                Object.class );
            }

            @Override
            public String toString()
            {
                return element;
            }
        }
    }


    class ELTemplateBuilder
    {
        public ELTemplate build( String uri )
        {
            try
            {
                return parse(
                              StreamTools.readUrl(
                                                   StreamTools.getLocalFileURL( getClass(), uri ) ) ).withUri( uri );
            }
            catch ( Exception e )
            {
                throw (RuntimeException) e;
            }
        }

        /*
         * Is the choice of the state numbers constrained?
         *
         * Yes it is!
         *
         * Try [OUTSIDE = 87] and [ENTERING = 0] and see the compiler errors.
         */
        private static final int OUTSIDE = 0;

        private static final int ENTERING = 0x10000;

        private static final int INSIDE = 0x2000;

        // 36
        private static final char PILOT = '$';

        // 35
        private static final char PILOT2 = '#';

        // 123
        private static final char START = '{';

        // 125
        private static final char END = '}';


        /*
         * Copied (and modified from MrTemplate - thanks Frank)
         */
        private ELTemplate parse( String elText )
        {
            final ELTemplate elTemplate = new ELTemplate();


            int state = OUTSIDE;
            boolean pilot2 = false;

            final StringBuilder token = new StringBuilder();
            final StringBuilder literal = new StringBuilder();
            final CharacterIterator it = new StringCharacterIterator( elText );

            for ( char c = it.first() ; c != CharacterIterator.DONE ; c = it.next() )
            {
                switch ( state + c )
                {
                    case OUTSIDE + PILOT2:
                        pilot2 = true;
                        state = ENTERING;
                        break;

                    case OUTSIDE + PILOT:
                        pilot2 = false;
                        state = ENTERING;
                        break;

                    case ENTERING + START:
                        state = INSIDE;
                        break;

                    case INSIDE + END:

                        state = OUTSIDE;

                        elTemplate.addLiteral( literal.toString() );

                        literal.setLength( 0 );

                        final String tokenText = token.toString().trim();

                        if ( tokenText.length() < 1 )
                        {
                            throw new ELTemplateException( "EL Template has no tokenText!" );
                        }

                        switch ( tokenText.charAt( 0 ) )
                        {
                            case '*': // frank's original feature for
                                      // including
                                      // another template
                                elTemplate.addTemplateRef( tokenText.substring( 1 ) );
                                break;

                            default:
                                elTemplate.addValueExpression( ( pilot2 ? "#{" : "${" ) + tokenText + "}" );
                                break;
                        }

                        token.setLength( 0 );


                        pilot2 = false;

                        break;


                    default:
                        switch ( state )
                        {
                            case ENTERING: // got $ or # without { - treat it as
                                           // a
                                           // literal
                                state = OUTSIDE;
                                literal.append( pilot2 ? PILOT2 : PILOT );
                                literal.append( c );
                                break;

                            case INSIDE:
                                token.append( c );
                                break;

                            default:
                                literal.append( c );
                        }
                }
            }

            if ( state == ENTERING )
            { // catch the odd case of $ at EOF
                literal.append( ( pilot2 ? PILOT2 : PILOT ) );
            }
            else if ( state == INSIDE )
            { // catch the even odder case of unclosed
              // ${ at EOF
                literal.append( ( pilot2 ? PILOT2 : PILOT ) );
                literal.append( START );
                literal.append( token.toString() );
            }

            if ( literal.length() > 0 )
            {
                elTemplate.addLiteral( literal.toString() );
            }

            literal.setLength( 0 );

            return elTemplate;
        }
    }

    /**
     * We are free to add new types (e.g. like TEMPLATE_REF) as long as we can
     * justify (and implement) their purpose.
     *
     * @author ADobson
     *
     */
    enum ELType
    {
        /**
         * A literal segment of text that contains no expressions.
         *
         */
        LITERAL,

        /**
         * An EL expression.
         * <p/>
         *
         * E.g. ${request.id}
         */
        VALUE_EXPRESSION,


        /**
         * A template reference.
         * <p/>
         *
         * E.g. ${*header} or ${*header.tpl}
         */
        TEMPLATE_REF,

    }
}
