package com.brentcroft.util.templates.jstl.tag;

import com.brentcroft.util.templates.Renderable;
import com.brentcroft.util.templates.jstl.JstlTemplate;

public interface JstlElement extends Renderable
{
    /**
     * Every JstlElement may have an inner JstlTemplate (which actually does the
     * rendering).
     * <p/>
     * Note that for some types of element it may be null.
     * 
     * @return the inner JstlTemplate
     */
    JstlTemplate getInnerJstlTemplate();


    /**
     * Introspect and tidy up (after being built) ready for action.
     * <p/>
     * This is called when the parser is closing an element, so after all its
     * content has been parsed.
     */
    void normalize();


    /**
     * Reconstruct text that could be used to recreate this element.
     * 
     * @return A string that could be parsed to reproduce this element.
     */
    String toText();

}
