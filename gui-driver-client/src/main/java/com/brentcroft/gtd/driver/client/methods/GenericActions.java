package com.brentcroft.gtd.driver.client.methods;

import com.brentcroft.gtd.driver.client.GuiDriver;
import com.brentcroft.util.Waiter8;

import static com.brentcroft.util.DateUtils.secondsToMillis;
import static java.lang.String.format;

/**
 * Created by adobson on 25/05/2016.
 */
public class GenericActions
{
    public static void executeAction( GuiDriver driver, String key, String path, String text, Double secondsToWait )
    {
        Action
                .getAction( key )
                .executeAction( driver, path, text, secondsToWait );
    }

    public enum Action
    {
        CLICK( "click" ) {
            @Override
            public void executeAction( GuiDriver driver, String path, String text, Double secondsToWait )
            {
                GenericMethods.clickOnComponent( driver, path, secondsToWait );
            }
        },

        CLICK_IF_EXISTS( "click if exists" ) {
            @Override
            public void executeAction( GuiDriver driver, String path, String text, Double secondsToWait )
            {
                if ( driver.exists( path, secondsToWait ) )
                {
                    System.out.println( format( "Path exists [%s] so click on it.", path ) );

                    // obviously we don't want to wait very long
                    GenericMethods.clickOnComponent( driver, path, 0.1 );
                }
            }
        },

        DOUBLE_CLICK( "double click" ) {
            @Override
            public void executeAction( GuiDriver driver, String path, String text, Double secondsToWait )
            {
                GenericMethods.doubleClickOnComponent( driver, path, secondsToWait );
            }
        },

        EXISTS( "exists" ) {
            @Override
            public void executeAction( GuiDriver driver, String path, String text, Double secondsToWait )
            {
                if ( !driver.exists( path, secondsToWait ) )
                {
                    throw new RuntimeException(
                            format( "Path [%s] did not exist even after [%s] seconds.", path, secondsToWait ) );
                }
            }
        },

        NOT_EXISTS( "not exists" ) {
            @Override
            public void executeAction( final GuiDriver driver, final String path, String dummy,
                    final Double secondsToWait )
            {
                new Waiter8()
                        .withTimeoutMillis( secondsToMillis( secondsToWait ) )
                        .onTimeout( millis -> {
                            throw new RuntimeException(
                                    format( "Path [%s] still exists even after [%s] seconds.", path, secondsToWait ) );

                        } )
                        .until( () -> {
                            // don't want to hang around waiting for it to exist
                            // since we don't expect it to exist
                            return !driver.exists( path, 0.01 );
                        } );
            }
        },

        NOT_EMPTY( "not empty" ) {
            @Override
            public void executeAction( GuiDriver driver, String path, String dummy, Double secondsToWait )
            {
                GenericMethods.waitUntilNotEmpty( driver, path, secondsToWait );
            }
        },

        MORE_THAN( "more than" ) {
            @Override
            public void executeAction( GuiDriver driver, String path, String amount, Double secondsToWait )
            {
                GenericMethods.waitUntilItemCountMoreThan( driver, path, Integer.valueOf( amount ), secondsToWait );
            }
        },

        LESS_THAN( "less than" ) {
            @Override
            public void executeAction( GuiDriver driver, String path, String amount, Double secondsToWait )
            {
                GenericMethods.waitUntilItemCountLessThan( driver, path, Integer.valueOf( amount ), secondsToWait );
            }
        },

        ENTER( "enter" ) {
            @Override
            public void executeAction( GuiDriver driver, String path, String text, Double secondsToWait )
            {
                GenericMethods.enterText( driver, path, text, secondsToWait );
            }
        },

        KEYS( "keys" ) {
            @Override
            public void executeAction( GuiDriver driver, String path, String text, Double secondsToWait )
            {
                GenericMethods.typeText( driver, path, text, secondsToWait );
            }
        },

        SELECT_ITEM( "select item" ) {
            @Override
            public void executeAction( GuiDriver driver, String path, String index, Double secondsToWait )
            {
                GenericMethods.setSelectedIndex( driver, path, Integer.valueOf( index ), secondsToWait );
            }
        },

        SELECT_ROW( "select row" ) {
            @Override
            public void executeAction( GuiDriver driver, String path, String row, Double secondsToWait )
            {
                GenericMethods.selectTableRow( driver, path, Integer.parseInt( row ), secondsToWait );
            }
        },

        SELECT_CELL( "select cell" ) {
            @Override
            public void executeAction( GuiDriver driver, String path, String rowColumn, Double secondsToWait )
            {
                String[] rc = rowColumn.split( "\\s*,\\s*" );

                GenericMethods.selectTableCell(
                        driver,
                        path,
                        Integer.parseInt( rc[ 0 ] ),
                        Integer.parseInt( rc[ 1 ] ),
                        secondsToWait );
            }
        },

        SELECT_NODE( "select node" ) {
            @Override
            public void executeAction( GuiDriver driver, String path, String nodePath, Double secondsToWait )
            {
                GenericMethods.selectTreeNode( driver, path, nodePath, secondsToWait );
            }
        };

        private final String key;

        Action( String key )
        {
            this.key = key;
        }

        public abstract void executeAction( GuiDriver driver, String path, String text, Double secondsToWait );

        public String getKey()
        {
            return key;
        }

        public static Action getAction( String key )
        {
            for ( Action action : Action.values() )
            {
                if ( action.key.equalsIgnoreCase( key ) )
                {
                    return action;
                }
            }

            throw new RuntimeException( format( "No such action [%s].", key ) );
        }
    }

}
