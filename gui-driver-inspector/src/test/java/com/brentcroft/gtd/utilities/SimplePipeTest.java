package com.brentcroft.gtd.utilities;

import com.brentcroft.util.FileUtils;
import com.brentcroft.util.Pipes;
import com.brentcroft.util.XmlUtils;
import com.brentcroft.util.templates.JstlTemplateManager;
import com.brentcroft.util.templates.el.StandardELFilter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Templates;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;

/**
 * Created by Alaric on 28/11/2016.
 */
@Ignore
public class SimplePipeTest
{
    private Map< String, Object > parameters = new HashMap< String, Object >();

    private final static JstlTemplateManager JSTL = new JstlTemplateManager()
            .withStripComments( true )
            .withELFilter( StandardELFilter.XML_ESCAPE_FILTER );

    private Document accumulate = null;

    private Document masterDocument = null;
    private Templates reducerTemplates = null;
    private Templates jsonTemplates = null;


    private static final String JSON_TRANSFORM_DEFAULT_URI = "xslt/generate-model.xslt";


    private XmlAccumulator accumulator = null;
    private XmlAccumulator masterator = null;


    @Before
    public void setUp() throws Exception
    {
        accumulate = XmlUtils.parse( "<gui/>" );

        masterDocument = XmlUtils.newDocument( FileUtils
                .getFileOrResourceAsReader(
                        null,
                        "master.xml" ) );

        reducerTemplates = XmlUtils
                .newTemplates(
                        FileUtils
                                .getFileOrResourceAsReader(
                                        null,
                                        "reducer.xslt" ) );


        jsonTemplates = XmlUtils
                .newTemplates(
                        FileUtils
                                .getFileOrResourceAsReader(
                                        null,
                                        JSON_TRANSFORM_DEFAULT_URI ) );


        accumulator = new XmlAccumulator();

        masterator = new XmlAccumulator();
    }


    @SuppressWarnings( "unchecked" )
	@Test
    @Ignore
    public void receivesMessage()
    {
        Pipes.Listener< String > xmlToJsonPipe = Pipes.newStringPipe()
                .withProcessor(
                        ( message ) ->
                        {
                            try
                            {
                                return XmlUtils.transformToText(
                                        jsonTemplates,
                                        XmlUtils.parse( JSTL
                                                .expandText(
                                                        message,
                                                        parameters ) ),
                                        parameters );
                            }
                            catch ( Exception e )
                            {
                                throw new RuntimeException( e );
                            }
                        } )
                .withListeners(
                        ( message ) ->
                        {
                            System.out.println( "object pipe listener: \n" + message );
                        } );


        Pipes.Listener< String > masteringPipe = Pipes.newStringPipe()
                .withProcessor( ( message ) ->
                        {
                            Document masterCopy = XmlUtils
                                    .copyNodeToNewDocument(
                                            masterDocument.getDocumentElement() );

                            masterator
                                    .merge(
                                            masterCopy.getDocumentElement(),
                                            XmlUtils
                                                    .parse( message )
                                                    .getDocumentElement() );

                            return XmlUtils
                                    .serialize(
                                            XmlUtils
                                                    .removeTrimmedEmptyTextNodes( masterCopy ),
                                            true,
                                            false );
                        }
                )
                .withListeners(
                        ( message ) ->
                        {
                            System.out.println( "master pipe listener: \n" + message );
                        },

                        xmlToJsonPipe );

        Pipes.Listener< String > reducingPipe = Pipes.newStringPipe()
                .withProcessor(
                        ( message ) ->
                        {
                            try
                            {
                                return XmlUtils.serialize( XmlUtils
                                        .removeTrimmedEmptyTextNodes(
                                                XmlUtils
                                                        .transform(
                                                                reducerTemplates,
                                                                XmlUtils.parse( message ),
                                                                parameters ) ), true, false );
                            }
                            catch ( Exception e )
                            {
                                throw new RuntimeException( "Error during process!", e );
                            }
                        }
                )
                .withListeners(
                        ( message ) ->
                        {
                            System.out.println( "red pipe listener: \n" + message );
                        },

                        masteringPipe
                );

        Pipes.Listener< String > accPipe = Pipes.newStringPipe()
                .withProcessor(
                        ( message ) ->
                        {
                            accumulator
                                    .merge(
                                            accumulate
                                                    .getDocumentElement(),
                                            XmlUtils
                                                    .parse( message )
                                                    .getDocumentElement() );

                            return XmlUtils.serialize( accumulate, true, false );
                        } )
                .withListeners(
                        ( message ) ->
                        {
                            System.out.println( "acc pipe listener: \n" + message );
                        },
                        reducingPipe
                );


        Pipes.Listener< String > p = Pipes.newStringPipe()
                .withProcessor( ( message ) ->
                {
                    return message;
                } )
                .withListeners(
                        ( message ) ->
                        {
                            System.out.println( "top pipe listener: \n" + message );
                        },
                        accPipe
                );

        p.receive( "<gui><JPanel><JTextField text='test field' red='green' purple='34'/><JTextField text='test field' red='green' purple='34'/></JPanel></gui>" );
    }

}