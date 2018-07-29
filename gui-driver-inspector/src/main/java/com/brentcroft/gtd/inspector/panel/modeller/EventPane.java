package com.brentcroft.gtd.inspector.panel.modeller;

import static com.brentcroft.gtd.driver.Backend.STAR;
import static com.brentcroft.util.XmlUtils.newTemplates;
import static com.brentcroft.util.XmlUtils.parse;
import static com.brentcroft.util.XmlUtils.removeTrimmedEmptyTextNodes;
import static com.brentcroft.util.XmlUtils.serialize;
import static com.brentcroft.util.XmlUtils.transformToText;
import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.transform.Templates;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.brentcroft.gtd.driver.client.GuiSession;
import com.brentcroft.gtd.inspector.Inspector;
import com.brentcroft.gtd.inspector.model.GuiEventListener;
import com.brentcroft.gtd.inspector.panel.ModelTreePane;
import com.brentcroft.gtd.js.context.Context;
import com.brentcroft.gtd.js.context.model.ModelMember;
import com.brentcroft.gtd.js.context.model.ModelObject;
import com.brentcroft.gtd.utilities.WebTemplatePane;
import com.brentcroft.util.FileUtils;
import com.brentcroft.util.StringUpcaster;
import com.brentcroft.util.XPathUtils;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * Created by Alaric on 14/11/2016.
 */
@SuppressWarnings( "restriction" )
public class EventPane extends SplitPane implements GuiEventListener
{
	private final static Logger logger = Logger.getLogger( EventPane.class );

	private static final String EMPTY_HISTORY = "<event-history xmlns:a=\"com.brentcroft.gtd.adapter\"/>";

	private String modelHistoryTransformPropertyName = "modeller.history.templates";
	private static final String MODEL_HISTORY_TRANSFORM_DEFAULT_URI = "xslt/generate-model-history.xslt";

	private Document history = parse( EMPTY_HISTORY );
	private Templates modelHistoryTransform = null;

	private String sessionKey;

	private Context context;
	private ModelTreePane modelTreePane;

	private WebTemplatePane eventPane = WebTemplatePane.newXmlEditor( "<!-- -->" );
	private final WebTemplatePane historyPane = WebTemplatePane.newXmlEditor( EMPTY_HISTORY );

	// private ComboBox< AWTEventMask > eventMask = new ComboBox< AWTEventMask >();
	// private TextInputControl eventMaskText = new TextField( "" +
	// AWTEventMask.DEFAULT_GUI.getMask() );

	private TextInputControl eventIdFilterText = new TextField( "400, 500, 502, click, input" );
	private TextInputControl historyMaskText = new TextField( "400, 500, 502, click, input" );
	private final CheckBox historyTransformSwitch = new CheckBox( "script" );

	private Button runHistoryScriptButton = Inspector.newButton( "Run...", event -> {
		Inspector.execute( "History script", () -> {
			Platform.runLater( () -> {
				try
				{
					context.execute( historyPane.getText() );
				}
				catch ( Exception e )
				{
					logger.warn( "History script error.", e );
				}
			} );
		} );
	} );

	public EventPane( String sessionKey, Context context, ModelTreePane modelTreePane )
	{
		this.sessionKey = sessionKey;

		this.context = context;
		this.modelTreePane = modelTreePane;

		// TODO: especially for configuration
		configure();

		eventPane.setId( "eventPane" );

		// eventMask.setId( "eventMask" );
		// eventMask
		// .getItems()
		// .addAll( AWTEventMask.values() );
		//
		// eventMask.setOnAction( ( event ) ->
		// {
		//
		// AWTEventMask aem = eventMask
		// .getSelectionModel()
		// .getSelectedItem();
		//
		// if ( aem != null )
		// {
		// Platform.runLater( () ->
		// {
		// eventMaskText.setText( "" + aem.getMask() );
		// } );
		// }
		// } );

		// eventMaskText.setId( "eventMaskText" );

		historyPane.setId( "historyPane" );

		{
			VBox.setVgrow( eventPane, Priority.ALWAYS );

			CheckBox notificationCheckBox = new CheckBox( "Notifications" );

			notificationCheckBox.setOnAction( actionNotifications );
			//
			// Button synchDriverEventMask = new Button( "Set Driver Mask" );
			//
			// synchDriverEventMask.setOnAction( actionSynchDriverMask );

			eventPane
					.withToolbarNodes(
							notificationCheckBox,
							// eventMask,
							// eventMaskText,
							// synchDriverEventMask,
							eventIdFilterText
			);

		}

		{
			VBox.setVgrow( historyPane, Priority.ALWAYS );

			historyTransformSwitch.setOnAction( ( e ) -> {
				Inspector.execute( "update-historyPane", () -> {
					updateHistoryPane( () -> {
						if ( historyTransformSwitch.isSelected() )
						{
							historyPane.withToolbarNodes( runHistoryScriptButton );

							historyPane.withTemplateType( WebTemplatePane.TemplateType.javascript );
						}
						else
						{
							historyPane.withoutToolbarNodes( runHistoryScriptButton );

							historyPane.withTemplateType( WebTemplatePane.TemplateType.xml );
						}
					} );
				} );
			} );

			historyPane
					.withToolbarNodes(

							Inspector.newButton( "clear", ( event ) -> {
								history = parse( EMPTY_HISTORY );

								updateHistoryPane();
							} ),

							Inspector.newButton( "reload", ( event ) -> {
								configure();

								updateHistoryPane();
							} ),

							historyMaskText,

							historyTransformSwitch
							);
		}

		// now me
		setOrientation( Orientation.VERTICAL );
		setDividerPositions( 0.2f, 0.8f );

		getItems()
				.addAll(
						eventPane,
						historyPane );
	}

	private void configure()
	{
		Properties p = context.getProperties();

		String modelHistoryTransformUri = p.getProperty(
				modelHistoryTransformPropertyName,
				MODEL_HISTORY_TRANSFORM_DEFAULT_URI );

		try
		{
			modelHistoryTransform = newTemplates(
					FileUtils
							.getFileOrResourceAsReader(
									null,
									modelHistoryTransformUri ),
					FileUtils
							.resolvePath( null, modelHistoryTransformUri )
							.toURI()
							.toURL()
							.toExternalForm()
							);

			logger.info( format( "Installed %smodel history templates: key=[%s], url=[%s].",
					(MODEL_HISTORY_TRANSFORM_DEFAULT_URI.equals( modelHistoryTransformUri ) ? "default " : ""),
					modelHistoryTransformPropertyName,
					modelHistoryTransformUri
		) );
		}
		catch ( Exception e )
		{
			throw new RuntimeException( format( "Failed installing %smodel history templates: key=[%s], url=[%s].",
					(MODEL_HISTORY_TRANSFORM_DEFAULT_URI.equals( modelHistoryTransformUri ) ? "default " : ""),
					modelHistoryTransformPropertyName,
					modelHistoryTransformUri
	), e );
		}

	}

	private void updateHistoryPane( Runnable... callbacks )
	{
		final String[] historyText = { null };

		try
		{
			if ( historyTransformSwitch.isSelected() )
			{
				Map< String, Object > parameters = new HashMap<>();

				parameters.put(
						"root",
						context
								.getSession( sessionKey )
								.getModel()
								.getName() );

				historyText[ 0 ] = transformToText(
						modelHistoryTransform,
						history,
						parameters );
			}
			else
			{
				historyText[ 0 ] = serialize( history, true, true );
			}

			Platform.runLater( () -> {
				historyPane.setText( historyText[ 0 ] );

				if ( (callbacks != null) && (callbacks.length > 0) )
				{
					Arrays
							.stream( callbacks )
							.forEach( Runnable::run );
				}
			} );
		}
		catch ( Exception e )
		{
			logger.warn( "Failed to transform history to XML", e );
		}
	}

	//
	// private final EventHandler<ActionEvent> actionSynchDriverMask = new
	// EventHandler< ActionEvent >()
	// {
	// @Override
	// public void handle( ActionEvent event )
	// {
	// GuiSession session = sessionPane.getGuiSession();
	//
	// if ( session == null || session.getDriver() == null )
	// {
	// return;
	// }
	//
	// Inspector.execute( "Synch. Driver Mask", () ->
	// {
	// session
	// .getDriver()
	// .notifyAWTEventMask(
	// Long.valueOf( eventMaskText.getText()
	// ) );
	// } );
	// }
	// };

	private boolean isGuiEventType( String type )
	{
		switch ( type )
		{
			case "awt-event":
			case "dom-event":
			case "fx-event":
				return true;
		}
		return false;
	}

	private final EventHandler< ActionEvent > actionNotifications = new EventHandler< ActionEvent >()
	{

		@Override
		public void handle( ActionEvent event )
		{
			if ( event.getSource() instanceof CheckBox )
			{
				CheckBox cb = ( CheckBox ) event.getSource();

				Inspector.execute( "Set notifications", () -> {
					GuiSession session = context.getSession( sessionKey );

					if ( (session == null) || (session.getDriver() == null) )
					{
						logger.debug( format( "No session for key [%s]!", sessionKey ) );
						return;
					}

					if ( cb.isSelected() )
					{
						// I want to listen to myself so I can show the text
						addGuiEventListener( EventPane.this );

						// I want to evict events with duplicate seq attributes
						final int numToRemember = 10;

						final LinkedHashMap< Long, String > seqDupeEvictor = new LinkedHashMap< Long, String >()
						{
							private static final long serialVersionUID = 6009268981713209420L;

							@Override
							protected boolean removeEldestEntry( Map.Entry< Long, String > eldest )
							{
								return this.size() > numToRemember;
							}
						};

						session
								.getDriver()
								.addNotificationListener( ( notification, handback ) -> {
									if ( !isGuiEventType( notification.getType() ) )
									{
										// ignoring anything that isn't a gui event type
										return;
									}

									// dupes happen at the same time!
									synchronized ( seqDupeEvictor )
									{
										if ( seqDupeEvictor
												.containsKey(
														notification
																.getSequenceNumber() ) )
										{
											// evicting duplicate
											return;
										}

										seqDupeEvictor.put(
												notification.getSequenceNumber(),
												"archetype" );
									}

									Inspector.execute(
											"Notify GUI Event Listeners: " + notification.getSequenceNumber(),
											() -> {

												String HASH_PATH = "//*/*[ @hash ][ 1 ]/@hash";

												String message = notification.getMessage();

												try
												{
													final Document document = parse( message );

													Element de = document.getDocumentElement();

													long seq = Long.valueOf( de.getAttribute( "seq" ) );

													String eventId = de.getAttribute( "id" );
													String params = de.getAttribute( "params" );

													String em = eventIdFilterText.getText();

													if ( !matches( StringUpcaster.upcastSet( em ), eventId ) )
													{
														if ( logger.isTraceEnabled() )
														{
															logger.trace( format( "Filter [%s] rejected event: id=[%s]; seq=[%s], params=[%s].",
																	em,
																	eventId,
																	seq,
																	params ) );
														}

														return;
													}

													final String targetHash = XPathUtils
															.getCompiledPath( HASH_PATH )
															.evaluate( document );

													if ( (targetHash == null) || targetHash.isEmpty() )
													{
														if ( logger.isDebugEnabled() )
														{
															logger.debug( format( "No hash on event: id=[%s]; seq=[%s], params=[%s].",
																	eventId,
																	seq,
																	params ) );
														}

														return;
													}

													if ( logger.isTraceEnabled() )
													{
														logger.trace( format( "Accepting event; hash=[%s], id=[%s], seq=[%s], param=[%s].",
																targetHash,
																eventId,
																seq,
																params ) );
													}

													ModelMember mm = (modelTreePane == null)
															? null
															: modelTreePane.findModelMemberByHash( targetHash );

													if ( (mm != null) && mm.isObject() )
													{
														ModelObject mo = ( ModelObject ) mm;

														// put a adapter element inside the parsed event element
														Element me = document.createElement( "model" );

														de.appendChild( me );

														me.setAttribute( "name", mo.name() );

														// don't use the name "ancestor" in XML
														// as it conflicts with the axis specifier
														me.setAttribute( "parent", mo.ancestor().fullname() );
														me.setAttribute( "actions", "" + mo.get( "$actions" ) );

														// while we're at it
														// so formats nicely
														removeTrimmedEmptyTextNodes( document );

														String hm = historyMaskText.getText();

														if ( matches( StringUpcaster.upcastSet( hm ), eventId ) )
														{
															addEventToHistory( document );

															if ( logger.isTraceEnabled() )
															{
																logger.trace( format( "Stored event; hash=[%s], id=[%s], seq=[%s].",
																		targetHash,
																		eventId,
																		seq ) );
															}
														}
													}
													else
													{
														if ( logger.isTraceEnabled() )
														{
															logger.trace( format( "Unidentified object, not in tree; hash=[%s], id=[%s], seq=[%s], params=[%s].",
																	targetHash,
																	eventId,
																	seq,
																	params ) );
														}
													}

													notifyGuiEventListeners( serialize( document ) );

												}
												catch ( Exception e )
												{
													throw new RuntimeException( e );
												}

											} );
								} );
					}
					else
					{
						// TODO:
						// I'm in charge - and I'm exclusive
						// It's my session
						session
								.getDriver()
								.removeAllNotificationListeners();

						// I don't want to listen to myself anymore
						removeGuiEventListener( EventPane.this );
					}
				} );
			}
		}
	};

	private boolean matches( Set< String > filter, String eventId )
	{
		if ( eventId == null )
		{
			return false;
		}
		else if ( (filter == null) || filter.isEmpty() )
		{
			return false;
		}
		else if ( (filter.size() == 1) && STAR.equals( filter.iterator().next() ) )
		{
			return true;
		}

		for ( String id : filter )
		{
			if ( eventId.equals( id ) )
			{
				return true;
			}
		}

		return false;
	}

	private void addEventToHistory( Document document )
	{
		synchronized ( history )
		{
			if ( document != null )
			{
				history
						.getDocumentElement()
						.appendChild(
								history
										.importNode(
												document
														.getDocumentElement(),
												true ) );

				// while we're at it
				// so formats nicely
				removeTrimmedEmptyTextNodes( history );
			}
		}

		updateHistoryPane();
	}

	// private void trackMouseClick( String message, String mouseTrackMode )
	// {
	// // share them all
	// notifyGuiEventListeners( message );
	//
	//
	// Document document = parse( message );
	//
	// Element element = document.getDocumentElement();
	//
	//
	// if ( "awt-event".equalsIgnoreCase( element.getTagName() ) )
	// {
	// if ( element.hasAttribute( "id" ) )
	// {
	// if ( mouseTrackMode.equalsIgnoreCase( element.getAttribute( "id" ) ) )
	// {
	// // scoot to the first child element
	// // there will be text nodes...
	// Node fc = element.getFirstChild();
	//
	// while ( fc != null && ! ( fc instanceof Element ) )
	// {
	// fc = fc.getNextSibling();
	// }
	//
	// if ( fc == null || ! ( fc instanceof Element ) )
	// {
	// // not tracked
	// return;
	// }
	//
	//
	// Element mouseClickElement = ( Element ) fc;
	//
	// long hash = Long.valueOf( mouseClickElement.getAttribute( "hash" ) );
	//
	// if ( logger.isDebugEnabled() )
	// {
	// logger.debug( format( "Tracking mouse click: hash=[%s].", hash ) );
	// }
	//
	//
	// Platform.runLater( () ->
	// {
	//
	// eventPane.setText( message );
	//
	// /*
	// final String currentSnapshotText = historyPane.getText();
	//
	// if ( currentSnapshotText == null || currentSnapshotText.isEmpty() )
	// {
	// if ( logger.isInfoEnabled())
	// {
	// logger.info( format( "Current snapshot is empty!" ) );
	// }
	//
	// return;
	// }
	//
	// Inspector.execute( "identify target by hash", () -> {
	//
	// try
	// {
	// Document snapshot = XmlUtils.parse( currentSnapshotText );
	//
	// String hashXPath = format( "//*[@hash='%s']", hash );
	//
	// Node node = (Node) XPathUtils
	// .getCompiledPath( hashXPath )
	// .evaluate( snapshot, XPathConstants.NODE );
	//
	//
	// if ( node == null )
	// {
	// logger.debug( format(
	// "No node found in snapshot for path [%s]: %s",
	// hashXPath,
	// message ) );
	// return;
	// }
	//
	//
	// List< String > paths = getPathsForNode( node );
	//
	// if ( paths != null && paths.size() > 0 )
	// {
	// String recommendedXPath = paths.get( 0 );
	//
	//
	// logger.debug( format(
	// "Updating recommended xpath [%s] for hash [%s].",
	// recommendedXPath,
	// hash ) );
	//
	// Platform.runLater( () -> {
	//
	// // add to the observable list
	// candidateSelections.clear();
	// candidateSelections.addAll( paths );
	//
	// // select the first one
	// candidateSelectionsComboBox
	// .getSelectionModel()
	// .select( 0 );
	//
	//
	// Pattern p = Pattern.compile( format(
	// "<\\w+( \\w+=\"[^\"]*\")* hash=\"%s\"( \\w+=\"[^\"]*\")*\\s*(/>|\\s*>)",
	// hash ) );
	//
	// Matcher m = p.matcher( historyPane.getText() );
	//
	// if ( m.find() )
	// {
	// historyPane.selectRange(
	// m.start(),
	// m.end() );
	//
	// if ( synchXPathOriginCheckBox.isSelected() )
	// {
	// snapshotXPathTextField.setText( recommendedXPath );
	// snapshotXPathCheckBox.setSelected( true );
	// historyPane.setText(
	// getSnapshotFromOrigin(
	// recommendedXPath,
	// currentSnapshotText ) );
	//
	// }
	// }
	// } );
	// }
	//
	// }
	// catch ( Exception e )
	// {
	// logger.warn( format(
	// "Failed to track mouse event: mode=[%s];%n%s",
	// mouseTrackMode,
	// message ),
	// e );
	// }
	//
	//
	// } );
	// */
	//
	// } );
	// }
	// }
	// }
	// }

	// private String getSnapshotFromOrigin( String originXPath, String snapshotText
	// )
	// {
	//
	// try
	// {
	// Node locusNode = ( Node ) XPathUtils
	// .getCompiledPath( originXPath )
	// .evaluate(
	// parse( snapshotText ),
	// XPathConstants.NODE );
	//
	// if ( locusNode != null )
	// {
	// removeTrimmedEmptyTextNodes( locusNode );
	//
	// // TODO:
	// // annoyingly the xml-decl doesn't get a line break following it
	// // presumably as a result of trimming all empty text nodes
	// return serialize( locusNode, true, false );
	// }
	//
	// return serialize( null );
	// }
	// catch ( Exception e )
	// {
	// return Inspector.stackTraceToString( e );
	// }
	// }

	// private List< String > getPathsForNode( Node node )
	// {
	// List< String > paths = new ArrayList< String >();
	//
	// paths.add( XPathUtils.Node2XPathFunctions.getIndexedXPath( node ) );
	// paths.add( XPathUtils.Node2XPathFunctions.getSimpleXPath( node ) );
	//
	//
	// return paths;
	// }

	/**
	 * Receive notification of GUI event from the harness system..
	 *
	 * @param message
	 */
	@Override
	public void receive( String message )
	{
		if ( message == null )
		{
			return;
		}

		Platform.runLater( () -> {
			eventPane.setText( message );
		} );
	}

	protected List< GuiEventListener > remoteListeners = new ArrayList< >();

	private void notifyGuiEventListeners( String notification )
	{
		if ( notification == null )
		{
			return;
		}

		for ( GuiEventListener l : remoteListeners )
		{
			l.receive( notification );
		}
	}

	public void addGuiEventListener( GuiEventListener l )
	{
		remoteListeners.add( l );

		logger.info( "Added GuiEventListener: " + l );

	}

	public void removeGuiEventListener( GuiEventListener l )
	{
		remoteListeners.remove( l );

		logger.info( "Removed GuiEventListener: " + l );
	}

	public void removeAllGuiEventListeners()
	{
		remoteListeners.clear();
	}

	public void setModelTreePane( ModelTreePane modelTreePane )
	{
		this.modelTreePane = modelTreePane;
	}

	public void setContext( Context context )
	{
		this.context = context;
	}
}
