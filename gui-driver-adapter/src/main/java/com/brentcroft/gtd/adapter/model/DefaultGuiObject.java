package com.brentcroft.gtd.adapter.model;

import com.brentcroft.gtd.camera.CameraObjectManager;
import com.brentcroft.util.xpath.gob.Gob;
import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;

import static com.brentcroft.gtd.driver.Backend.XML_NAMESPACE_URI;
import static com.brentcroft.util.XmlUtils.maybeAppendElementAttribute;

/**
 * Created by Alaric on 15/07/2017.
 */
public abstract class DefaultGuiObject< T extends Object > extends AbstractGuiObject< T > implements GuiObject.Robot
{
    public DefaultGuiObject( T go, Gob parent, GuiObjectConsultant< T > guiObjectConsultant, CameraObjectManager objectManager )
    {
        super( go, parent, guiObjectConsultant, objectManager );
    }


    private void setAction( Element element, Map< String, Object > options, String action )
    {
        maybeAppendElementAttribute( options, element, XML_NAMESPACE_URI, ACTIONS_ATTRIBUTE, action );
    }


    protected void addRobotAction( Element element, Map< String, Object > options )
    {
        // TODO: remove the concept of a RobotAction
        // its implicit on every GuiObject
        //setAction( element, options, ATTRIBUTE_VALUE_ROBOT );
    }

    protected void addClickAction( Element element, Map< String, Object > options )
    {
        setAction( element, options, ATTRIBUTE_VALUE_CLICK );
    }


    protected void addTextAction( Element element, Map< String, Object > options )
    {
        setAction( element, options, ATTRIBUTE_VALUE_TEXT );
    }

    protected void addTreeAction( Element element, Map< String, Object > options )
    {
        setAction( element, options, ATTRIBUTE_VALUE_TREE );
    }

    protected void addIndexAction( Element element, Map< String, Object > options )
    {
        setAction( element, options, ATTRIBUTE_VALUE_INDEX );
    }

    protected void addTabAction( Element element, Map< String, Object > options )
    {
        setAction( element, options, ATTRIBUTE_VALUE_TAB );
    }

    protected void addTabsAction( Element element, Map< String, Object > options )
    {
        setAction( element, options, ATTRIBUTE_VALUE_TABS );
    }

    protected void addTableAction( Element element, Map< String, Object > options )
    {
        setAction( element, options, ATTRIBUTE_VALUE_TABLE );
    }


    public static class Converter
    {
        public interface ValueConverter< T >
        {
            String convert( T value );
        }

        private static final Map< Class, ValueConverter > converters = new HashMap< Class, ValueConverter >();


        public static void addConverter( ValueConverter processor, Class... targets )
        {
            for ( Class target : targets )
            {
                converters.put( target, processor );
            }
        }


        public static String maybeConvertValue( Object value )
        {
            String convert;

            if ( value == null )
            {
                convert = "";
            }
            else if ( ( value instanceof String ) || ! converters.containsKey( value.getClass() ) )
            {
                convert = value.toString();
            }
            else
            {
                convert = converters.get( value.getClass() ).convert( value );
            }

            return convert;
        }

        public static String maybeGetValueType( Object value )
        {
            if ( value == null )
            {
                return "";
            }

            try
            {
                return value.getClass().getSimpleName();
            }
            catch ( Exception ignored )
            {
                // some inner classes have no simple name
            }

            return value.getClass().getName();

        }
    }
}