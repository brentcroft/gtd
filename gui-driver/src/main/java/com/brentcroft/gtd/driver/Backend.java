package com.brentcroft.gtd.driver;

import static java.lang.String.format;

public class Backend
{
    // TODO: to auto increment
    public static final String BUILD_DATE = "2018-01-19";
    public static final String BUILD_VERSION = "0.3.2";

    /**
     * The KEY_MODEL used to store a GuiObject in an XML nodes UserData map.
     */
    public static final String GUI_OBJECT_KEY = "GUI_OBJECT_KEY";


    /**
     * Attributes
     */
    public static final String XML_NAMESPACE_TAG = "a";
    public static final String XML_NAMESPACE_URI = "com.brentcroft.gtd.model";


    public static final String DUPLICATE_ATTRIBUTE = "a:duplicate";
    public static final String VISITS_ATTRIBUTE = "a:visits";
    public static final String RESURRECTED_ATTRIBUTE = "a:matches";
    //public static final String REALLOCATED_ATTRIBUTE = "a:reused";
    public static final String SHALLOW_ATTRIBUTE = "a:shallow";
    public static final String ACTIONS_ATTRIBUTE = "a:actions";
    public static final String KEY_ATTRIBUTE = "a:key";
    public static final String KEY_RAW_ATTRIBUTE = "a:xpath";
    public static final String NAME_ATTRIBUTE = "a:name";
    public static final String NAME_PREVIOUS_ATTRIBUTE = "a:name-previous";
    public static final String NAME_CLAIMS_ATTRIBUTE = "a:name-claims";
    public static final String HASH_ATTRIBUTE = "hash";
    public static final String HASH_NEW_ATTRIBUTE = "hash-new";
    public static final String MODEL_ATTRIBUTE = "adapter";
    public static final String ID_ATTRIBUTE = "id";
    //public static final String CLASS_ATTRIBUTE = "class";
    public static final String HASH_FOR_ATTRIBUTE = "for-hash";
    public static final String HTML_FOR_ATTRIBUTE = "for";
    public static final String TEXT_ATTRIBUTE = "text";
    //public static final String TITLE_ATTRIBUTE = "title";
    public static final String STAR = "*";

    /**
     * Calculates and returns the JMX bean object name for this class:<br/>
     * <p>
     * ${class.package}:type=${class.simpleName}
     *
     * @return the JMX bean object name
     */
    public static String getMBeanRef(Class< ? > controllerClass)
    {
        return format( "%s:type=%s",
                controllerClass.getPackage().getName(),
                controllerClass.getSimpleName() );
    }
}
