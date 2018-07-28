package com.brentcroft.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * Created by adobson on 19/05/2016.
 */
public class DateUtils
{
    /**
     * Utility method to convert a Double value representing seconds to a long value representing the corresponding
     * amount of milliseconds.
     *
     * @param seconds a Double value representing seconds
     * @return a long value representing the corresponding amount of milliseconds
     */
    public static long secondsToMillis( Double seconds )
    {
        return ( seconds == null )
                ? 0
                : Double.valueOf( seconds * 1000 ).longValue();
    }


    public static Date getCurrentDate( int... parts )
    {
        Calendar c = Calendar.getInstance();

        if ( parts != null )
        {
            switch ( parts.length )
            {
                case 8:
                    c.add( Calendar.MILLISECOND, parts[ 6 ] );
                case 6:
                    c.add( Calendar.SECOND, parts[ 5 ] );
                case 5:
                    c.add( Calendar.MINUTE, parts[ 4 ] );
                case 4:
                    c.add( Calendar.HOUR_OF_DAY, parts[ 3 ] );
                case 3:
                    c.add( Calendar.DAY_OF_MONTH, parts[ 2 ] );
                case 2:
                    // month arg is 1 based
                    c.add( Calendar.MONTH, parts[ 1 ] - 1 );
                case 1:
                    c.add( Calendar.YEAR, parts[ 0 ] );
            }
        }
        return c.getTime();
    }

    public static Date getAbsoluteDate( int... parts )
    {
        Calendar c = Calendar.getInstance();

        c.clear();
        c.set( 0, 0, 0, 0, 0, 0 );
        c.set( Calendar.MILLISECOND, 0 );

        if ( parts != null )
        {
            switch ( parts.length )
            {
                case 8:
                    c.set( Calendar.MILLISECOND, parts[ 6 ] );

                case 6:
                    c.set( Calendar.SECOND, parts[ 5 ] );

                case 5:
                    c.set( Calendar.MINUTE, parts[ 4 ] );

                case 4:
                    c.set( Calendar.HOUR_OF_DAY, parts[ 3 ] );
                case 3:
                    c.set( Calendar.DAY_OF_MONTH, parts[ 2 ] );

                case 2:
                    // month arg is 1 based
                    c.set( Calendar.MONTH, parts[ 1 ] - 1 );

                case 1:
                    c.set( Calendar.YEAR, parts[ 0 ] );

                case 0:
            }
        }

        return c.getTime();
    }


    public static int dateYear( Date date )
    {
        Calendar c = Calendar.getInstance();
        c.setTime( date );

        return c.get( Calendar.YEAR );
    }

    public static int dateMonth( Date date )
    {
        Calendar c = Calendar.getInstance();
        c.setTime( date );

        // . return as 1 based
        return c.get( Calendar.MONTH ) + 1;
    }

    public static int dateDay( Date date )
    {
        Calendar c = Calendar.getInstance();
        c.setTime( date );

        return c.get( Calendar.DAY_OF_MONTH );
    }

    public static int dateHour( Date date )
    {
        Calendar c = Calendar.getInstance();
        c.setTime( date );

        return c.get( Calendar.HOUR_OF_DAY );
    }


    public static int dateMinute( Date date )
    {
        Calendar c = Calendar.getInstance();
        c.setTime( date );

        return c.get( Calendar.MINUTE );
    }


    public static int dateSecond( Date date )
    {
        Calendar c = Calendar.getInstance();
        c.setTime( date );

        return c.get( Calendar.SECOND );
    }


    public static int dateMillis( Date date )
    {
        Calendar c = Calendar.getInstance();
        c.setTime( date );

        return c.get( Calendar.MILLISECOND );
    }


    public static String timestamp()
    {
        return timestamp( Calendar.getInstance() );
    }

    public static String timestamp( long millis )
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis( millis );
        return timestamp( c );
    }

    public static String timestamp( Calendar c )
    {
        return CalendarFormatter.doFormat( "%Y-%M-%DT%h:%m:%s%z", c );
    }


    // this is copied from DatatypeConverterImpl
    // temporarily to avoid module classpath issue with java ea9
    private static final class CalendarFormatter
    {

        public static String doFormat( String format, Calendar cal ) throws IllegalArgumentException
        {
            int fidx = 0;
            int flen = format.length();
            StringBuilder buf = new StringBuilder();

            while ( fidx < flen )
            {
                char fch = format.charAt( fidx++ );

                if ( fch != '%' )
                {  // not a meta character
                    buf.append( fch );
                    continue;
                }

                // seen meta character. we don't do error check against the format
                switch ( format.charAt( fidx++ ) )
                {
                    case 'Y': // year
                        formatYear( cal, buf );
                        break;

                    case 'M': // month
                        formatMonth( cal, buf );
                        break;

                    case 'D': // days
                        formatDays( cal, buf );
                        break;

                    case 'h': // hours
                        formatHours( cal, buf );
                        break;

                    case 'm': // minutes
                        formatMinutes( cal, buf );
                        break;

                    case 's': // parse seconds.
                        formatSeconds( cal, buf );
                        break;

                    case 'z': // time zone
                        formatTimeZone( cal, buf );
                        break;

                    default:
                        // illegal meta character. impossible.
                        throw new InternalError();
                }
            }

            return buf.toString();
        }

        private static void formatYear( Calendar cal, StringBuilder buf )
        {
            int year = cal.get( Calendar.YEAR );

            String s;
            if ( year <= 0 ) // negative value
            {
                s = Integer.toString( 1 - year );
            }
            else // positive value
            {
                s = Integer.toString( year );
            }

            while ( s.length() < 4 )
            {
                s = '0' + s;
            }
            if ( year <= 0 )
            {
                s = '-' + s;
            }

            buf.append( s );
        }

        private static void formatMonth( Calendar cal, StringBuilder buf )
        {
            formatTwoDigits( cal.get( Calendar.MONTH ) + 1, buf );
        }

        private static void formatDays( Calendar cal, StringBuilder buf )
        {
            formatTwoDigits( cal.get( Calendar.DAY_OF_MONTH ), buf );
        }

        private static void formatHours( Calendar cal, StringBuilder buf )
        {
            formatTwoDigits( cal.get( Calendar.HOUR_OF_DAY ), buf );
        }

        private static void formatMinutes( Calendar cal, StringBuilder buf )
        {
            formatTwoDigits( cal.get( Calendar.MINUTE ), buf );
        }

        private static void formatSeconds( Calendar cal, StringBuilder buf )
        {
            formatTwoDigits( cal.get( Calendar.SECOND ), buf );
            if ( cal.isSet( Calendar.MILLISECOND ) )
            { // milliseconds
                int n = cal.get( Calendar.MILLISECOND );
                if ( n != 0 )
                {
                    String ms = Integer.toString( n );
                    while ( ms.length() < 3 )
                    {
                        ms = '0' + ms; // left 0 paddings.
                    }
                    buf.append( '.' );
                    buf.append( ms );
                }
            }
        }

        /**
         * formats time zone specifier.
         */
        private static void formatTimeZone( Calendar cal, StringBuilder buf )
        {
            TimeZone tz = cal.getTimeZone();

            if ( tz == null )
            {
                return;
            }

            // otherwise print out normally.
            int offset = tz.getOffset( cal.getTime().getTime() );

            if ( offset == 0 )
            {
                buf.append( 'Z' );
                return;
            }

            if ( offset >= 0 )
            {
                buf.append( '+' );
            }
            else
            {
                buf.append( '-' );
                offset *= - 1;
            }

            offset /= 60 * 1000; // offset is in milli-seconds

            formatTwoDigits( offset / 60, buf );
            buf.append( ':' );
            formatTwoDigits( offset % 60, buf );
        }

        /**
         * formats Integer into two-character-wide string.
         */
        private static void formatTwoDigits( int n, StringBuilder buf )
        {
            // n is always non-negative.
            if ( n < 10 )
            {
                buf.append( '0' );
            }
            buf.append( n );
        }
    }
}
