package com.brentcroft.gtd.driver;

import com.brentcroft.gtd.driver.utils.HashCache;
import com.brentcroft.util.xpath.gob.Gob;

/**
 * Created by Alaric on 12/05/2017.
 */
public interface GuiObjectManager<T>
{
    T adapt( Object object, Gob parent );

    HashCache getHashCache();

}
