package com.brentcroft.gtd.utilities;

import com.brentcroft.util.Waiter8;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import javafx.application.Platform;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.apache.log4j.Logger;

import static java.lang.String.format;

/**
 * @author Yuhi Ishikura
 */
public class TextInputControlStream
{
    // foreign logger may be null
    private final Logger logger;

    private final TextInputControlInputStream in;
    private final TextInputControlOutputStream out;
    private final PrintWriter pw;
    private final Charset charset;

    public TextInputControlStream( final TextInputControl textInputControl, Charset charset, Logger logger )
    {
        this.charset = charset;
        this.in = new TextInputControlInputStream( textInputControl );
        this.out = new TextInputControlOutputStream( textInputControl );
        this.pw = new PrintWriter( out );
        this.logger = logger;

        textInputControl.addEventFilter( KeyEvent.KEY_PRESSED, e ->
        {
            if ( e.getCode() == KeyCode.ENTER )
            {
                getIn().enterKeyPressed();
                return;
            }

            if ( textInputControl.getCaretPosition() <= getIn().getLastLineBreakIndex() )
            {
                e.consume();
            }
        } );
        textInputControl.addEventFilter( KeyEvent.KEY_TYPED, e ->
        {
            if ( textInputControl.getCaretPosition() < getIn().getLastLineBreakIndex() )
            {
                e.consume();
            }
        } );
    }

    public void clear() throws IOException
    {
        this.in.clear();
        this.out.clear();
    }

    TextInputControlStream.TextInputControlInputStream getIn()
    {
        return this.in;
    }

    public TextInputControlStream.TextInputControlOutputStream getOut()
    {
        return this.out;
    }

    public PrintWriter getPrintWriter()
    {
        return pw;
    }


    void startProgramInput()
    {
        // do nothing
    }

    void endProgramInput()
    {
        getIn().moveLineStartToEnd();
    }

    Charset getCharset()
    {
        return this.charset;
    }

    /**
     * @author Yuhi Ishikura
     */
    class TextInputControlInputStream extends InputStream
    {

        private final TextInputControl textInputControl;
        private final PipedInputStream outputTextSource;
        private final PipedOutputStream inputTextTarget;
        private int lastLineBreakIndex = 0;

        public TextInputControlInputStream( TextInputControl textInputControl )
        {
            this.textInputControl = textInputControl;
            this.inputTextTarget = new PipedOutputStream();
            try
            {
                this.outputTextSource = new PipedInputStream( this.inputTextTarget );
            }
            catch ( IOException e1 )
            {
                throw new RuntimeException( e1 );
            }
        }

        int getLastLineBreakIndex()
        {
            return this.lastLineBreakIndex;
        }

        void moveLineStartToEnd()
        {
            this.lastLineBreakIndex = this.textInputControl.getLength();
        }

        void enterKeyPressed()
        {
            synchronized ( this )
            {
                try
                {
                    this.textInputControl.positionCaret( this.textInputControl.getLength() );

                    final String lastLine = getLastLine();
                    final ByteBuffer buf = getCharset().encode( lastLine + "\r\n" );
                    this.inputTextTarget.write( buf.array(), 0, buf.remaining() );
                    this.inputTextTarget.flush();
                    this.lastLineBreakIndex = this.textInputControl.getLength() + 1;
                }
                catch ( IOException e )
                {
                    if ( "Read end dead".equals( e.getMessage() ) )
                    {
                        return;
                    }
                    throw new RuntimeException( e );
                }
            }
        }

        private String getLastLine()
        {
            synchronized ( this )
            {
                return this.textInputControl.getText( this.lastLineBreakIndex, this.textInputControl.getLength() );
            }
        }

        @Override
        public int available() throws IOException
        {
            return this.outputTextSource.available();
        }

        @Override
        public int read() throws IOException
        {
            try
            {
                return this.outputTextSource.read();
            }
            catch ( IOException ex )
            {
                return - 1;
            }
        }

        @Override
        public int read( final byte[] b, final int off, final int len ) throws IOException
        {
            try
            {
                return this.outputTextSource.read( b, off, len );
            }
            catch ( IOException ex )
            {
                return - 1;
            }
        }

        @Override
        public int read( final byte[] b ) throws IOException
        {
            try
            {
                return this.outputTextSource.read( b );
            }
            catch ( IOException ex )
            {
                return - 1;
            }
        }

        @Override
        public void close() throws IOException
        {
            super.close();
        }

        void clear() throws IOException
        {
            this.inputTextTarget.flush();
            this.lastLineBreakIndex = 0;
        }
    }

    /**
     * @author Yuhi Ishikura
     */
    public final class TextInputControlOutputStream extends OutputStream
    {

        private final TextInputControl textInputControl;
        private final CharsetDecoder decoder;
        private ByteArrayOutputStream buf;

        public TextInputControlOutputStream( TextInputControl textInputControl )
        {
            this.textInputControl = textInputControl;
            this.decoder = getCharset().newDecoder();
        }

        @Override
        public synchronized void write( int b )
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
        public void flush()
        {

            maybeInvokeNowOnFXThread( () ->
            {
                try
                {
                    flushImpl();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            } );
        }

        public void maybeInvokeNowOnFXThread( Runnable runnable )
        {
            if ( Platform.isFxApplicationThread() )
            {
                runnable.run();
            }
            else
            {
                final Exception[] exception = { null };
                final Boolean[] completed = { false };

                Platform.runLater( () ->
                {
                    try
                    {
                        runnable.run();
                    }
                    catch ( Exception e )
                    {
                        exception[ 0 ] = e;
                    }
                    finally
                    {
                        completed[ 0 ] = true;
                    }
                } );

                new Waiter8()
                        .until( () -> completed[ 0 ] )
                        .onTimeout( millis ->
                        {
                            throw new Waiter8.TimeoutException( format( "Gave up waiting after [%s] millis.", millis ) );
                        } )
                        .withTimeoutMillis( 3000 * 1000 );

                if ( exception[ 0 ] != null )
                {
                    throw exception[ 0 ] instanceof RuntimeException
                            ? ( RuntimeException ) exception[ 0 ]
                            : new RuntimeException( exception[ 0 ] );
                }
            }
        }


        private void flushImpl() throws IOException
        {
            synchronized ( this )
            {
                if ( this.buf == null )
                {
                    return;
                }
                startProgramInput();
                final ByteBuffer byteBuffer = ByteBuffer.wrap( this.buf.toByteArray() );
                final CharBuffer charBuffer = this.decoder.decode( byteBuffer );
                try
                {
                    String text = charBuffer.toString();

                    // in this case we don't want extra lines - which are likely from scripts etc
                    if ( logger != null )
                    {
                        logger.info( text.trim() );
                    }

                    this.textInputControl.appendText( charBuffer.toString() );
                    this.textInputControl.positionCaret( this.textInputControl.getLength() );
                }
                finally
                {
                    this.buf = null;
                    endProgramInput();
                }
            }
        }

        @Override
        public void close() throws IOException
        {
            flush();
        }

        void clear() throws IOException
        {
            this.buf = null;
        }
    }

    public static class TeeOutputStream extends OutputStream
    {

        private final OutputStream[] streams;

        public TeeOutputStream( OutputStream... streams )
        {
            this.streams = streams;
        }


        @Override
        public void write( int b ) throws IOException
        {
            for ( OutputStream stream : streams )
            {
                stream.write( b );
            }
        }


        @Override
        public void write( byte[] b, int offset, int length ) throws IOException
        {
            for ( OutputStream stream : streams )
            {
                stream.write( b, offset, length );
            }
        }


        @Override
        public void flush() throws IOException
        {
            for ( OutputStream stream : streams )
            {
                stream.flush();
            }
        }


        @Override
        public void close() throws IOException
        {
            for ( OutputStream stream : streams )
            {
                forceClose( stream );
            }
        }


        private void forceClose( OutputStream stream )
        {
            try
            {
                stream.close();
            }
            catch ( IOException ignored )
            {
                ignored.printStackTrace();
            }
        }
    }
}


