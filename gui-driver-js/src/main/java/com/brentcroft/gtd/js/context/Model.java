package com.brentcroft.gtd.js.context;

import com.brentcroft.util.FileUtils;
import com.brentcroft.util.CommentedProperties;
import java.io.File;

import static java.lang.String.format;

/**
 * Created by Alaric on 17/03/2017.
 */
public class Model
{
    private File workingDir;
    private CommentedProperties properties;

    private String name;
    private String xmlUri;
    private String jsonUri;

    // provided resources
    private String xslPreReducerUri = "xslt/reducer.jstl";
    private String xslReducerUri = "xslt/reducer.jstl";
    private String xsl2JsonModelUri = "xslt/generate-model.xslt";


    private final String xml;
    private final String json;

    public Model( File workingDir, String name, String xmlUri, String jsonUri )
    {
        this.name = name;
        this.xmlUri = xmlUri;
        this.jsonUri = jsonUri;

        this.workingDir = workingDir;

        this.xml = ( xmlUri == null )
                ? null
                : FileUtils
                        .getFileOrResourceAsString(
                                workingDir,
                                xmlUri );
        this.json = ( jsonUri == null )
                ? null
                : FileUtils
                        .getFileOrResourceAsString(
                                workingDir,
                                jsonUri );

        if ( xml == null && json == null )
        {
            throw new NullPointerException( "Both xml and json cannot be null!" );
        }
    }


    public Model withXslReducerUri( String uri )
    {
        this.xslReducerUri = uri;
        return this;
    }


    public Model withXslPreReducerUri( String uri )
    {
        this.xslPreReducerUri = uri;
        return this;
    }

    public Model withXsl2JsonModelUri( String uri )
    {
        this.xsl2JsonModelUri = uri;
        return this;
    }


    public String toString()
    {
        return format(
                "name:        %s%n" +
                "xmlUri:      %s%n" +
                "reducerUri:  %s%n" +
                "jsonUri:     %s",
                name,
                xmlUri + ( xml == null ? "" : " (loaded)" ),
                xslReducerUri,
                jsonUri + ( json == null ? "" : " (loaded)" )
        );
    }


    public String getName()
    {
        return name;
    }

    public String getXml()
    {
        return xml;
    }

    public String getXmlUri()
    {
        return xmlUri;
    }

    public String getJson()
    {
        return json;
    }

    public String getJsonUri()
    {
        return jsonUri;
    }

    public String getXslPreReducerUri()
    {
        return xslPreReducerUri;
    }

    public String getXslReducerUri()
    {
        return xslReducerUri;
    }

    public String getXsl2JsonModelUri()
    {
        return xsl2JsonModelUri;
    }

    public void setProperties( CommentedProperties properties )
    {
        this.properties = properties;
    }

    public CommentedProperties getProperties()
    {
        return properties;
    }
}
