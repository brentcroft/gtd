(function( session ) {
    /* model replaced here */
    var sessionModel = GUI_OBJECT_MODEL;

    var api = gtdApi.api;
    var gtdPrototypes = gtdApi.objectPrototypes;

    sessionModel.$api = gtdApi.api;
    sessionModel.$fullname = api.fullyqualifiedname;
    sessionModel.$ancestry = api.ancestry;
    sessionModel.$buildAncestry = api.buildAncestry;
    sessionModel.$buildAncestry( api );

    // so can use data driven
    gtdApi.applyPrototype( sessionModel, "base" );


    sessionModel.$distributeChange = api.distributeChange;

    // example using distribute change sets the prototypes
    sessionModel.$distributeChange( {
        "rootFirst": false,
        "action": function( item, key )
        {
            if ( !item.$actions )
            {
                return;
            }

            // local session driver
            item.$driver = session.driver;

            var actions = ("" + item.$actions).split( /\s*,\s*/ );
            var action = actions[ actions.length - 1 ];


            switch( action )
            {
                case "table":
                case "tree":
                case "index":
                case "text":
                case "richtext":
                case "click":
                case "tabs":
                case "tab":
                case "robot":
                    gtdApi.applyPrototype( item, action );
                    break;

                default:
                    print( "Unexpected action [" + action + "] on item: " + item.$fullname() );
            }
        }
    } );


    // insert any further apis
    sessionModel[ "#session" ] = {
        "configure": function( script ){ return session.getDriver().configure( script ); },
        "echo": function( echo ){ return session.getDriver().echo( echo ); },
        "gc": function( ){ return session.getDriver().gc( ); },
        "hashCache": function( level ){ return session.getDriver().hashCache( onOff ); },
        "reattach": function( ){
            var driver = session.getDriver();
            driver.detachRemoteNotificationListener();
            driver.attachRemoteNotificationListener();
            return "OK";
         },
        "notifySnapshotEventDelay": function( delay ){ return session.getDriver().notifySnapshotEventDelay( delay ); },
        "notifyAWTEvents": function( mask ){ return session.getDriver().notifyAWTEvents( mask ); },
        "notifyFXEvents": function( types ){ return session.getDriver().notifyFXEvents( types ); },
        "notifyDOMEvents": function( types ){ return session.getDriver().notifyDOMEvents( types ); },
        "logNotifications": function( level ) { session.getDriver().logNotifications( level ); },
        "getSuperSnapshotXml": function( ) { return session.getDriver().configure( "service.getGuiObjectLocator().getSuperSnapshotXmlText()" ); }
    };

    sessionModel[ "#driver" ] = session.getDriver();

    return sessionModel;
}( session ));

