package com.brentcroft.util.tools;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import javax.xml.bind.DatatypeConverter;

/**
 * 
 * @author ADobson
 * 
 */
public class XsdDateUtils
{

    public static Calendar getCalendar( Date date )
    {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime( date );
        return calendar;
    }

    /**
     * Create a date with as much detail as you require:
     * 
     * year, month, day, hour, minute, second, milli
     * 
     * @param k
     * 
     * @return a Date constructed from the arguments.
     */
    public static Date getDate( int... k )
    {
        final Calendar calendar = Calendar.getInstance();

        calendar.clear();

        switch ( k.length )
        {
            case 7:
                calendar.set( Calendar.MILLISECOND, k[ 6 ] );
            case 6:
                calendar.set( Calendar.SECOND, k[ 5 ] );
            case 5:
                calendar.set( Calendar.MINUTE, k[ 4 ] );
            case 4:
                calendar.set( Calendar.HOUR_OF_DAY, k[ 3 ] );
            case 3:
                calendar.set( Calendar.DAY_OF_MONTH, k[ 2 ] );
            case 2:
                calendar.set( Calendar.MONTH, k[ 1 ] );
            case 1:
                calendar.set( Calendar.YEAR, k[ 0 ] );
        }

        return calendar.getTime();
    }


    /**
     * Create a date, offset from the current date, with as much detail as you
     * require:
     * 
     * year, month, day, hour, minute, second, milli
     * 
     * @param k
     * 
     * @return a Date constructed from now, offset by the arguments.
     */
    public static Date getCurrentDate( int... k )
    {
        final Calendar calendar = Calendar.getInstance();

        switch ( k.length )
        {
            case 7:
                calendar.add( Calendar.MILLISECOND, k[ 6 ] );
            case 6:
                calendar.add( Calendar.SECOND, k[ 5 ] );
            case 5:
                calendar.add( Calendar.MINUTE, k[ 4 ] );
            case 4:
                calendar.add( Calendar.HOUR_OF_DAY, k[ 3 ] );
            case 3:
                calendar.add( Calendar.DAY_OF_MONTH, k[ 2 ] );
            case 2:
                calendar.add( Calendar.MONTH, k[ 1 ] );
            case 1:
                calendar.add( Calendar.YEAR, k[ 0 ] );
        }

        return calendar.getTime();
    }


    /**
     * Create a date, offset from the current date, with as much detail as you
     * require:
     * 
     * year, month, day, hour, minute, second, milli
     * 
     * @param k
     * 
     * @return a Date constructed from now, offset by the arguments.
     */
    public static Date getDate( Date date, int... k )
    {
        final Calendar calendar = Calendar.getInstance();

        calendar.setTime( date );

        switch ( k.length )
        {
            case 7:
                calendar.add( Calendar.MILLISECOND, k[ 6 ] );
            case 6:
                calendar.add( Calendar.SECOND, k[ 5 ] );
            case 5:
                calendar.add( Calendar.MINUTE, k[ 4 ] );
            case 4:
                calendar.add( Calendar.HOUR_OF_DAY, k[ 3 ] );
            case 3:
                calendar.add( Calendar.DAY_OF_MONTH, k[ 2 ] );
            case 2:
                calendar.add( Calendar.MONTH, k[ 1 ] );
            case 1:
                calendar.add( Calendar.YEAR, k[ 0 ] );
        }

        return calendar.getTime();
    }

    /**
     * Truncate a given date from the specified calendar field.
     * 
     * @param calendarField
     * 
     * @return a Date constructed from now, offset by the arguments.
     */
    public static Date truncateDate( Date date, int calendarField )
    {
        final Calendar calendar = Calendar.getInstance();

        calendar.setTime( date );

        switch ( calendarField )
        {
            case Calendar.YEAR:
                calendar.set( Calendar.YEAR, 0 );
            case Calendar.MONTH:
                calendar.set( Calendar.MONTH, 0 );
            case Calendar.DAY_OF_MONTH:
            case Calendar.DAY_OF_WEEK:
                switch ( calendarField )
                {
                    case Calendar.DAY_OF_MONTH:
                        calendar.set( Calendar.DAY_OF_MONTH, 1 );
                        break;
                    case Calendar.DAY_OF_WEEK:
                        calendar.set( Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek() );
                        break;
                }
            case Calendar.HOUR_OF_DAY:
                calendar.set( Calendar.HOUR_OF_DAY, 0 );
            case Calendar.MINUTE:
                calendar.set( Calendar.MINUTE, 0 );
            case Calendar.SECOND:
                calendar.set( Calendar.SECOND, 0 );
            case Calendar.MILLISECOND:
                calendar.set( Calendar.MILLISECOND, 0 );
        }

        return calendar.getTime();
    }


    public static String getDateDifferenceHHmmssSSS( Date d1, Date d2 )
    {
        long millis = d1.getTime() - d2.getTime();

        int milliseconds = (int) ( millis ) % 1000;
        int seconds = (int) ( millis / 1000 ) % 60;
        int minutes = (int) ( ( millis / ( 1000 * 60 ) ) % 60 );
        int hours = (int) ( ( millis / ( 1000 * 60 * 60 ) ) );


        return String.format( "%02d:%02d:%02d.%03d", hours, minutes, seconds, milliseconds );
    }


    /**
     * Create an XSD date time string from a Date
     * 
     * @param date
     * 
     * @return the xsd String value of the supplied Date.
     */
    public static String getXsdDateTime( Date date )
    {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime( date );
        return DatatypeConverter.printDateTime( calendar );
    }

    /**
     * Create a Date from an XSD date time string
     * 
     * @param lexicalXSDDateTime
     * 
     * @return a Date constructed from the supplied String.
     */
    public static Date getDateFromXsdDateTime( String lexicalXSDDateTime )
    {
        return DatatypeConverter.parseDateTime( lexicalXSDDateTime ).getTime();
    }


    /**
     * Create a Date from an XSD date time string
     * 
     * @param lexicalXSDDateTime
     * 
     * @return a Date constructed from the supplied String.
     */
    public static LocalDateTime getLocalDateTime( String lexicalXSDDateTime )
    {
        return LocalDateTime.parse( lexicalXSDDateTime );
    }


    /**
     * No arguments - for use in XSLT
     * 
     * @return
     */
    public static Date now()
    {
        return getCurrentDate();
    }

    /**
     * No arguments - for use in XSLT
     * 
     * @return
     */
    public static Date today()
    {
        return truncateDate( now(), Calendar.HOUR_OF_DAY );
    }
}
