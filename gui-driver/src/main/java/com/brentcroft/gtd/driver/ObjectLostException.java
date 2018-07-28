package com.brentcroft.gtd.driver;

import java.io.Serializable;

/**
 * When a found GuiObject has lost its inner WeakReference to an actual GUI object.<p/>
 *
 * This exception may be returned to a remote caller.
 */
public class ObjectLostException extends RuntimeException implements Serializable
{
    private static final long serialVersionUID = 6336047388117977764L;

    public ObjectLostException( String m )
    {
        super( m );
    }
}
