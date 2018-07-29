package com.brentcroft.gtd.js.context;

import com.brentcroft.util.FileUtils;
import com.brentcroft.util.XmlUtils;
import com.brentcroft.util.templates.JstlTemplateManager;
import com.brentcroft.util.templates.el.StandardELFilter;
import java.io.File;
import java.util.Map;
import javax.xml.transform.Templates;

import static com.brentcroft.util.XmlUtils.parse;
import static com.brentcroft.util.XmlUtils.transformToText;
import static java.lang.String.format;

/**
 * Created by Alaric on 17/03/2017.
 */
public class Modeller
{
    private final JstlTemplateManager jstl = new JstlTemplateManager()
            .withStripComments( true )
            .withELFilter( StandardELFilter.HTML_ESCAPE_FILTER );

    @SuppressWarnings( "unused" )
	private final File root;
    private final String xsltUri;
    private final String adapterBaseUri;
    private final String adapterUri;

    // could be reloaded
    private Templates xslt;
    private String adapterBase;
    private String adapter;

    public Modeller( File root, String xsltUri, String adapterBaseUri, String adapterUri )
    {
        this.root = root;
        this.xsltUri = xsltUri;
        this.adapterBaseUri = adapterBaseUri;
        this.adapterUri = adapterUri;

        try
        {
            this.xslt = XmlUtils.newTemplates(
                    FileUtils
                            .getFileOrResourceAsReader(
                                    root,
                                    xsltUri ),
                    FileUtils
                            .resolvePath( root, xsltUri )
                            .toURI()
                            .toURL()
                            .toExternalForm()
            );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( format( "Failed to load model transform [%s]", xsltUri ), e );
        }

        this.adapterBase = ( adapterBaseUri == null )
                ? null
                : FileUtils
                        .getFileOrResourceAsString(
                                root,
                                adapterBaseUri );

        this.adapter = ( adapterUri == null )
                ? null
                : FileUtils
                        .getFileOrResourceAsString(
                                root,
                                adapterUri );
    }


    public String toString()
    {
        StringBuffer b = new StringBuffer();

        b
                .append( "xsltUri:    " )
                .append( xsltUri )
                .append( xslt == null ? "" : " (loaded)" )
                .append( "\n" );

        b
                .append( "adapterBaseUri: " )
                .append( adapterBaseUri )
                .append( adapterBase == null ? "" : " (loaded)" )
                .append( "\n" );
        b
                .append( "adapterUri: " )
                .append( adapterUri )
                .append( adapter == null ? "" : " (loaded)" )
                .append( "\n" );

        return b.toString();
    }

    public String getXsltUri()
    {
        return xsltUri;
    }


    public String getAdapterBase()
    {
        return adapterBase;
    }

    public String adaptJsonToModelScript( String modelJson )
    {
        return adapter.replace( "GUI_OBJECT_MODEL", modelJson );
    }


    // passing in a url for the file or resource
    // so relative references work in JSTL tags
    public String expandModelToJson( String modelXmlText, String urlRef, Map< String, Object > parameters )
    {
        String translatedMasterText = jstl
                .expandText(
                        modelXmlText,
                        urlRef,
                        parameters );

        return transformToText(
                xslt,
                parse( translatedMasterText ),
                parameters );
    }


    public String expandModel( String modelXmlText, String urlRef, Map< String, Object > parameters )
    {
        return adaptJsonToModelScript(
                expandModelToJson(
                        modelXmlText,
                        urlRef,
                        parameters ) );
    }
}
