package com.brentcroft.util.templates.el;


import java.util.function.Function;
import org.apache.commons.text.StringEscapeUtils;

/**
 * Standard EL Filters use the facilities from
 * org.apache.commons.text.StringEscapeUtils to provide escaping filters.
 * <p>
 * NB: UNESCAPE_ESCAPE filters always un-escape before escaping to preclude double
 * escaping.
 */
public enum StandardELFilter implements ELFilter
{
    XML_ESCAPE_FILTER( StringEscapeUtils::escapeXml11 ),

    XML_UNESCAPE_ESCAPE_FILTER( v -> StringEscapeUtils.escapeXml11( StringEscapeUtils.unescapeXml( v ) ) ),

    XML_UNESCAPE_FILTER(StringEscapeUtils::unescapeXml),

    CSV_ESCAPE_FILTER(StringEscapeUtils::escapeCsv),

    CSV_UNESCAPE_ESCAPE_FILTER(v -> StringEscapeUtils.escapeCsv( StringEscapeUtils.unescapeCsv( v ) )),

    HTML_ESCAPE_FILTER(StringEscapeUtils::escapeHtml4),

    HTML_UNESCAPE_ESCAPE_FILTER(v->StringEscapeUtils.escapeHtml4( StringEscapeUtils.unescapeHtml4( v ) )),

    ;

    private Function< String, String > filterFunction;

    StandardELFilter( Function< String, String > filterFunction )
    {
        this.filterFunction = filterFunction;
    }


    public Object filter( Object value )
    {
        return value == null ? null : filterFunction.apply( value.toString() );
    }
}
