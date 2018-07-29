package com.brentcroft.util;

import com.brentcroft.util.buffer.AsynchBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.log4j.Logger;

import static java.lang.String.format;

/**
 * Created by Alaric on 01/12/2016.
 */
public class Pipes
{
    public interface Processor< IN, OUT >
    {
        OUT process( IN in );
    }

    public interface Listener< IN >
    {
        void receive( IN in );
    }

    public interface Emitter< OUT >
    {
        void addListener( Listener< OUT > listener );

        void removeListener( Listener< OUT > listener );
    }

    public interface Condition< IN >
    {
        boolean onCondition( IN in );
    }


    public static < IN, OUT > Pipe< IN, OUT > newPipe( Class< IN > inType, Class< OUT > outType )
    {
        return new Pipe< IN, OUT >();
    }

    public static Pipe< String, String > newStringPipe()
    {
        return new Pipe< String, String >();
    }


    public static class Pipe< IN, OUT > implements Listener< IN >, Emitter< OUT >
    {
        private final static Logger logger = Logger.getLogger( Pipe.class );

        private Condition< IN > inCondition = null;
        private Processor< IN, OUT > processor = null;
        private Condition< OUT > outCondition = null;
        private AsynchBuffer< OUT > outBuffer = null;
        private List< Listener< OUT > > listeners = null;


        private boolean logMessages = false;

        public boolean isLogMessages()
        {
            return logMessages;
        }

        public void setLogMessages( boolean logMessages )
        {
            this.logMessages = logMessages;
        }

        public Pipe< IN, OUT > withLogMessages( boolean logMessages )
        {
            this.logMessages = logMessages;
            return this;
        }


        private static long inst = 0;

        private String name = "Pipe: " + ( inst++ );

        public String getName()
        {
            return name;
        }

        public void setName( String name )
        {
            this.name = name;
        }

        public Pipe< IN, OUT > withName( String name )
        {
            this.name = name;
            return this;
        }



        /*

         */

        public OUT process( IN in )
        {
            if ( processor == null )
            {
                throw new RuntimeException( format( "[%s] No processor!", name ) );
            }

            return processor.process( in );
        }


        public void receive( IN message )
        {
            if ( isLogMessages() && logger.isDebugEnabled() )
            {
                logger.debug( format( "[%s] Received: %s", name, message ) );
            }

            if ( inCondition != null && ! inCondition.onCondition( message ) )
            {
                if ( isLogMessages() && logger.isDebugEnabled() )
                {
                    logger.debug( format( "[%s] In-condition denied.", name ) );
                }

                return;
            }

            // process message
            final OUT processResult = process( message );

            if ( isLogMessages() && logger.isDebugEnabled() )
            {
                logger.debug( format( "[%s] Process result: %s", name, processResult ) );
            }

            if ( outCondition != null && ! outCondition.onCondition( processResult ) )
            {
                if ( isLogMessages() && logger.isDebugEnabled() )
                {
                    logger.debug( format( "[%s] Out-condition denied.", name ) );
                }

                return;
            }

            if ( outBuffer == null )
            {
                notifyListeners( processResult );
            }
            else
            {
                outBuffer.add( processResult );
            }
        }

        public Pipe< IN, OUT > withProcessor( Processor< IN, OUT > processor )
        {
            this.processor = processor;
            return this;
        }


        public Pipe< IN, OUT > withConditionIn( Condition< IN > condition )
        {
            this.inCondition = condition;
            return this;
        }

        public Pipe< IN, OUT > withConditionOut( Condition< OUT > condition )
        {
            this.outCondition = condition;
            return this;
        }


        public Pipe< IN, OUT > withOutBuffer( double delaySeconds, int maxEntries, boolean fifo )
        {
            this.outBuffer = new AsynchBuffer< OUT >( "buffer" )
            {
                @Override
                public void process( OUT out )
                {
                    notifyListeners( out );
                }
            }
                    .withDelaySeconds( delaySeconds )
                    .withMaxEntries( maxEntries )
                    .withFirstInFirstOut( fifo );

            return this;
        }

        @SuppressWarnings( "unchecked" )
		public Pipe< IN, OUT > withListeners( Listener< OUT >... newListeners )
        {
            if ( newListeners != null )
            {
                for ( Listener< OUT > l : newListeners )
                {
                    addListener( l );
                }
            }
            return this;
        }


        public void addListener( Listener< OUT > listener )
        {
            if ( listeners == null )
            {
                listeners = new ArrayList< Listener< OUT > >();
            }
            listeners.add( listener );
        }


        public void removeListener( Listener< OUT > listener )
        {
            Optional
                    .ofNullable( listeners )
                    .ifPresent( listeners -> listeners.remove( listener ) );
        }

        private void notifyListeners( OUT out )
        {
            Optional
                    .ofNullable( listeners )
                    .ifPresent( listeners -> listeners.forEach( listener -> listener.receive( out ) ) );
        }
    }
}
