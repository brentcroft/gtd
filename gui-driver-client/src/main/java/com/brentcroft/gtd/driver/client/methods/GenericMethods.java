package com.brentcroft.gtd.driver.client.methods;

import com.brentcroft.gtd.driver.client.GuiDriver;
import java.util.Objects;
import org.apache.log4j.Logger;

import static com.brentcroft.util.DateUtils.secondsToMillis;
import static java.lang.String.format;

/**
 * Created by adobson on 19/05/2016.
 */
public class GenericMethods
{
    private final static transient Logger logger = Logger.getLogger( GenericMethods.class );

    /**
     * Select a node in a tree.<br/>
     * <p>
     * The tree is specified by an XPath string that must locate a JTree object.<br/>
     * <p>
     * The node is specified by a colon-separated string of child indexes, e.g. "1:3:26:4", where each component
     * identifies the child index from the respective parent.<br/>
     * <p>
     * The root node has the node path: "1", hence all non-empty node paths begin with "1".<br/>
     *
     * @param path     the path to the tree component
     * @param nodePath the path in the tree to the node (e.g. "1:2:2:3" )
     */
    public static void selectTreeNode( GuiDriver driver, String path, String nodePath, double timeoutSeconds )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "About to select tree node [%s] at path [%s].", nodePath, path ) );
        }

        driver.selectTreeNode( path, nodePath, timeoutSeconds );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "Selected tree node [%s] at path [%s].", nodePath, path ) );
        }
    }


    /**
     * Select a row in a table.<br/>
     * <p>
     * The table is specified by an XPath string that must locate a JTable object.<br/>
     * <p>
     * The row is specified by an integer string.<br/>
     *
     * @param path the path to the table component
     * @param row  an integer string representing the row to be selected
     */
    public static void selectTableRow( GuiDriver driver, String path, int row, double timeoutSeconds )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "About to select table row [%s] at path [%s].", row, path ) );
        }

        driver.selectTableRow( path, row, timeoutSeconds );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "Selected table row [%s] at path [%s].", row, path ) );
        }
    }


    public static void selectTableCell( GuiDriver driver, String path, int row, int column, double timeoutSeconds )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "About to select table cell [%s,%s] at path [%s].", row, column, path ) );
        }

        driver.selectTableCell( path, row, column, timeoutSeconds );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "Selected table cell [%s,%s] at path [%s].", row, column, path ) );
        }
    }

    /**
     * Select an item in a list or combobox.<br/>
     * <p>
     * The list or combobox is specified by an XPath string that must locate a JList or JComboBox object.<br/>
     * <p>
     * The item index is specified by an integer string.<br/>
     *
     * @param path  the path to the list or combobox component
     * @param index an integer string representing the index to be selected
     */
    public static void setSelectedIndex( GuiDriver driver, String path, int index, double timeoutSeconds )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "About to select item [%s] at path [%s].", index, path ) );
        }

        driver.setSelectedIndex( path, index, timeoutSeconds );


        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "Selected item [%s] at path [%s].", index, path ) );
        }
    }


    /**
     * Enter text into a component (that can accept text, i.e. has a setText(String text) method).<br/>
     *
     * @param path the path to the component that can accept text
     * @param text the text to enter into the specified component
     */
    public static void enterText( GuiDriver driver, String path, String text, double timeoutSeconds )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "About to enter text [%s] at path [%s].", text, path ) );
        }

        driver.setText( path, text, timeoutSeconds );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "Entered text [%s] at path [%s].", text, path ) );
        }
    }


    /**
     * Type text (i.e. send KEY_MODEL strokes) into a component.<br/>
     *
     * @param path the path to the component that can accept keystrokes
     * @param text the text (keys) to enter into the specified component
     */
    public static void typeText( GuiDriver driver, String path, String text, double timeoutSeconds )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "About to type text [%s] at path [%s].", text, path ) );
        }

        driver.robotKeys( path, text, timeoutSeconds );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "Typed text [%s] at path [%s].", text, path ) );
        }
    }


    /**
     * Click on a component.<br/>
     *
     * @param path the path to the component that will be clicked on
     */
    public static void clickOnComponent( GuiDriver driver, String path, double timeoutSeconds )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "About to click at path [%s].", path ) );
        }

        driver.click( path, timeoutSeconds );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "Clicked at path [%s].", path ) );
        }
    }


    /**
     * Double click on a component.<br/>
     *
     * @param path the path to the component that will be double clicked on
     */
    public static void doubleClickOnComponent( GuiDriver driver, String path, double timeoutSeconds )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "About to double click at path [%s].", path ) );
        }

        driver.robotDoubleClick( path, timeoutSeconds );

        if ( logger.isDebugEnabled() )
        {
            logger.debug( format( "Double clicked at path [%s].", path ) );
        }
    }


    public static void waitUntilItemCountMoreThan( final GuiDriver driver, String path, final Integer expectedCount,
                                                   final Double secondsToWait )
    {
        final long started = System.currentTimeMillis();

        Integer actualAmount = null;

        while ( ( System.currentTimeMillis() - started ) < secondsToMillis( secondsToWait ) )
        {
            actualAmount = driver.getItemCount( path,
                    ( secondsToWait - ( System.currentTimeMillis() - started ) ) );

            if ( actualAmount != null && actualAmount > expectedCount )
            {
                // ok
                return;
            }
        }
        throw new RuntimeException(
                format( "Path [%s] still has item count [%s] which is not more than [%s] even after [%s] seconds.",
                        path, actualAmount, expectedCount, secondsToWait ) );
    }


    public static void waitUntilItemCountEqual( final GuiDriver driver, String path, final Integer expectedCount,
                                                final Double secondsToWait )
    {
        final long started = System.currentTimeMillis();

        Integer actualAmount = null;

        while ( ( System.currentTimeMillis() - started ) < secondsToMillis( secondsToWait ) )
        {
            actualAmount = driver.getItemCount( path,
                    ( secondsToWait - ( System.currentTimeMillis() - started ) ) );

            if ( actualAmount != null && Objects.equals( actualAmount, expectedCount ) )
            {
                // ok
                return;
            }
        }
        throw new RuntimeException(
                format( "Path [%s] still has item count [%s] which is not equal to [%s] even after [%s] seconds.",
                        path, actualAmount, expectedCount, secondsToWait ) );
    }


    public static void waitUntilItemCountLessThan( final GuiDriver driver, String path, final Integer expectedCount,
                                                   final Double secondsToWait )
    {
        final long started = System.currentTimeMillis();

        Integer actualAmount = null;

        while ( ( System.currentTimeMillis() - started ) < secondsToMillis( secondsToWait ) )
        {
            actualAmount = driver.getItemCount( path,
                    ( secondsToWait - ( System.currentTimeMillis() - started ) ) );

            if ( actualAmount != null && actualAmount < expectedCount )
            {
                // ok
                return;
            }
        }
        throw new RuntimeException(
                format( "Path [%s] still has item count [%s] which is not less than [%s] even after [%s] seconds.",
                        path, actualAmount, expectedCount, secondsToWait ) );
    }


    public static void waitUntilNotEmpty( GuiDriver driver, String path, Double secondsToWait )
    {
        final long started = System.currentTimeMillis();

        String text = null;

        while ( ( System.currentTimeMillis() - started ) < secondsToMillis( secondsToWait ) )
        {
            text = driver.getText( path,
                    ( secondsToWait - ( System.currentTimeMillis() - started ) ) );

            if ( null == text )
            {
                // ok
                return;
            }
        }
        throw new RuntimeException(
                format( "Path [%s] still has text [%s] even after [%s] seconds.", path, text, secondsToWait ) );
    }

    public static void waitUntilText( GuiDriver driver, String path, final String expectedText, double secondsToWait )
    {
        final long started = System.currentTimeMillis();

        String text = null;

        while ( ( System.currentTimeMillis() - started ) < secondsToMillis( secondsToWait ) )
        {
            text = driver.getText( path,
                    ( secondsToWait - ( System.currentTimeMillis() - started ) ) );

            if ( 0 == expectedText.compareTo( text ) )
            {
                // ok
                return;
            }
        }
        throw new RuntimeException(
                format( "Path [%s] expected [%s] but still has text [%s] even after [%s] seconds.", path, expectedText,
                        text, secondsToWait ) );

    }


    public static void waitUntilExists( GuiDriver driver, String path, double secondsToWait )
    {
        final long started = System.currentTimeMillis();

        while ( ( System.currentTimeMillis() - started ) < secondsToMillis( secondsToWait ) )
        {
            if ( driver.exists( path,
                    ( secondsToWait - ( System.currentTimeMillis() - started ) ) ) )
            {
                return;
            }
        }
        throw new RuntimeException(
                format( "Path [%s] does not exist even after [%s] seconds.", path, secondsToWait ) );
    }


    public static void waitUntilNotExists( GuiDriver driver, String path, Double secondsToWait )
    {
        final long started = System.currentTimeMillis();

        while ( ( System.currentTimeMillis() - started ) < secondsToMillis( secondsToWait ) )
        {
            if ( ! driver.exists( path,
                    ( secondsToWait - ( System.currentTimeMillis() - started ) ) ) )
            {
                return;
            }
        }
        throw new RuntimeException(
                format( "Path [%s] still exists even after [%s] seconds.", path, secondsToWait ) );
    }
}
