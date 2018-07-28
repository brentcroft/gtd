package com.brentcroft.gtd.events;

import javax.management.Notification;

/**
 * Created by Alaric on 25/05/2017.
 */
public interface JMXNotifier
{
    void sendNotification( Notification notification );
}
