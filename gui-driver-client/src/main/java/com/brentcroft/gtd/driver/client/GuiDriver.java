package com.brentcroft.gtd.driver.client;

import com.brentcroft.gtd.driver.GuiControllerMBean;
import javax.management.NotificationListener;

/**
 * Created by adobson on 20/05/2016.
 */
public interface GuiDriver extends GuiControllerMBean
{
    String getResultText( String path );

    String getComponentResultText( String path, String resultPath );
    boolean getComponentResult( String path, String resultPath );

    void addNotificationListener( NotificationListener nl );

    void removeNotificationListener( NotificationListener nl );

    void removeAllNotificationListeners();




    void delay( long millis );
    void delaySeconds( double seconds );

    boolean exists( String path );

    boolean exists( String path, double timeoutSeconds );

    boolean notExists( String path );

    boolean notExists( String path, double timeoutSeconds );

    boolean[] existsAll( String... paths );

    boolean[] existsAll( double timeoutSeconds, String... paths );


    boolean waitFor( final String path, final String resultPath );
    boolean waitFor( final String path, final String resultPath, double timeoutSeconds );
    boolean waitFor( final String path, final String resultPath, double timeoutSeconds, double pollIntervalSeconds );

    void click( String path );

    void click( String path, double timeoutSeconds );


    void robotClick( String path );

    void robotClick( String path, double timeoutSeconds );

    void robotClickPoint( String path, int x, int y );
    void robotClickPoint( String path, int x, int y, double timeoutSeconds );

    void robotDoubleClick( final String path );

    void robotDoubleClick( final String path, double timeoutSeconds );

    void robotDoubleClickPoint( final String path, int x, int y );

    void robotDoubleClickPoint( final String path, int x, int y, double timeoutSeconds );

    void robotKeys( final String path, final String keys );

    void robotKeys( final String path, final String keys, double timeoutSeconds );

    void robotKeysPoint( String path, String keys, int x, int y );
    void robotKeysPoint( String path, String keys, int x, int y, double timeoutSeconds );



    void setText( String path, String text );

    void setText( String path, String text, double timeoutSeconds );

    String getText( String path );

    String getText( String path, double timeoutSeconds );

    void selectTableRow( String path, int row );

    void selectTableRow( String path, int row, double timeoutSeconds );


    void selectTableColumn( String path, int column );

    void selectTableColumn( String path, int column, double timeoutSeconds );

    void selectTableCell( String path, int row, int column );

    void selectTableCell( String path, int row, int column, double timeoutSeconds );



    void setSelectedIndex( String path, final int index );

    void setSelectedIndex( String path, final int index, double timeoutSeconds );

    void selectTreeNode( String path, String treePath );

    void selectTreeNode( String path, String treePath, double timeoutSeconds );

    Integer getSelectedIndex( String path );

    Integer getSelectedIndex( String path, double timeoutSeconds );

    Integer getItemCount( String path );

    Integer getItemCount( String path, double timeoutSeconds );

}
