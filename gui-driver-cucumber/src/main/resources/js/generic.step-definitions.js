steps = {


	contextExecute: {
		"name" : "^execute$",
		"test" : [ "Then execute$" ],
		"step" : function( data )
		{
		  maybeCancel();

		  context.execute( data );
		}
	},


	waitForSeconds: {
		"name" : "^I wait for (\\d+(?:.\\d+)?) seconds?$",
		"test" : [ "And I wait for 2.34 seconds", "And I wait for 1 second" ],
		"step" : function( seconds )
		{
		   maybeCancel();

		   var Waiter = com.brentcroft.gtd.utilities.Waiter;

		   print( "Delaying for [" + seconds + "] seconds." );

		   Waiter.delaySeconds( seconds );
		}
	},

	loggedInTo: {
		"name" : '^I\'m logged in to "([^"]*)"(?: as "([^"]*)" "([^"]*)")?$',
		"test" : [
			"Given I'm logged in to \"SwingSet2-001\"",
			"Given I'm logged in to \"google\" as \"fred\" \"fr3d\""
		],
		"step" : function( sessionKey, username, password )
		{
		    maybeCancel();

			print( "Checking for session: " + sessionKey );

            var session = context.getSession( sessionKey );

            if ( session == null )
            {
                throw "No such session: " + sessionKey;
            }


            if ( username !== null && !username.isEmpty() )
            {
                var credentials = new java.util.Properties();

                credentials.put( "username", username );
                credentials.put( "password", password );

                session
                    .getGuiAdapter()
                    .setCredentials( credentials );
            }


            if ( session.isStopped() )
            {
                print( "Starting session: " + sessionKey );

                session.start();
            }
            else if ( !session.isLoggedIn() )
            {
                print( "Logging in to session: " + sessionKey );

                session.login();
            }
 		}
	},

    loggedOutFrom: {
		"name" : '^I\'m logged out from "([^"]*)"$',
		"test" : [
			"And I'm logged out from \"SwingSet2-001\"",
			"Given I'm logged out from \"google\""
		],
		"step" : function( sessionKey )
		{
		    maybeCancel();

			print( "Checking for session: " + sessionKey );

            var session = context.getSession( sessionKey );

            if ( session == null )
            {
                throw "No such session: " + sessionKey;
            }

            print( "Logging out from session: " + sessionKey );

            session.logout();
		}
	},


    /**
        alias one object as another
    */
	withObjectAs: {
		"name" : '^with object \\$([\\w\\d\\-\\_\\.]*) as \\$([\\w\\d\\-\\_]*)$',
		"test" : [
            'And with object $gui.frame.toolbar as $tb',
            'And with object $google.maps.html.area as $theArea'
        ],
		"step" : function( item, alias )
		{
		  maybeCancel();

		  var root = (item === null || item.isEmpty() ? null : item.trim() );

          var locus = root == null
                ? null
                : context.execute( item );

          if ( locus === null )
          {
            throw "Invalid object reference [" + item + "]";
          }
          else if ( locus == null )
          {
            throw "Bad object reference [" + item + "]";
          }
          else if ( alias == null )
          {
            throw "Invalid alias reference [" + alias + "]";
          }

          if ( alias )
          {
              // create a global variable named with the value of alias
              // alias = local;   // alias is a variable! we want its value as the name
              // [ alias ] = locus; // fails to compile
              // but this works!!!
              (function(name, value){this[alias]=value;}).call(null, alias, locus);
          }
		}
	},


	withObjectSetData: {
		"name" : "^with object \\$([\\w\\d\\-\\_\\.]*) set(?: data)?$",
		"test" : [
            "And with object $gui.toolbar set\n\"\"\"\n{}\n\"\"\"",
            "And with object $gui.toolbar set data\n\"\"\"\n{}\n\"\"\""
        ],
		"step" : function( item, data )
		{
		  maybeCancel();

		  var root = (item === null || item.isEmpty() ? null : item.trim() );

          var locus = root == null
                ? null
                : context.execute( item );

          if ( locus === null )
          {
            throw "Invalid object reference [" + item + "]";
          }
          else if ( locus == null )
          {
            throw "Bad object reference [" + item + "]";
          }
          else if ( ! locus.set )
          {
            throw "Object is not data driven [" + item + "]";
          }

          if ( data )
          {
            locus.set( JSON.parse( data ) );
          }
		}
	},


	clickOn: {
		"name" : "^I click on \\$([\\w\\d\\-\\_\\.]+)$",
		"test" : [ "And I click on $gui.toolbar.sessionButton" ],
		"step" : function( item )
		{
		    var item = context.execute( item );
			item.click( item );
		}
	},

	shouldExistWithin: {
		"name" : "^\\$([\\w\\d\\-\\_\\.]+) should exist(?: within (\\d+(?:.\\d+)?) seconds)?$",
		"test" : [
			"$gui.toolbar.sessionButton should exist",
			"$gui.toolbar.sessionButton should exist within 2.34 seconds"
		],
		"step" : function( item, timeout )
		{
            var item = context.execute( item );

			if ( timeout ? !item.exists( timeout ) : !item.exists( ) )
			{
				throw "item at [" + path + "] does not exist" + ( timeout ? " after " + timeout + "seconds" : "" );
			}
		}
	},


	shouldNotExistWithin: {
		"name" : "^\\$([\\w\\d\\-\\_\\.]+) should not exist(?: within (\\d+(?:.\\d+)?) seconds)?$",
		"test" : [
		    "$gui.toolbar.sessionButton should not exist",
		    "$gui.toolbar.sessionButton should not exist within 2.34 seconds"
		],
		"step" : function( item, timeout )
		{
			var item = context.execute( item );

			if ( timeout ? !item.notExists( timeout ) : !item.notExists( ) )
			{
				throw "item at [" + path + "] exists" + ( timeout ? " after " + timeout + "seconds" : "" );
			}
		}
	},

	shouldBeTo: {
		"name" : "^\\$([\\w\\d\\-\\_\\.]+) (?:attribute|offset) \"([^\"]+)\" should be (equal to|not equal to|less than|less than or equal to|greater than|greater than or equal to) \"([^\"]*)\"$",
		"test" : [
			"$gui.desktop.login.username attribute \"@text\" should be equal to \"alfredo\"",
			"$gui.desktop.login.username attribute \"@text\" should be not equal to \"alfredo\"",
			"$gui.desktop.countries offset \"model/@size\" should be less than \"7\"",
			"$gui.desktop.countries offset \"model/@size\" should be less than or equal to \"7\"",
			"$gui.desktop.countries attribute \"model/@size\" should be greater than \"7\"",
			"$gui.desktop.countries attribute \"model/@size\" should be greater than or equal to \"7\""
		],
		"step" : function( item, attribute, condition, expected )
		{
            var item = context.execute( item );

            var result = item.$driver
                    .getComponentResultText(
                    item,
                    attribute );

            switch ( condition )
            {
                case "equal to":
                    if ( result !== expected )
                    {
                        throw "Expected [ " + item +
                        " ] attribute [" + attribute +
                        "] to be equal to [" + expected +
                        "] but it was [" + result + "]";
                    }
                    break;
                case "not equal to":
                    if ( result == expected )
                    {
                        throw "Expected [ " + item +
                        " ] attribute [" + attribute +
                        "] to be not equal to [" + expected +
                        "] but it was [" + result + "]";
                    }
                    break;
                case "less than":
                    if ( result >= expected )
                    {
                        throw "Expected [ " + item +
                        " ] attribute [" + attribute +
                        "] to be less than [" + expected +
                        "] but it was [" + result + "]";
                    }
                    break;
                case "less than or equal to":
                    if ( result > expected )
                    {
                        throw "Expected [ " + item +
                        " ] attribute [" + attribute +
                        "] to be less than or equal to [" + expected +
                        "] but it was [" + result + "]";
                    }
                    break;
                case "greater than":
                    if ( result <= expected )
                    {
                        throw "Expected [ " + item +
                        " ] attribute [" + attribute +
                        "] to be greater than [" + expected +
                        "] but it was [" + result + "]";
                    }
                    break;
                case "greater than or equal to":
                    if ( result < expected )
                    {
                        throw "Expected [ " + item +
                        " ] attribute [" + attribute +
                        "] to be greater than or equal to [" + expected +
                        "] but it was [" + result + "]";
                    }
                    break;
                default:
                    throw "Invalid step condition: [" + condition + "]";
            }
        }
	},

	setTextInto: {
		"name" : "^I enter \"([^\"]*)\" into \\$(.*)$",
		"test" : [ "And I enter \"three blind mice...\" into $gui.login.username" ],
		"step" : function( text, item )
		{
            var item = context.execute( item );
            item.setText( text );
		}
	},

	typeTextInto: {
		"name" : "^I type \"([^\"]*)\" into \\$(.*)$",
		"test" : [ "And I type \"three blind mice...\" into $gui.login.username" ],
		"step" : function( text, item )
		{
            var item = context.execute( item );
			item.robotKeys( text );
		}
	},

	selectTabFrom: {
		"name" : "^I select tab (\\d+) from \\$([\\w\\d\\-\\_\\.]+)$",
		"test" : [ "And I select tab 2 from $gui.preferences.tabs" ],
		"step" : function( tab, item )
		{
            var item = context.execute( item );
            item.selectTab( tab );
		}
	},

	selectIndexFrom: {
		"name" : "^I select index (\\d+) from \\$([\\w\\d\\-\\_\\.]+)$",
		"test" : [ "And I select index 2 from $gui.preferences.colors" ],
		"step" : function( index, item )
		{
            var item = context.execute( item );
            item.setIndex( index );
		}
	},


	selectTableCellFrom: {
		"name" : "^I select cell (\\d+),(\\d+) from \\$([\\w\\d\\-\\_\\.]+)$",
		"test" : [ "And I select cell 2,3 from $ss2.tabs.Table_Demo.JTable" ],
		"step" : function( row, column, item )
		{
            var item = context.execute( item );
            item.selectCell( row, column );
		}
	},

	selectTableRowFrom: {
		"name" : "^I select row (\\d+) from \\$([\\w\\d\\-\\_\\.]+)$",
		"test" : [ "And I select row 4 from $ss2.tabs.Table_Demo.JTable" ],
		"step" : function( row, item )
		{
            var item = context.execute( item );
            item.selectRow( row );
		}
	}
};