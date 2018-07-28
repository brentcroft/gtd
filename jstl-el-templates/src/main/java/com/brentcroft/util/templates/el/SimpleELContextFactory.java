package com.brentcroft.util.templates.el;

import com.brentcroft.util.XmlUtils;
import java.beans.FeatureDescriptor;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import javax.el.ArrayELResolver;
import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ResourceBundleELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.xml.bind.DatatypeConverter;
import javax.xml.transform.Templates;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.brentcroft.util.tools.XsdDateUtils;


/**
 * This is a factory for making our own ELContext objects.
 * <p/>
 *
 * Its probably more complicated than we need, although it doesn't use a
 * RootPropertyMapper like the SimpleContext class in JUEL.
 * <p/>
 *
 * Highly influenced by this article:
 *
 * <pre>
 * http://illegalargumentexception.blogspot.co.uk/2008/04/java-using-el-outside-j2ee.html
 * </pre>
 * <p/>
 *
 * Also, worth looking at the source code in JUEL.
 * <p/>
 *
 * @author ADobson
 *
 */
public class SimpleELContextFactory implements ELContextFactory
{
    private final static Logger logger = Logger.getLogger( SimpleELContextFactory.class );

    private static final Map<String, Method> mappedFunctions = new HashMap<>();

    static
    {
        /*
         * functions available in EL expressions
         */
        try
        {
            // see:
            // http://docs.oracle.com/javase/6/docs/api/java/util/Formatter.html
            mappedFunctions.put( "c:format", String.class.getMethod( "format", String.class, Object[].class ) );
            mappedFunctions.put( "c:replaceAll", ELFunctions.class.getMethod( "replaceAll", String.class, String.class, String.class ) );

            mappedFunctions.put( "c:padLeft", ELFunctions.class.getMethod( "padLeft", String.class, char.class, int.class ) );
            mappedFunctions.put( "c:padRight", ELFunctions.class.getMethod( "padRight", String.class, char.class, int.class ) );

            //
            mappedFunctions.put( "c:new-document", XmlUtils.class.getMethod( "parse", String.class ) );
            mappedFunctions.put( "c:document", XmlUtils.class.getMethod( "newDocument", File.class ) );
            mappedFunctions.put( "c:templates", XmlUtils.class.getMethod( "newTemplates", File.class ) );
            mappedFunctions.put( "c:transform", XmlUtils.class.getMethod( "transform", Templates.class, Node.class, Map.class ) );
            mappedFunctions.put( "c:serialize", XmlUtils.class.getMethod( "serialize", Node.class ) );

            // xsd mappings functions
            mappedFunctions.put( "c:printBase64Binary", DatatypeConverter.class.getMethod( "printBase64Binary", byte[].class ) );
            mappedFunctions.put( "c:parseBase64Binary", DatatypeConverter.class.getMethod( "parseBase64Binary", String.class ) );

            mappedFunctions.put( "c:parseBytes", ELFunctions.class.getMethod( "bytesAsString", byte[].class, String.class ) );
            mappedFunctions.put( "c:fileExists", ELFunctions.class.getMethod( "fileExists", String.class ) );

            // math functions
            // mappedFunctions.put( "c:min", Math.class.getMethod( "max",
            // Object.class, Object.class ) );
            // mappedFunctions.put( "c:max", Math.class.getMethod( "min",
            // Object.class, Object.class ) );
            // mappedFunctions.put( "c:round", Math.class.getMethod( "round",
            // Object.class ) );
            // mappedFunctions.put( "c:ceil", Math.class.getMethod( "ceil",
            // Object.class ) );
            // mappedFunctions.put( "c:floor", Math.class.getMethod( "floor",
            // Object.class ) );

            // capture as float
            mappedFunctions.put( "c:float", ELFunctions.class.getMethod( "boxFloat", Float.class ) );
            mappedFunctions.put( "c:random", ELFunctions.class.getMethod( "random" ) );


            mappedFunctions.put( "c:uuid", UUID.class.getMethod( "randomUUID" ) );
            mappedFunctions.put( "c:radix", Long.class.getMethod( "toString", long.class, int.class ) );

            mappedFunctions.put( "c:currentTimeMillis", System.class.getMethod( "currentTimeMillis" ) );
            mappedFunctions.put( "c:date", XsdDateUtils.class.getMethod( "getDate", int[].class ) );
            mappedFunctions.put( "c:currentDate", XsdDateUtils.class.getMethod( "getCurrentDate", int[].class ) );
            mappedFunctions.put( "c:parseXsdDate", XsdDateUtils.class.getMethod( "getDateFromXsdDateTime", String.class ) );
            mappedFunctions.put( "c:formatXsdDate", XsdDateUtils.class.getMethod( "getXsdDateTime", Date.class ) );


            mappedFunctions.put( "c:console", ELFunctions.class.getMethod( "console", String.class, String.class ) );
            mappedFunctions.put( "c:consolePassword", ELFunctions.class.getMethod( "consolePassword", String.class, char[].class ) );
            mappedFunctions.put( "c:consoleFormat", ELFunctions.class.getMethod( "consoleFormat", String.class, Object[].class ) );


        }
        catch ( Exception e )
        {
            throw new RuntimeException( "Failed to initialise function map", e );
        }

        {
            final Level level = Level.DEBUG;
            if ( logger.isEnabledFor( level ) )
            {
                logger.log( level, listMappedFunctions() );
            }
        }

    }

    public static String listMappedFunctions()
    {
        final StringBuilder b = new StringBuilder( "Mapped EL Functions:" );

        for ( Map.Entry<String, Method> entry : mappedFunctions.entrySet() )
        {
            b.append( String.format( "\n  %1$-30s = %2$s", entry.getKey(), entry.getValue() ) );
        }
        return b.toString();
    }


    public static void mapFunction( String prefixedName, Method staticMethod )
    {
        mappedFunctions.put( prefixedName, staticMethod );
    }


    public static void mapFunctions( Map<String, Method> functions )
    {
        mappedFunctions.putAll( functions );
    }

    public ELContext getELContext( Map<?, ?> rootObjects )
    {
        return new SimpleELContext( rootObjects );
    }


    public ELContext getELConfigContext()
    {
        return new RootELContext( null );
    }


    static class SimpleELContext extends ELContext
    {
        protected static final FunctionMapper functionMapper = SIMPLE_FUNCTION_MAPPER;

        protected VariableMapper variableMapper = SIMPLE_VARIABLE_MAPPER;

        protected ELResolver resolver;

        protected final Map<?, ?> rootObjects;

        public SimpleELContext( Map<?, ?> rootObjects )
        {
            this.rootObjects = rootObjects;
        }

        @Override
        public ELResolver getELResolver()
        {
            if ( resolver == null )
            {
                resolver = new CompositeELResolver()
                {
                    {
                        add( new SimpleELResolver( rootObjects ) );
                        add( new ArrayELResolver() );
                        add( new ListELResolver() );
                        add( new BeanELResolver() );
                        add( new MapELResolver() );
                        add( new ResourceBundleELResolver() );
                    }
                };
            }
            return resolver;
        }

        @Override
        public FunctionMapper getFunctionMapper()
        {
            return functionMapper;
        }

        @Override
        public VariableMapper getVariableMapper()
        {
            return variableMapper;
        }
    }

    static final SimpleFunctionMapper SIMPLE_FUNCTION_MAPPER = new SimpleFunctionMapper();

    static class RootELContext extends SimpleELContext
    {
        protected static final FunctionMapper functionMapper = SIMPLE_FUNCTION_MAPPER;

        public RootELContext( Map<?, ?> rootObjects )
        {
            super( rootObjects );
        }
    }


    static class SimpleELResolver extends ELResolver
    {

        private final ELResolver delegate = new MapELResolver();

        private final Map<?, ?> userMap;

        public SimpleELResolver( Map<?, ?> rootObjects )
        {
            this.userMap = rootObjects;
        }

        @Override
        public Object getValue( ELContext context, Object base, Object property )
        {
            if ( base == null )
            {
                base = userMap;
            }
            return delegate.getValue( context, base, property );
        }

        @Override
        public Class<?> getCommonPropertyType( ELContext arg0, Object arg1 )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Iterator<FeatureDescriptor> getFeatureDescriptors( ELContext arg0, Object arg1 )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Class<?> getType( ELContext arg0, Object arg1, Object arg2 )
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean isReadOnly( ELContext arg0, Object arg1, Object arg2 )
        {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public void setValue( ELContext arg0, Object arg1, Object arg2, Object arg3 )
        {
            // TODO Auto-generated method stub

        }
    }

    static class SimpleCompositeELResolver extends CompositeELResolver
    {
        SimpleCompositeELResolver( Map<?, ?> rootObjects )
        {
            add( new SimpleELResolver( rootObjects ) );
            add( new ArrayELResolver() );
            add( new ListELResolver() );
            add( new BeanELResolver() );
            add( new MapELResolver() );
            add( new ResourceBundleELResolver() );
        }
    }

    static class SimpleFunctionMapper extends FunctionMapper
    {
        @Override
        public Method resolveFunction( String prefix, String localName )
        {
            return mappedFunctions.get( ( prefix == null ? "" : prefix + ":" ) + localName );
        }
    }


    static final EmptyFunctionMapper EMPTY_FUNCTION_MAPPER = new EmptyFunctionMapper();

    static final SimpleVariableMapper SIMPLE_VARIABLE_MAPPER = new SimpleVariableMapper();


    static class EmptyFunctionMapper extends FunctionMapper
    {
        @Override
        public Method resolveFunction( String prefix, String localName )
        {
            return null;
        }
    }

    static class SimpleVariableMapper extends VariableMapper
    {
        private Map<String, ValueExpression> variableMap = Collections.emptyMap();

        @Override
        public ValueExpression resolveVariable( String name )
        {
            return variableMap.get( name );
        }

        @Override
        public ValueExpression setVariable( String name, ValueExpression variable )
        {
            if ( variableMap.isEmpty() )
            {
                variableMap = new HashMap<>();
            }
            return variableMap.put( name, variable );
        }

    }
}
