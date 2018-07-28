package com.brentcroft.gtd.driver;

import java.util.Map;
import java.util.Properties;

/**
 * An MBean specification that supports a GuiDriver proxy within the Gui being driven.
 *
 * @author Alaric
 */
public interface GuiControllerMBean
{
    Object shutdown( int status );

    Object echo( Object o );

    String getSnapshotXmlText();

    String getSnapshotXmlText( Map< String, Object > options );

    String getSnapshotXmlText( String path, Map< String, Object > options );

    Object configure( String script );

    void setProperties( Properties properties );

    void notifyAWTEvents( long notificationEventMask );

    void notifyFXEvents( String eventTypes );

    void notifyDOMEvents( String eventTypes );

    void notifySnapshotEventDelay( long delay );

    void hashCache( int level );

    void gc();

    void logNotifications( int level );



    boolean exists( String path, double timeoutSeconds, double pollIntervalSeconds );

    boolean[] existsAll( double timeoutSeconds, double pollIntervalSeconds, String... paths );

    boolean notExists( String path, double timeoutSeconds, double pollIntervalSeconds );


    void click( String path, double timeoutSeconds, double pollIntervalSeconds );


    void robotClick( String path, double timeoutSeconds, double pollIntervalSeconds );

    void robotDoubleClick( final String path, double timeoutSeconds, double pollIntervalSeconds );

    void robotKeys( final String path, final String keys, double timeoutSeconds, double pollIntervalSeconds );

    void robotClickPoint( String path, final int x, final int y, double timeoutSeconds, double pollIntervalSeconds );

    void robotDoubleClickPoint( final String path, final int x, final int y, double timeoutSeconds, double pollIntervalSeconds );

    void robotKeysPoint( final String path, final String keys, final int x, final int y, double timeoutSeconds, double pollIntervalSeconds );


    void setText( String path, String text, double timeoutSeconds, double pollIntervalSeconds );

    String getText( String path, double timeoutSeconds, double pollIntervalSeconds );

    void selectTableRow( String path, int row, double timeoutSeconds, double pollIntervalSeconds );

    void selectTableColumn( String path, int column, double timeoutSeconds, double pollIntervalSeconds );

    void selectTableCell( String path, int row, int column, double timeoutSeconds, double pollIntervalSeconds );

    void setSelectedIndex( String path, final int index, double timeoutSeconds, double pollIntervalSeconds );

    void selectTreeNode( String path, String treePath, double timeoutSeconds, double pollIntervalSeconds );

    Integer getSelectedIndex( String path, double timeoutSeconds, double pollIntervalSeconds );

    Integer getItemCount( String path, double timeoutSeconds, double pollIntervalSeconds );


    void execute( final String path, final String script, double timeoutSeconds, double pollIntervalSeconds );

}

