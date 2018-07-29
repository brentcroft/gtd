package com.brentcroft.gtd.cucumber;

import static java.lang.String.format;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.brentcroft.gtd.js.context.Context;
import com.brentcroft.gtd.js.context.ContextUnit;
import com.brentcroft.util.FileUtils;
import com.brentcroft.util.templates.JstlTemplateManager;

import cucumber.runtime.Backend;
import cucumber.runtime.CucumberBackend;
import cucumber.runtime.CucumberSummaryPrinter;
import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.Reflections;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.formatter.FormatterFactory;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.model.CucumberFeature;

public class Cucumber
{
	private final static Logger logger = Logger.getLogger( Cucumber.class );

	public final static String CUCUMBER_GLUE_KEY = "cucumber.glue";

	private JstlTemplateManager jstl = new JstlTemplateManager();

	private String path;

	public Cucumber( String path )
	{
		this.path = path;
	}

	// this extracts args from properties
	private List< String > getArgs( Properties p )
	{
		List< String > args = new ArrayList< String >();

		String glue = p.getProperty( CUCUMBER_GLUE_KEY );

		if ( glue != null )
		{
			String[] gluePaths = glue.split( "\\s*,\\s*" );
			for ( String gluePath : gluePaths )
			{
				args.add( "--glue" );
				args.add( gluePath.trim() );

				logger.info( "Added glue: " + gluePath.trim() );
			}
		}

		args.add( "--name" );
		args.add( "gui-test-driver" );
		args.add( "--plugin" );
		args.add( "progress" );
		args.add( "--monochrome" );
		args.add( "--strict" );

		return args;
	}

	public static class FeatureResult
	{
		List< Throwable > errors;
		CucumberFeature cucumberFeature;
		String report;

		public FeatureResult( CucumberFeature cucumberFeature, List< Throwable > errors, String report )
		{
			this.cucumberFeature = cucumberFeature;
			this.errors = errors;
			this.report = report;
		}

		public boolean isSuccess()
		{
			return errors.isEmpty();
		}

		public boolean isFailure()
		{
			return !isSuccess();
		}

		public String getReport()
		{
			return report;
		}

		public String toString()
		{
			return format( "Feature: [%s]: %s%n%s",
					cucumberFeature.getPath(),
					isSuccess() ? "PASSED" : "FAILED",
					report
			);
		}
	}

	List< FeatureResult > featureResults = new ArrayList<>();

	public List< FeatureResult > processFeature( final String rawFeature, Context context, PrintWriter printer ) throws Exception
	{
		// immediately whack the feature as a JSTL template
		final String relUri = (context.getRoot() == null)
				? null
				: context.getRoot().toURI().toURL().toExternalForm();

		final Map< String, Object > params = new HashMap<>();

		params.put( "context", context );

		final String feature = jstl.expandText( rawFeature, relUri, params );

		final List< CucumberFeature > cucumberFeatures = new ArrayList< CucumberFeature >();

		final List< Object > filters = new ArrayList< Object >();

		try ( final FeatureBuilder builder = new FeatureBuilder( cucumberFeatures ) )
		{
			builder.parse( new Resource()
			{

				@Override
				public String getPath()
				{
					return path.indexOf( '/' ) > -1
							? path.substring( path.lastIndexOf( '/' ) + 1 )
							: path;
				}

				@Override
				public String getAbsolutePath()
				{
					return path;
				}

				@Override
				public InputStream getInputStream() throws IOException
				{
					return new ByteArrayInputStream( feature.getBytes( StandardCharsets.UTF_8 ) );
				}

				@Override
				public String getClassName( String extension )
				{
					return "(none)";
				}
			}, filters );
		}

		// MUY IMPORTANTE
		// this is where the glue gets passed over to cucumber
		List< String > args = getArgs( context.getProperties() );

		ClassLoader classLoader = getClass().getClassLoader();

		ResourceLoader resourceLoader = new MultiLoader( classLoader );

		// step definitions will be instantiated from scripts
		List< Backend > backends = getBackends( classLoader, resourceLoader, context );

		// override print method in context
		// context.getBindings().put( "printer", printer );
		// context.execute( "function print( text ) { printer.println( text ); }" );
		// context.execute( "function print( text ) { nashorn.global.print( text ); }"
		// );

		RuntimeOptions runtimeOptions = new RuntimeOptions( args );

		for ( CucumberFeature cucumberFeature : cucumberFeatures )
		{
			Runtime runtime = new Runtime(
					resourceLoader,
					classLoader,
					backends,
					runtimeOptions );

			// slip in our printer
			context.getBindings().put( "printer", printer );

			try
			{
				cucumberFeature.run(
						FormatterFactory.getUsageFormatter( printer ),
						FormatterFactory.getNullReporter(),
						runtime );
			}
			finally
			{
				// slip out our printer
				context.getBindings().remove( "printer" );
			}

			final ByteArrayOutputStream baos = new ByteArrayOutputStream();

			new CucumberSummaryPrinter( new PrintStream( new BufferedOutputStream( baos ) ) ).print( runtime );

			String report = baos.toString();

			featureResults.add( new FeatureResult( cucumberFeature, runtime.getErrors(), report ) );
		}

		cucumberFeatures.clear();

		return featureResults;
	}

	private List< Backend > getBackends( ClassLoader classLoader, ResourceLoader resourceLoader, Context context )
	{
		ArrayList< Backend > backends = new ArrayList< Backend >();

		final Collection< ? extends Backend > annotationBackends = new Reflections(
				new ResourceLoaderClassFinder(
						resourceLoader,
						classLoader ) )
								.instantiateSubclasses(
										Backend.class,
										"cucumber.runtime",
										new Class[] {
												ResourceLoader.class },
										new Object[] {
												resourceLoader } );

		if ( annotationBackends != null && !annotationBackends.isEmpty() )
		{
			logger.info( format( "Adding [%s] Cucumber Backends...", annotationBackends.size() ) );

			// now look for our backend, and set the context
			for ( Backend b : annotationBackends )
			{
				if ( b instanceof CucumberBackend )
				{
					// put our context into cucumber
					// although note that cucumber may reconfigure it
					(( CucumberBackend ) b).withContext( context );

					// make backend available to step definition ingestor
					context.getBindings().put( "backend", b );

					logger.info( format( "Adding Cucumber Javascript backend [%s].", b ) );
				}
				else
				{
					logger.info( format( "Adding Cucumber Java backend [%s].", b ) );
				}
			}

			backends.addAll( annotationBackends );
		}
		else
		{
			// no backends - there will be a problem
		}

		return backends;
	}

	private static void usage()
	{
		System.out.println( "java ... com.brentcroft.gtd.cucumber.Cucumber config-file feature-file[, feature-file]*" );
	}

	public static void main( String[] args )
	{
		if ( args.length < 2 )
		{
			usage();
			System.exit( 1 );
		}

		try
		{
			logger.info( format( "Calling ContextUnit file [%s].", args[ 0 ] ) );

			ContextUnit unit = new ContextUnit( FileUtils.resolvePath( null, args[ 0 ] ) );

			PrintWriter pw = new PrintWriter( new OutputStream()
			{
				Optional< Logger > cucumberLogger = Optional.ofNullable( Logger.getLogger( "CUCUMBER" ) );

				private ByteArrayOutputStream buf;
				private final CharsetDecoder decoder = Charset.forName( "UTF-8" ).newDecoder();

				@Override
				public synchronized void write( int b ) throws IOException
				{
					synchronized ( this )
					{
						if ( this.buf == null )
						{
							this.buf = new ByteArrayOutputStream();
						}
						this.buf.write( b );
					}
				}

				@Override
				public void close() throws IOException
				{
					flush();
				}

				@Override
				public void flush() throws IOException
				{
					synchronized ( this )
					{
						if ( this.buf == null )
						{
							return;
						}
						final ByteBuffer byteBuffer = ByteBuffer.wrap( this.buf.toByteArray() );
						final CharBuffer charBuffer = this.decoder.decode( byteBuffer );
						try
						{
							String text = charBuffer.toString();

							// in this case we don't want extra lines - which are likely from scripts etc
							cucumberLogger
									.orElse( logger )
									.info( text.trim() );
						}
						finally
						{
							this.buf = null;
						}
					}
				}
			} );

			try
			{
				Context context = unit.newContext();

				for ( int i = 1, n = args.length; i < n; i++ )
				{
					File file = FileUtils.resolvePath( null, args[ i ] );

					if ( file.isDirectory() )
					{
						List< Path > paths = Files
								.list( file.toPath() )
								.filter( path -> path.toFile().getName().endsWith( ".feature" ) )
								.collect( Collectors.toList() );

						logger.info( format( "Loading [%s] features from [%s].", paths.size(), args[ i ] ) );

						for ( Path path : paths )
						{
							logger.info( format( "Calling cucumber feature file [%s].", path ) );

							String feature = FileUtils.getFileOrResourceAsString( null, path.toString() );

							List< FeatureResult > featureResults = new Cucumber( path.toString() ).processFeature( feature, context, pw );

							if ( featureResults
									.stream()
									.anyMatch( FeatureResult::isFailure ) )
							{
								String msg = featureResults
										.stream()
										.filter( FeatureResult::isFailure )
										.map( FeatureResult::toString )
										.collect( Collectors.joining( "\n" ) );

								throw new RuntimeException( msg );
							}
						}
					}
					else
					{
						logger.info( format( "Calling cucumber feature file [%s].", args[ i ] ) );

						List< FeatureResult > featureResults = new Cucumber( args[ i ] )
								.processFeature(
										FileUtils.getFileOrResourceAsString( null, args[ i ] ),
										context,
										pw );

						if ( featureResults
								.stream()
								.anyMatch( FeatureResult::isFailure ) )
						{
							String msg = featureResults
									.stream()
									.filter( FeatureResult::isFailure )
									.map( FeatureResult::toString )
									.collect( Collectors.joining( "\n" ) );

							throw new RuntimeException( msg );
						}
					}
				}
			}
			finally
			{
				try
				{
					unit.stopAllSessions();
				}
				catch ( Exception allowed )
				{
					logger.warn( "Stopping all sessions.", allowed );
				}
			}
		}
		catch ( Exception e )
		{
			throw e instanceof RuntimeException
					? ( RuntimeException ) e
					: new RuntimeException( e );
		}
	}
}
