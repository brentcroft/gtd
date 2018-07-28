package com.brentcroft.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;

/**
 * The CommentedProperties class is an extension of java.util.Properties to
 * allow retention of comment lines and blank (whitespace only) lines in the
 * properties file.
 * <p>
 * Thanks waltf:
 * http://www.dreamincode.net/forums/topic/53734-java-code-to-modify
 * -properties-file-and-preserve-comments/
 */
public class CommentedProperties extends Properties
{

    private static final long serialVersionUID = 7956992219703870545L;

    /**
     * Use a Vector to keep a copy of lines that are a comment or 'blank'
     */
    private List< String > lineData = new ArrayList<>();

    /**
     * Use a Vector to keep a copy of lines containing a key, i.e. they are a
     * property.
     */
    private List< String > keyData = new ArrayList<>();


    private String newLine = "\n";

    public CommentedProperties()
    {
        this( null );
    }

    public CommentedProperties( Properties parent )
    {
        super( parent );
    }


    /**
     * Use the specified string as the newLine separator.
     *
     * @param newLine
     * @return
     */
    public CommentedProperties withNewLine( String newLine )
    {
        this.newLine = newLine;
        return this;
    }


    @Override
    public void clear()
    {
        lineData.clear();
        keyData.clear();
        super.clear();
    }


    public void replaceWith( CommentedProperties cp )
    {
        lineData.clear();
        keyData.clear();
        super.clear();

        lineData.addAll( cp.getLineData() );
        keyData.addAll( cp.getKeyData() );
        super.putAll( cp );
    }


    /**
     * Load properties from the specified Reader. Overload the load method in
     * Properties so we can keep comment and blank lines.
     *
     * @param r The Reader to read.
     */
    @Override
    public void load( Reader r ) throws IOException
    {
        final BufferedReader reader = ( r instanceof BufferedReader ) ? ( BufferedReader ) r : new BufferedReader( r );

        String line;

        try
        {

            while ( ( line = reader.readLine() ) != null )
            {
                char c = 0;
                int pos = 0;

                // Leading whitespaces must be deleted first.
                while ( pos < line.length() && Character.isWhitespace( c = line.charAt( pos ) ) )
                {
                    pos++;
                }

                // If empty line or begins with a comment character
                if ( ( line.length() - pos ) == 0
                     || line.charAt( pos ) == '#' || line.charAt( pos ) == '!' )
                {
                    addLine( line );
                    continue;
                }

                // The characters up to the next Whitespace, ':', or '='
                // describe the key. But look for escape sequences.
                // Try to short-circuit when there is no escape char.
                int start = pos;

                boolean needsEscape = line.indexOf( '\\', pos ) != - 1;

                final StringBuilder key = needsEscape ? new StringBuilder() : null;

                while ( pos < line.length()
                        && ! Character.isWhitespace( c = line.charAt( pos++ ) )
                        && c != '=' && c != ':' )
                {
                    if ( needsEscape && c == '\\' )
                    {
                        if ( pos == line.length() )
                        {
                            // The line continues on the next line. If there
                            // is no next line, just treat it as a key with an
                            // empty value.
                            line = reader.readLine();

                            if ( line == null )
                            {
                                line = "";
                            }

                            pos = 0;

                            while ( pos < line.length() && Character.isWhitespace( c = line.charAt( pos ) ) )
                            {
                                pos++;
                            }
                        }
                        else
                        {
                            c = line.charAt( pos++ );

                            switch ( c )
                            {
                                case 'n':
                                    key.append( '\n' );
                                    break;
                                case 't':
                                    key.append( '\t' );
                                    break;
                                case 'r':
                                    key.append( '\r' );
                                    break;
                                case 'u':
                                    if ( pos + 4 <= line.length() )
                                    {
                                        char uni = ( char ) Integer.parseInt( line.substring( pos, pos + 4 ), 16 );
                                        key.append( uni );
                                        pos += 4;
                                    } // else throw exception?
                                    break;
                                default:
                                    key.append( c );
                                    break;
                            }
                        }
                    }
                    else if ( needsEscape )
                    {
                        key.append( c );
                    }
                }

                boolean isDelim = ( c == ':' || c == '=' );

                String keyString;

                if ( needsEscape )
                {
                    keyString = key.toString();
                }
                else if ( isDelim || Character.isWhitespace( c ) )
                {
                    keyString = line.substring( start, pos - 1 );
                }
                else
                {
                    keyString = line.substring( start, pos );
                }

                while ( pos < line.length() && Character.isWhitespace( c = line.charAt( pos ) ) )
                {
                    pos++;
                }

                if ( ! isDelim && ( c == ':' || c == '=' ) )
                {
                    pos++;
                    while ( pos < line.length() && Character.isWhitespace( c = line.charAt( pos ) ) )
                    {
                        pos++;
                    }
                }

                // Short-circuit if no escape chars found.
                if ( ! needsEscape )
                {
                    add( keyString, line.substring( pos ) );
                    continue;
                }

                // Escape char found so iterate through the rest of the line.
                final StringBuilder element = new StringBuilder( line.length() - pos );

                while ( pos < line.length() )
                {
                    c = line.charAt( pos++ );

                    if ( c == '\\' )
                    {
                        if ( pos == line.length() )
                        {
                            // The line continues on the next line.
                            line = reader.readLine();

                            // We might have seen a backslash at the end of
                            // the file. The JDK ignores the backslash in
                            // this case, so we follow for compatibility.
                            if ( line == null )
                            {
                                break;
                            }

                            pos = 0;

                            while ( pos < line.length()
                                    && Character.isWhitespace( c = line.charAt( pos ) ) )
                            {
                                pos++;
                            }

                            element.ensureCapacity( line.length() - pos + element.length() );
                        }
                        else
                        {
                            c = line.charAt( pos++ );

                            switch ( c )
                            {
                                case 'n':
                                    element.append( '\n' );
                                    break;
                                case 't':
                                    element.append( '\t' );
                                    break;
                                case 'r':
                                    element.append( '\r' );
                                    break;
                                case 'u':
                                    if ( pos + 4 <= line.length() )
                                    {
                                        char uni = ( char ) Integer.parseInt
                                                ( line.substring( pos, pos + 4 ), 16 );
                                        element.append( uni );
                                        pos += 4;
                                    } // else throw exception?
                                    break;
                                default:
                                    element.append( c );
                                    break;
                            }
                        }
                    }
                    else
                    {
                        element.append( c );
                    }
                }

                add( keyString, element.toString() );
            }
        }
        finally
        {
            reader.close();
        }
    }


    /**
     * Load properties from the specified InputStream. Overload the load method
     * in Properties so we can keep comment and blank lines.
     *
     * @param inStream The InputStream to read.
     */
    @Override
    public void load( InputStream inStream ) throws IOException
    {
        // The spec says that the file must be encoded using ISO-8859-1.
        load( new BufferedReader( new InputStreamReader( inStream, "ISO-8859-1" ) ) );
    }


    /**
     * Write the properties to the specified OutputStream.
     * <p>
     * Overloads the store method in Properties so we can put back comment and
     * blank lines.
     *
     * @param out    The OutputStream to write to.
     * @param header Ignored, here for compatability w/ Properties.
     * @throws IOException
     */
    @Override
    public void store( OutputStream out, String header ) throws IOException
    {
        store( new BufferedWriter( new OutputStreamWriter( out, "8859_1" ) ), header );

    }

    /**
     * Default is false
     *
     * @return whether the property "$strict" is "true"
     */
    public boolean isStrict()
    {
        return containsKey( "$strict" ) || StringUpcaster.upcast( getProperty( "$strict" ), false );
    }

    /**
     * Write the properties to the specified OutputStream.
     * <p>
     * Overloads the store method in Properties so we can put back comment and
     * blank lines.
     *
     * @param w      The Writer to write to.
     * @param header Ignored, here for compatability w/ Properties.
     * @throws IOException
     */
    @Override
    public void store( Writer w, String header ) throws IOException
    {
        // The spec says that the file must be encoded using ISO-8859-1.
        final BufferedWriter writer = w instanceof BufferedWriter ? ( BufferedWriter ) w : new BufferedWriter( w );


        // We ignore the header, because if we prepend a commented header
        // then read it back in it is now a comment, which will be saved
        // and then when we write again we would prepend Another header...

        String line;
        String key;
        StringBuilder s = new StringBuilder();

        for ( int i = 0; i < lineData.size(); i++ )
        {
            line = lineData.get( i );
            key = keyData.get( i );
            if ( key.length() > 0 )
            {
                // This is a 'property' line, so rebuild it
                formatForOutput( key, s, true );

                s.append( '=' );

                formatForOutput( ( String ) get( key ), s, false );

                writer.write( s.toString() );

                if ( newLine == null )
                {
                    writer.newLine();
                }
                else
                {
                    writer.write( newLine );
                }
            }
            else
            {
                // was a blank or comment line, so just restore it
                writer.write( line );

                if ( newLine == null )
                {
                    writer.newLine();
                }
                else
                {
                    writer.write( newLine );
                }
            }
        }
        writer.flush();

        writer.close();
    }

    public void putAll( Map< ? extends Object, ? extends Object > properties )
    {
        properties.forEach( ( k, v ) -> setProperty( k.toString(), v.toString() ) );
    }



    public Object setProperty( String key, String value )
    {
        // TODO: can't use containsKey( value )
        // as that won't descend the hierarchy of defaults
        // but annoyingly this gets called unnecessarily many times
        String existing = getProperty( key );

        if ( existing != null )
        {
            return super.setProperty( key, value );
        }

        add( key, value );
        return null;
    }


    public void removeProperty( String name )
    {
        for ( int i = 0; i < lineData.size(); i++ )
        {
            final String key = keyData.get( i );

            if ( key.equals( name ) )
            {
                keyData.set( i, "" );

                final String line = lineData.get( i );

                lineData.set( i, "# removed: " + key + "=" + line );
                return;
            }
        }
    }

    /**
     * Need this method from Properties because original code has StringBuilder,
     * which is an element of Java 1.5, used StringBuffer instead (because this
     * code was written for Java 1.4)
     *
     * @param str    - the string to format
     * @param buffer - buffer to hold the string
     * @param key    - true if str the key is formatted, false if the value is
     *               formatted
     */
    public void formatForOutput( String str, StringBuilder buffer, boolean key )
    {
        if ( key )
        {
            buffer.setLength( 0 );
            buffer.ensureCapacity( str.length() );
        }
        else
        {
            buffer.ensureCapacity( buffer.length() + str.length() );
        }

        if ( ! isStrict() )
        {
            buffer.append( str );
            return;
        }

        boolean head = true;

        for ( int i = 0, size = str.length(); i < size; i++ )
        {
            final char c = str.charAt( i );

            switch ( c )
            {
                case '\n':
                    buffer.append( "\\n" );
                    break;
                case '\r':
                    buffer.append( "\\r" );
                    break;
                case '\t':
                    buffer.append( "\\t" );
                    break;
                case ' ':
                    buffer.append( head ? "\\ " : " " );
                    break;
                case '\\':
                case '!':
                case '#':
                case '=':
                case ':':
                    buffer.append( '\\' ).append( c );
                    break;
                default:
                    if ( c < ' ' || c > '~' )
                    {
                        final String hex = Integer.toHexString( c );
                        buffer.append( "\\u0000".substring( 0, 6 - hex.length() ) );
                        buffer.append( hex );
                    }
                    else
                    {
                        buffer.append( c );
                    }
            }
            if ( c != ' ' )
            {
                head = key;
            }
        }
    }


    public static void formatForInput( String value, StringBuilder buffer )
    {
        buffer.setLength( 0 );

        char c = 0;
        int pos = 0;


        // buffer.setLength( value.length() - pos );

        while ( pos < value.length() )
        {
            c = value.charAt( pos++ );

            if ( c == '\\' )
            {
                c = value.charAt( pos++ );

                switch ( c )
                {
                    case 'n':
                        buffer.append( '\n' );
                        break;

                    case 't':
                        buffer.append( '\t' );
                        break;

                    case 'r':
                        buffer.append( '\r' );
                        break;

                    case 'u':
                        if ( pos + 4 <= value.length() )
                        {
                            char uni = ( char ) Integer.parseInt( value.substring( pos, pos + 4 ), 16 );
                            buffer.append( uni );
                            pos += 4;
                        }
                        // else throw exception?
                        break;

                    default:
                        buffer.append( c );
                        break;
                }
            }
            else
            {
                buffer.append( c );
            }
        }
    }

    /**
     * Add a Property to the end of the CommentedProperties.
     *
     * @param keyString The Property key.
     * @param value     The value of this Property.
     */
    public void add( String keyString, String value )
    {
        put( keyString, value );
        lineData.add( "" );
        keyData.add( keyString );
    }

    /**
     * Add a comment or blank line or comment to the end of the
     * CommentedProperties.
     *
     * @param line The string to add to the end, make sure this is a comment or a
     *             'whitespace' line.
     */
    public void addLine( String line )
    {
        lineData.add( line );
        keyData.add( "" );
    }

    private final static String COMMA = ",";

    /**
     * Downcasts a Properties to a string representation where every entry is on one line.
     *
     * @return a string representation where every entry is on one line
     */
    public String downcast()
    {
        return IntStream
                .range( 0, lineData.size() )
                .mapToObj( i -> {
                    String key = keyData.get( i );
                    String value = getProperty( key );

                    return ( key.length() > 0 )
                            ? format(
                            "%s=%s",
                            key,
                            value.contains( "=" )
                                    ? Stream
                                    .of( value.split( COMMA ) )
                                    .collect( Collectors.joining( COMMA + " \\\n\t" ) )
                                    : value )
                            : lineData.get( i );

                } )
                .collect( Collectors.joining( newLine ) );
    }


    public List< String > getLineData()
    {
        return lineData;
    }

    public List< String > getKeyData()
    {
        return keyData;
    }
}
