package com.brentcroft.gtd.adapter.utils;

import static java.lang.String.format;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.apache.log4j.Logger;

import com.sun.javafx.event.EventUtil;
import com.sun.javafx.robot.FXRobot;
import com.sun.javafx.robot.FXRobotFactory;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;

public class RobotUtils
{
	private final static Logger logger = Logger.getLogger( RobotUtils.class );

	private static final ExecutorService executor = Executors.newSingleThreadExecutor();

	static
	{
		Runtime.getRuntime().addShutdownHook( new Thread( () -> {

			logger.warn( "Shutting down executor." );

			executor.shutdown();

			try
			{
				if ( !executor.awaitTermination( 1000, TimeUnit.MILLISECONDS ) )
				{
					List< Runnable > droppedTasks = executor.shutdownNow();

					logger.warn( format( "Executor was shut down abruptly: dropped-tasks=[%s]", droppedTasks.size() ) );
				}
			}
			catch ( Exception e )
			{
				logger.warn( "Exception shutting down executor.", e );
			}
		} ) );
	}

	private final static KeyEventParser keyEventParser = new KeyEventParser();

	public static final int DEFAULT_KEYS_DELAY = 20;
	public static final int DEFAULT_CLICK_DELAY = 50;
	public static final int DEFAULT_DOUBLE_CLICK_DELAY = 30;

	private static void run( Runnable job )
	{
		executor.execute( job );
	}

	private static void robotInteraction( Consumer< Robot > interaction, Consumer< Exception > exceptionHandler )
	{
		run( () -> {
			try
			{
				interaction.accept( new Robot() );
			}
			catch ( Exception e )
			{
				handleException( e, exceptionHandler );
			}
		} );
	}

	private static void fxRobotInteraction( Object item, Consumer< FXRobot > interaction,
			Consumer< Exception > exceptionHandler )
	{
		run( () -> {
			try
			{
				interaction.accept( getRobot( item ) );
			}
			catch ( Exception e )
			{
				handleException( e, exceptionHandler );
			}
		} );
	}

	private static void handleException( Exception originalException, Consumer< Exception > exceptionHandler )
	{
		if ( exceptionHandler == null )
		{
			logger.warn( "Unhandled exception.", originalException );
		}
		else
		{
			try
			{
				exceptionHandler.accept( originalException );
			}
			catch ( Exception e2 )
			{
				logger.warn( format( "Exception Handler raised exception: original exception=[%s]", originalException ),
						e2 );
			}
		}
	}

	private static FXRobot getRobot( Object item )
	{
		FXRobot robot = FXRobotFactory.createRobot( getScene( item ) );

		if ( robot == null )
		{
			throw new RuntimeException( format( "FXRobotFactory failed to provide a robot for item [%s].", item ) );
		}

		return robot;
	}

	private static Scene getScene( Object item )
	{
		Scene scene = (item instanceof Node)
				? (( Node ) item).getScene()
				: (item instanceof Tab)
						? (( Tab ) item).getContent().getScene()
						: (item instanceof MenuItem)
								? (( MenuItem ) item).getParentPopup().getScene()
								: null;

		if ( scene == null )
		{
			throw new RuntimeException( format( "Item does not have a scene: [%s].", item ) );
		}

		return scene;
	}

	public static void fxRobotClick( Object item )
	{
		fxRobotInteraction(
				item,
				robot -> {
					robot.mouseClick( MouseButton.PRIMARY );
					robot.waitForIdle();

					logger.debug( format( "Clicked on item: [%s].", item ) );
				},
				e -> {
					logger.warn( format( "Error processing click: item=[%s].", item ), e );
				} );
	}

	public static void fxRobotDoubleClick( Object item )
	{
		fxRobotInteraction(
				item,
				robot -> {
					robot.mouseClick( MouseButton.PRIMARY, 2 );
					robot.waitForIdle();

					logger.debug( format( "Double-clicked on item: [%s].", item ) );
				},
				e -> {
					logger.warn( format( "Error processing double-click: item=[%s].", item ), e );
				} );
	}

	public static void fxRobotKeys( Object item, BooleanSupplier focusCheck, String keys )
	{
		List< List< KeyCode > > kcs = keyEventParser.parseTokens( keys );

		if ( (kcs == null) || (kcs.size() == 0) )
		{
			return;
		}

		fxRobotInteraction(
				item,
				robot -> {

					if ( !focusCheck.getAsBoolean() )
					{
						throw new RuntimeException( "Failed focus check." );
					}

					new FxKeyCodeProcessor()
					{
						private void handle( KeyEvent keyEvent )
						{
							Object result = EventUtil.fireEvent( getScene( item ), keyEvent );

							logger.debug( format( "Fired event: %s", result ) );
						}

						@Override
						void press( KeyCode kc )
						{
							handle(
									new KeyEvent(
											robot,
											getScene( item ),
											KeyEvent.KEY_PRESSED,
											kc.getName(),
											kc.name(),
											kc,
											isShift.get(),
											isCtrl.get(),
											isAlt.get(),
											false ) );
							// robot.keyPress( kc );
						}

						@Override
						void release( KeyCode kc )
						{
							handle(
									new KeyEvent(
											robot,
											getScene( item ),
											KeyEvent.KEY_RELEASED,
											kc.getName(),
											kc.name(),
											kc,
											isShift.get(),
											isCtrl.get(),
											isAlt.get(),
											false ) );
							// robot.keyRelease( kc );
						}

						@Override
						void typed( String text )
						{
							handle(
									new KeyEvent(
											robot,
											getScene( item ),
											KeyEvent.KEY_TYPED,
											text,
											null,
											null,
											false,
											false,
											false,
											false ) );
						}
					}
							.process( keys );

					// robot.waitForIdle();

					logger.debug( format( "Sent: keys=[%s], item=[%s].", keys, item ) );
				},
				e -> {
					logger.warn( format( "Error sending keys: item=[%s], keys=[%s].", item, keys ), e );
				} );
	}

	static abstract class FxKeyCodeProcessor
	{
		AtomicBoolean isAlt = new AtomicBoolean( false );
		AtomicBoolean isCtrl = new AtomicBoolean( false );
		AtomicBoolean isShift = new AtomicBoolean( false );

		abstract void press( KeyCode kc );

		abstract void typed( String text );

		abstract void release( KeyCode kc );

		public void process( String keys )
		{
			List< List< KeyCode > > kcs = keyEventParser.parseTokens( keys );

			if ( (kcs == null) || (kcs.size() == 0) )
			{
				return;
			}

			kcs.forEach( keyCodes -> {

				final int[] lastKeyIndex = { -1 };
				final String[] lastKeyCode = { null };

				RuntimeException lastException = null;

				try
				{
					// press the keys
					keyCodes.forEach( keyCode -> {
						switch ( keyCode )
						{
							case ALT:
								isAlt.set( true );
								break;
							case CONTROL:
								isCtrl.set( true );
								break;
							case SHIFT:
								isShift.set( true );
								break;

							default:
								press( keyCode );
						}

						lastKeyIndex[ 0 ]++;
					} );
				}
				catch ( RuntimeException re )
				{
					lastException = re;
				}
				finally
				{
					// release any keys that were pressed
					for ( int j = lastKeyIndex[ 0 ]; j >= 0; j-- )
					{
						KeyCode keyCode = keyCodes.get( j );

						switch ( keyCode )
						{
							case ALT:
								isAlt.set( false );
								break;
							case CONTROL:
								isCtrl.set( false );
								break;
							case SHIFT:
								isShift.set( false );
								break;

							default:
								release( keyCode );
						}
					}
				}

				if ( lastException != null )
				{
					throw new RuntimeException( format( "Error processing keys: last-key-code=[%s]", lastKeyCode[ 0 ] ),
							lastException );
				}
			} );

			typed( keys );
		}
	}

	public static void awtRobotClick( int[] coords )
	{
		awtRobotClick( DEFAULT_CLICK_DELAY, coords );
	}

	public static void awtRobotDoubleClick( int[] coords )
	{
		awtRobotDoubleClickOnPoint( DEFAULT_DOUBLE_CLICK_DELAY, coords );
	}

	public static void awtRobotClick( int delay, int[] coords )
	{
		awtRobotClickOnPoint( delay, coords );
	}

	public static void awtRobotClickOnPoint( int[] coords )
	{
		awtRobotClickOnPoint( DEFAULT_CLICK_DELAY, coords );
	}

	public static void awtRobotKeys( int[] coords, BooleanSupplier focusCheck, final String keys )
	{
		awtRobotKeys( DEFAULT_KEYS_DELAY, coords, focusCheck, keys );
	}

	public static void awtRobotClickOnPoint( int delay, int[] coords )
	{
		robotInteraction(
				robot -> {
					// logger.warn( format( "About to move to point: [ %s, %s ].", coords[ 0 ],
					// coords[ 1 ] ) );

					robot.mouseMove( coords[ 0 ], coords[ 1 ] );

					try
					{
						// logger.warn( format( "About to press button down: [ %s, %s ].", coords[ 0 ],
						// coords[ 1 ] ) );

						robot.mousePress( InputEvent.BUTTON1_DOWN_MASK );
						robot.delay( delay );
					}
					finally
					{
						// logger.warn( format( "About to release button down: [ %s, %s ].", coords[ 0
						// ], coords[ 1 ] ) );

						robot.mouseRelease( InputEvent.BUTTON1_DOWN_MASK );
						robot.delay( delay );
					}

					logger.warn( format( "Clicked on point: [ %s, %s ].", coords[ 0 ], coords[ 1 ] ) );
				},
				e -> {
					logger.warn( format( "Error clicking on point: coords=[ %s, %s ].", coords[ 0 ], coords[ 1 ] ), e );
				} );
	}

	public static void awtRobotDoubleClickOnPoint( int delay, int[] coords )
	{
		if ( (coords == null) || (coords.length < 2) )
		{
			throw new IllegalArgumentException(
					format( "Coords cannot be null or with length less than 2: %s", coords ) );
		}

		robotInteraction(
				robot -> {

					robot.mouseMove( coords[ 0 ], coords[ 1 ] );

					try
					{
						robot.mousePress( InputEvent.BUTTON1_DOWN_MASK );
						robot.delay( delay );
					}
					finally
					{
						robot.mouseRelease( InputEvent.BUTTON1_DOWN_MASK );
						robot.delay( delay );
					}

					try
					{
						robot.mousePress( InputEvent.BUTTON1_DOWN_MASK );
						robot.delay( delay );
					}
					finally
					{
						robot.mouseRelease( InputEvent.BUTTON1_DOWN_MASK );
					}

					logger.debug( format( "Double-clicked on point: [ %s, %s ].", coords[ 0 ], coords[ 1 ] ) );
				},
				e -> {
					logger.warn( format( "Error double-clicking on point: coords=[%s,%s].", coords[ 0 ], coords[ 1 ] ),
							e );
				} );
	}

	public static void awtRobotKeys( final int keyDelay, int[] coords, BooleanSupplier focusCheck, final String keys )
	{
		if ( (coords == null) || (coords.length < 2) )
		{
			throw new IllegalArgumentException(
					format( "Coords cannot be null or with length less than 2: %s", coords ) );
		}

		// don't send keys to screen origin
		if ( IntStream.of( coords )
				.filter( c -> c == 0 )
				.findAny()
				.isPresent() )
		{
			if ( logger.isDebugEnabled() )
			{
				logger.debug(
						format( "Intercepted attempt by [%s] to send keys [%s] to screen origin.", coords, keys ) );
			}

			return;
		}

		List< List< Integer > > kcs = keyEventParser.parse( keys );

		if ( (kcs == null) || kcs.isEmpty() )
		{
			return;
		}

		int[][] keyCodesSequences = KeyEventParser.toArray( kcs );

		robotInteraction(
				robot -> {

					robot.mouseMove( coords[ 0 ], coords[ 1 ] );

					try
					{
						robot.mousePress( InputEvent.BUTTON1_DOWN_MASK );
						robot.delay( DEFAULT_CLICK_DELAY );
					}
					finally
					{
						robot.mouseRelease( InputEvent.BUTTON1_DOWN_MASK );
					}

					if ( !focusCheck.getAsBoolean() )
					{
						throw new RuntimeException( "Failed focus check." );
					}

					for ( int[] keyCodes : keyCodesSequences )
					{
						int lastKeyIndex = -1;
						int lastKeyCode = -1;

						Throwable lastException = null;

						try
						{
							// press the keys
							for ( int keyCode : keyCodes )
							{
								lastKeyCode = keyCode;

								robot.keyPress( keyCode );
								lastKeyIndex++;
							}
						}
						catch ( Throwable re )
						{
							lastException = re;
						}
						finally
						{
							robot.delay( keyDelay );

							// release any keys that were pressed
							for ( int i = lastKeyIndex; i >= 0; i-- )
							{
								try
								{
									robot.keyRelease( keyCodes[ i ] );
								}
								catch ( Throwable t )
								{
									logger.warn( format( "Error releasing key: [%s]", keyCodes[ i ] ), t );
								}
							}

							robot.delay( keyDelay );
						}

						if ( lastException != null )
						{
							throw new RuntimeException(
									format( "Error processing keys: last-key-code=[%s]", lastKeyCode ), lastException );
						}

						logger.debug( format( "Sent: coords=[%s,%s], keys=[%s].", coords[ 0 ], coords[ 1 ], keys ) );
					}
				},

				e -> {
					logger.warn(
							format( "Error sending keys: coords=[%s,%s], keys=[%s].", coords[ 0 ], coords[ 1 ], keys ),
							e );
				} );
	}
}
