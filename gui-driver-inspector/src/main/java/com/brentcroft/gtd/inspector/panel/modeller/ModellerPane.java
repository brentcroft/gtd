package com.brentcroft.gtd.inspector.panel.modeller;


import static com.brentcroft.gtd.driver.Backend.XML_NAMESPACE_TAG;
import static com.brentcroft.gtd.driver.Backend.XML_NAMESPACE_URI;
import static com.brentcroft.util.StringUpcaster.upcast;
import static com.brentcroft.util.XmlUtils.addXmlnsPrefixNamespaceDeclaration;
import static com.brentcroft.util.XmlUtils.copyNodeToNewDocument;
import static com.brentcroft.util.XmlUtils.removeTrimmedEmptyTextNodes;
import static com.brentcroft.util.XmlUtils.serialize;
import static com.brentcroft.util.XmlUtils.transformToText;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.brentcroft.gtd.inspector.ContextManager;
import com.brentcroft.gtd.inspector.Inspector;
import com.brentcroft.gtd.utilities.KeyUtils;
import com.brentcroft.gtd.utilities.NameUtils;
import com.brentcroft.util.NodeVisitor;
import com.brentcroft.util.Pipes;
import com.brentcroft.util.Pipes.Pipe;
import com.brentcroft.util.TextUtils;
import com.brentcroft.util.XmlUtils;

import javafx.application.Platform;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

@SuppressWarnings( "restriction" )
public class ModellerPane extends AbstractModellerPane
{
    private Map< String, Object > parameters = new HashMap< String, Object >();
    private boolean logPipelineMessages = false;

    public ModellerPane( ContextManager contextManager, String sessionKey, EventPane ep )
    {
        super( 10, contextManager, sessionKey, ep );

        pipe = buildPipeline();


        parameters.put( "adapter-name", context
                .getSession( sessionKey )
                .getModel()
                .getName() );


        parameters.put( "tx", TextUtils.getTranslations() );


        snapshotButton.setOnAction( ( event ) -> {

                    Inspector.execute(
                            "Snap",
                            () -> {
                                try
                                {
                                    String text = context
                                            .getSession( sessionKey )
                                            .getDriver()
                                            .getSnapshotXmlText();

                                    Document document = XmlUtils.parse( text );

                                    Element de = document.getDocumentElement();

                                    if ( de.hasAttribute( "seq" ) && de.hasAttribute( "timestamp" ) )
                                    {
                                        long seq = upcast( de.getAttribute( "seq" ), - 1L );
                                        String ts = de.getAttribute( "timestamp" );

                                        Platform.runLater( () -> historyPane.addItem( seq, ts, text, true ) );
                                    }

                                    snap( document );
                                }
                                catch ( Exception e )
                                {
                                    Inspector.errorAlert( e );
                                }
                            } );
                }
        );


        rerunHistoryButton.setOnAction( e -> {

            setRunningDisabled();

            Inspector.execute(
                    "Rerun history",
                    () -> {
                        try
                        {
                            historyPane.replayAll();
                        }
                        finally
                        {
                            setStoppedEnabled();
                        }
                    } );
        } );
    }



    private static int numPipes = 8;

    private void setProgress( int step )
    {
        setProgress( step, numPipes );
    }

    @SuppressWarnings( "unchecked" )
	private Pipe< String, ScriptObjectMirror > jsonToModelObjectPipe = Pipes.newPipe( String.class, ScriptObjectMirror.class )
            .withName( "Model" )
            .withConditionIn( ( in ) -> in != null )
            .withProcessor( ( in ) -> context.generateModel( getSession(), in ) )
            .withListeners(
                    ( n ) -> setProgress( 7 ),
                    ( som ) -> modelTreePane.buildSOMTree( som ),
                    // update local context
                    ( som ) -> context.getBindings().put( getSession().getModel().getName(), som ) )
            .withLogMessages( logPipelineMessages );


    @SuppressWarnings( "unchecked" )
	private Pipe< Node, String > xmlToJsonPipe = Pipes.newPipe( Node.class, String.class )
            .withName( "Jsonator" )
            .withConditionIn( ( in ) -> in != null && jsonTemplates != null )
            .withProcessor( ( in ) -> jstl.expandText(
                    transformToText(
                            jsonTemplates,
                            in,
                            parameters ),
                    parameters ) )
            .withListeners(
                    ( n ) -> setProgress( 6 ),
                    jsonToModelObjectPipe,
                    ( out ) -> updateTextArea( jsonModelPane, out )
            )
            .withLogMessages( logPipelineMessages );


    @SuppressWarnings( "unchecked" )
	private Pipe< Node, Node > masteringPipe = Pipes.newPipe( Node.class, Node.class )
            .withName( "Master" )
            .withConditionIn( ( in ) -> in != null )
            .withProcessor( ( in ) -> masterDocument == null || ! masterCheckBox.isSelected()
                    ? in
                    : removeTrimmedEmptyTextNodes(
                            mergeAccumulateToMaster(
                                    copyNodeToNewDocument( masterDocument.getDocumentElement() ),
                                    XmlUtils.getDocument( in ) )
                    ) )
            .withListeners(
                    ( n ) -> setProgress( 5 ),
                    xmlToJsonPipe,
                    ( out ) -> updateTextArea( xmlTextArea, serialize( out, true, false ) )
            )
            .withLogMessages( logPipelineMessages );


    @SuppressWarnings( "unchecked" )
	private Pipe< Node, Node > accPipe = Pipes.newPipe( Node.class, Node.class )
            .withName( "Accumulator" )
            .withConditionIn( ( in ) -> in != null )
            .withProcessor( ( in ) -> accumulatedSnapshot == null || ! accumulateCheckBox.isSelected()
                    ? in
                    : mergeSnapshotToAccumulate(
                            accumulatedSnapshot,
                            XmlUtils.getDocument( in )
                    ) )
            .withListeners(
                    ( n ) -> setProgress( 4 ),
                    masteringPipe )
            .withLogMessages( logPipelineMessages );


    @SuppressWarnings( "unchecked" )
	private Pipe< Node, Node > reducingPipe = Pipes.newPipe( Node.class, Node.class )
            .withName( "Reducer" )
            .withConditionIn( Objects::nonNull )
            .withProcessor( ( in ) -> ( reducer == null || ! reduceTransformCheckBox.isSelected() )
                    ? in
                    : removeTrimmedEmptyTextNodes(
                            addXmlnsPrefixNamespaceDeclaration(
                                    reducer.reduce( in ),
                                    XML_NAMESPACE_TAG,
                                    XML_NAMESPACE_URI ) ) )
            .withListeners(
                    ( n ) -> setProgress( 3 ),
                    accPipe )
            .withLogMessages( logPipelineMessages );


    @SuppressWarnings( "unchecked" )
	private Pipe< Node, Node > integratorPipe = Pipes.newPipe( Node.class, Node.class )
            .withName( "integrator" )
            .withConditionIn( Objects::nonNull )
            .withProcessor( in -> ! integratorCheckBox.isSelected()
                    ? in
                    : new NodeVisitor()
                    {
                        KeyUtils keyUtils = getKeyUtils().preCache( in );

                        @Override
                        public boolean open( Node node )
                        {
                            if ( node instanceof Document )
                            {
                                return true;
                            }
                            else if ( node.getOwnerDocument() == node.getParentNode() )
                            {
                                // don't do the document element!
                                return true;
                            }
                            else if ( ! ( node instanceof Element ) )
                            {
                                return false;
                            }
                            else
                            {
                                // NB: using client name
                                final String xpath = XmlUtils
                                        .disambiguateAttr(
                                                ( Element ) node,
                                                XML_NAMESPACE_URI,
                                                "xpath",
                                                keyUtils.calculateKeyRaw( ( Element ) node ),
                                                "[%s]" );

                                XmlUtils
                                        .maybeSetElementAttribute(
                                                ( Element ) node,
                                                XML_NAMESPACE_URI,
                                                "a:xpath",
                                                xpath );


                                keyUtils
								        .getNameUtils();
								// NB: using client name
                                final String name = XmlUtils
                                        .disambiguateAttr(
                                                ( Element ) node,
                                                XML_NAMESPACE_URI,
                                                "name",
                                                NameUtils
                                                        .cleanName(
                                                                keyUtils
                                                                        .getNameUtils()
                                                                        .buildName( ( Element ) node ) ),
                                                "%s" );

                                XmlUtils
                                        .maybeSetElementAttribute(
                                                ( Element ) node,
                                                XML_NAMESPACE_URI,
                                                "a:name",
                                                name );

                                return true;
                            }

                        }
                    }.visit( in ) )
            .withListeners(
                    ( n ) -> maybeGenerateTxFile(),
                    ( n ) -> setProgress( 2 ),
                    reducingPipe )
            .withLogMessages( logPipelineMessages );


    @SuppressWarnings( "unchecked" )
	private Pipe< Node, Node > preReducingPipe = Pipes.newPipe( Node.class, Node.class )
            .withName( "Pre-reducer" )
            .withConditionIn( Objects::nonNull )
            .withProcessor( ( in ) -> ( preReducer == null || ! preReduceCheckBox.isSelected() )
                    ? in
                    : removeTrimmedEmptyTextNodes(
                            addXmlnsPrefixNamespaceDeclaration(
                                    preReducer.reduce( in ),
                                    XML_NAMESPACE_TAG,
                                    XML_NAMESPACE_URI ) ) )
            .withListeners(
                    ( n ) -> setProgress( 1 ),
                    integratorPipe )
            .withLogMessages( logPipelineMessages );


    @SuppressWarnings( "unchecked" )
	private Pipe< Node, Node > buildPipeline()
    {
        return Pipes.newPipe( Node.class, Node.class )
                .withName( "Snapper" )
                .withConditionIn( ( in ) -> in != null )
                .withProcessor( ( in ) -> in )
                .withListeners( preReducingPipe )
                .withLogMessages( logPipelineMessages );
    }


}
