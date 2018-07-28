var XmlUtils = Java.type( 'com.brentcroft.util.XmlUtils' );

/**
* A printer may be dynamically assigned to divert log text flow
*/
function log( text )
{
    if ( typeof( printer ) !== "undefined" )
    {
        printer.println( text );
        printer.flush();
    }
    else
    {
        print( text );
    }
}

/**
* A CANCEL object may be dynamically assigned to indicate quit!
*/
function maybeCancel()
{
    if ( typeof( CANCEL ) !== "undefined" )
    {
        // should be a CancelException
        throw CANCEL;
    }
}

var gtdApi = {

    objectPrototypes: (

        function preparePrototypes()
        {
            function GuiBase(){};
            GuiBase.prototype = {};

            function GuiObject(){};
            GuiObject.prototype = new GuiBase();
            GuiObject.prototype.exists = function( timeout ) { return timeout ? this.$driver.exists( this, timeout ) : this.$driver.exists( this ); };
            GuiObject.prototype.notExists = function( timeout ) { return timeout ? this.$driver.notExists( this, timeout ) : this.$driver.notExists( this ); };
            GuiObject.prototype.calculate = function( offset ) { return this.$driver.getComponentResultText( this, offset ); };
            GuiObject.prototype.robotClick = function( timeout ) { return timeout ? this.$driver.robotClick( this, timeout ) : this.$driver.robotClick( this ); };
            //GuiObject.prototype.robotClickPoint = function( x, y, timeout ) { return timeout ? this.$driver.robotClickPoint( this, x, y, timeout ) : this.$driver.robotClickPoint( this, x, y ); };
            //GuiObject.prototype.robotDoubleClick = function( ) { this.$driver.robotDoubleClick( this ); };
            //GuiObject.prototype.robotDoubleClickPoint = function( x, y ) { this.$driver.robotDoubleClick( this, x, y ); };
            GuiObject.prototype.robotKeys = function( keys ) { this.$driver.robotKeys( this, keys ); };
            //GuiObject.prototype.robotKeysPoint = function( keys, x, y ) { this.$driver.robotKeys( this, keys, x, y ); };
            GuiObject.prototype.waitFor = function( xpath, timeout ) { return timeout ? this.$driver.waitFor( item, xpath, timeout ) : this.$driver.waitFor( item, xpath ); };

            function GuiTabObject(){};
            GuiTabObject.prototype = new GuiObject();
            GuiTabObject.prototype.selectTab = function(){ this.$api.maybeSetTab( this );};

            function GuiClickObject(){};
            GuiClickObject.prototype = new GuiObject();
            GuiClickObject.prototype.click = function( timeout ) { return timeout ? this.$driver.click( this, timeout ) : this.$driver.click( this ); };
            GuiClickObject.prototype.doubleClick = function( timeout ) { return timeout ? this.$driver.doubleClick( this, timeout ) : this.$driver.doubleClick( this ); };

            function GuiTextObject(){};
            GuiTextObject.prototype = new GuiClickObject();
            GuiTextObject.prototype.getText = function() { return this.$driver.getText( this ); };
            GuiTextObject.prototype.setText = function( text, timeout ) { return timeout ? this.$driver.setText( this, text, timeout ) : this.$driver.setText( this, text ); };

            function GuiRichTextObject(){};
            GuiRichTextObject.prototype = new GuiTextObject();
            // TODO: get component and investigate
            GuiRichTextObject.prototype.isHtml = function() { return false; };


            function GuiIndexObject(){};
            GuiIndexObject.prototype = new GuiTextObject();
            GuiIndexObject.prototype.getIndex = function() { return this.$driver.getSelectedIndex( this ); };
            GuiIndexObject.prototype.setIndex = function( index ) { return this.$driver.setSelectedIndex( this, index ); };

            function GuiTabsObject(){};
            GuiTabsObject.prototype = new GuiIndexObject();


            function GuiTableObject(){};
            GuiTableObject.prototype = new GuiObject();
            GuiTableObject.prototype.selectRow = function( row ) { return this.$driver.selectTableRow( this, row ); };
            GuiTableObject.prototype.selectCell = function( row, column ) { return this.$driver.selectTableCell( this, row, column ); };

            function GuiTreeObject(){};
            GuiTreeObject.prototype = new GuiObject();
            GuiTreeObject.prototype.selectNode = function( nodePath ) { return this.$driver.selectTreeNode( this, nodePath ); };

            log( "Template.Model API: prepared prototypes." );

            return {
                GuiBase: GuiBase,
                GuiObject: GuiObject,
                GuiTabObject: GuiTabObject,
                GuiClickObject: GuiClickObject,
                GuiTextObject: GuiTextObject,
                GuiRichTextObject: GuiRichTextObject,
                GuiIndexObject: GuiIndexObject,
                GuiTableObject: GuiTableObject,
                GuiTabsObject: GuiTabsObject,
                GuiTreeObject: GuiTreeObject
            };
        }
    )(),


    api :
    {
        ancestry: function ( item )
        {
            var localPath = typeof( item.$xpath ) !== "undefined" ? ( item.$xpath ) : null;
            var ancestorPath =  typeof( item.$ancestor ) !== "undefined" ? this.$ancestry( item.$ancestor ) : null ;
            return (localPath == null || localPath == "")
                    ? (ancestorPath == null || ancestorPath == "")
                        ? ""
                        : ancestorPath
                    : (ancestorPath == null || ancestorPath == "")
                        ? localPath
                        : localPath + "[ ancestor::" +  ancestorPath + " ]";
        },

        distributeChange: function ( change )
        {
            for (var i in this)
            {
                if ( i.startsWith( "$" ) )
                {
                    continue;
                }

                if (typeof this[i] == 'object' && this[i].$name)
                {
                    if ( change.rootFirst )
                    {
                        change.action( this[i], i );
                    }

                    this[i].$distributeChange = this.$distributeChange;
                    this[i].$distributeChange( change );

                    if ( !change.rootFirst )
                    {
                        change.action( this[i], i );
                    }

                }
            }
            return this;
        },

        buildAncestry: function ( api )
        {
            var isLeaf = true;

            for( var i in this )
            {
                if ( i.startsWith( "$" ) )
                {
                    continue;
                }

                if (typeof this[i] == 'object')
                {
                    this[i].$name = "" + i;
                    this[i].$ancestry = this.$ancestry;
                    this[i].$buildAncestry = this.$buildAncestry;
                    if ( this[i].$buildAncestry )
                    {
                        this[i].$buildAncestry( api );
                        this[i].$path = api.path;
                        this[i].$forwardpath = api.forwardpath;
                        this[i].toString = api.canonicalPath;
                        isLeaf = false;
                    }
                    this[i].$ancestor = this;
                    this[i].$fullname = api.fullyqualifiedname;

                }
            }

            this.$leaf = isLeaf;

            return this;
        },

        path: function ()
        {
            var localPath = typeof( this.$xpath ) !== "undefined" ? ( "//" + this.$xpath ) : null;
            var ancestorPath =  typeof( this.$ancestor ) !== "undefined" ? this.$ancestry( this.$ancestor ) : null ;
            return (localPath == null || localPath == "")
                    ? (ancestorPath == null || ancestorPath == "")
                        ? ""
                        : ancestorPath
                    : (ancestorPath == null || ancestorPath == "")
                        ? localPath
                        : localPath + "[ ancestor::" +  ancestorPath + " ]";
        },

        forwardpath: function ()
        {
            var localPath = typeof( this.$xpath ) !== "undefined" ? ( "//" + this.$xpath ) : "";
            var ancestorPath =  typeof( this.$ancestor ) !== "undefined" && this.$ancestor.$forwardpath ? this.$ancestor.$forwardpath() : "" ;
            return ancestorPath + localPath;
        },


        fullyqualifiedname: function ()
        {
            return ( ( typeof( this.$ancestor ) == "undefined" ) ? "" : ( this.$ancestor.$fullname() + "." ) ) + this.$name;
        },


        canonicalPath: function ()
        {
            return (
                ( this.$hash && this.$active )
                    ? ( "hash=" + this.$hash + ";" )
                    : "" ) + this.$forwardpath();
        },



        isDefined: function(n) {
            return typeof( n ) !== "undefined";
        },
        isNumeric: function(n) {
            return !isNaN(parseFloat(n)) && isFinite(n);
        },
        isObject: function(o) {
            return o !== null && typeof o === 'object';
        },
        isFunction: function(f) {
            return f !== null && typeof f === "function";
        },
        isString: function(o) {
            return o !== null && typeof o === 'string';
        },

        dataDriven: function( item, data ) {

            // check for cancel
            maybeCancel();

            var modelItem = item.$fullname ? item.$fullname() : "anonymous";

            // these are ad-hoc supplied by the model
            if ( item.$onInput )
            {
                log( "locus=[" + modelItem + "]: onInput" );
                item.$onInput();
            }

            // do any actions specified in the data before traversing
            // these supplied by the api and action mappings
            if ( item.$input )
            {
                log( "locus=[" + modelItem + "], data=[" + JSON.stringify( data ) + "]" );
                item.$input( data );
            }

            if ( Array.isArray( data ) )
            {
                var dl = data.length;

                for ( var i = 0; i < dl; i++ )
                {
                    // calling the item's implementation
                    item.set( data[ i ] );
                }
            }
            else if ( typeof data === 'object' )
            {
                for( var key in data )
                {
                    if ( key.startsWith( "$" ) || !data.hasOwnProperty( key ) )
                    {
                        continue;
                    }

                    var newLocus = item[ key ];
                    var newData = data[ key ];

                    if ( newLocus == null )
                    {
                        // underscore or star must precede a dynamic reference
                        if ( key.startsWith( "_" ) )
                        {
                            var path = ( newData.$path )
                                ? newData.$path
                                : key.substring( 1 );

                            var reference = eval( path );

                            newLocus = reference;

                            log( "Dynamic ref: path=[" + path + "] -> " + reference );

                            delete newData.$path;
                        }
                        else
                        {
                            throw ( "dataDriven: Invalid object path step [" + key + "] from [" + modelItem + "]; [$path=" + newData.$path + "]." );
                        }
                    }
                    else if ( !item.$api.isFunction( newLocus.set ) )
                    {
                        throw "dataDriven: Object at path step [" + key + "] from [" + modelItem + "] is not data driven; " + newLocus.set;
                    }

                    // calling the newLocus's implementation
                    newLocus.set( newData );
                }
            }
        },

        dataExtractObject: function( item )
        {
            // check for cancel
            maybeCancel();

            var modelItem = item.$fullname ? item.$fullname() : "anonymous";

            var localData = null;

            if ( item.$api.isFunction( item.$output ) )
            {
                try
                {
                    localData = item.$output();
                    log( "out: locus=[" + modelItem + "], data=[" + JSON.stringify( localData ) + "]" );
                }
                catch (e)
                {
                    throw "Error calling this.$output(): " + e;
                }
            }
            else
            {
                localData = {};
            }

            for( var key in item )
            {
                // dealt with by it's parent
                if ( key.startsWith( "$" ) )
                {
                    continue;
                }

                var newLocus = item[ key ];

                if ( newLocus == null )
                {
                    throw ( "dataExtractObject: Invalid object path step [" + key + "] from [" + modelItem + "]." );
                }
                else if ( ! ( typeof newLocus === 'object' ) )
                {
                    continue;
                }

                try
                {
                    localData[ key ] = item.$api.dataExtractObject( newLocus );
                }
                catch ( e )
                {
                    localData[ key ] = { "$error": 1 };
                }
            }

            return localData;
        },

      dataExtractJson: function( item )
      {
        return JSON.stringify( item.$api.dataExtractObject( item ), null, 2 );
      },


      canonicalWaitFor: function( item, data )
      {
        var until = null;
        var timeout = null;

        if ( !data.$waitFor )
        {
          return;
        }

        /*
            $waitFor: { until: "{xpath}" }
        */
        if ( item.$api.isObject( data.$waitFor ) && item.$api.isString( data.$waitFor.until ) )
        {
          until = data.$waitFor.until;
        }
        /*
            $waitFor: { contains: "{text}" }
        */
        else if ( item.$api.isObject( data.$waitFor ) && item.$api.isString( data.$waitFor.contains ) )
        {
          until = "//*[ contains( @text, '" + XmlUtils.escapeForXmlAttribute( data.$waitFor.contains ) + "' ) ]";
        }
        /*
            $waitFor: "{xpath}"
        */
        else if ( item.$api.isString( data.$waitFor ) )
        {
          until = data.$waitFor;
        }
        /*
            $waitFor: {timeout},
            $text: "{contains}"
        */
        else if ( item.$api.isNumeric( data.$waitFor ) && data.$text )
        {
          timeout = data.$waitFor;
          until = "//*[ contains( @text, '" + XmlUtils.escapeForXmlAttribute( data.$text ) + "' ) ]";
        }
        else
        {
          throw "Invalid Data Argument: cannot locate 'until' text in, or next to, '$waitFor': " + JSON.stringify( data );
        }

        if ( item.$api.isObject( data.$waitFor ) && item.$api.isNumeric( data.$waitFor.timeout ) )
        {
          /*
            $waitFor: {
                until: "~",
                timeout: {seconds}
            }
          */
          timeout = data.$waitFor.timeout;
        }
        else
        {
          //throw "Invalid Data Argument: cannot locate 'timeout' seconds in, or as, '$waitFor': " + JSON.stringify( data );
          /*
            $waitFor: {
                until: "~",
                timeout: 3.0
            }
          */
          timeout = 3.0;
        }

        data.$waitFor = {
            "until": until,
            "timeout": timeout
        };
      },


      canonicalCheckFor: function( item, data )
      {
        var until = null;
        var timeout = null;

        if ( !data.$checkFor )
        {
          return;
        }

        if ( item.$api.isObject( data.$checkFor ) && item.$api.isString( data.$checkFor.until ) )
        {
          until = data.$checkFor.until;
        }
        else if ( item.$api.isObject( data.$checkFor ) && item.$api.isString( data.$checkFor.contains ) )
        {
          until = "//*[ contains( @text, '" + XmlUtils.escapeForXmlAttribute( data.$checkFor.contains ) + "' ) ]";
        }
        else if ( item.$api.isString( data.$checkFor ) )
        {
          until = data.$checkFor;
        }
        /*
            $checkFor: {timeout},
            $text: "{contains}"
        */
        else if ( item.$api.isNumeric( data.$checkFor ) && data.$text )
        {
          timeout = data.$checkFor;
          until = "//*[ contains( @text, '" + XmlUtils.escapeForXmlAttribute( data.$text ) + "' ) ]";
        }
        else
        {
          throw "Invalid Data Argument: cannot locate 'until' text in, or next to, '$checkFor': " + JSON.stringify( data );
        }

        // TIMEOUT
        if ( item.$api.isObject( data.$checkFor ) && item.$api.isNumeric( data.$checkFor.timeout ) )
        {
          timeout = data.$checkFor.timeout;
        }
        else if ( !timeout )
        {
          //throw "Invalid Data Argument: cannot locate 'timeout' seconds in, or as, '$checkFor': " + JSON.stringify( data );
          timeout = 3.0;
        }

        data.$checkFor = {
            "until": until,
            "timeout": timeout
        };

        log( "canonicalCheckFor: item=[" + item.$fullname() + "], until=" + until + ", timeout=[" + timeout + " s].");
      },


      canonicalClick: function( item, key, data )
      {
        var count = null;
        var x = null;
        var y = null;
        var delay = null;

        if ( !data[ key ] )
        {
          return;
        }

        var d = data[ key ];

        if ( item.$api.isObject( d ) )
        {
            //
            if ( !item.$api.isDefined( d.count ) || !item.$api.isNumeric( d.count ) )
            {
                // ok - assume its a passing call
                return;
            }

            count = d.count;

            if ( item.$api.isDefined( d.x ) && item.$api.isNumeric( d.x ) )
            {
                x = d.x;
            }
            if ( item.$api.isDefined( d.y ) && item.$api.isNumeric( d.y ) )
            {
                y = d.y;
            }
            if ( item.$api.isDefined( d.delay ) && item.$api.isNumeric( d.delay ) )
            {
                delay = d.delay;
            }
        }
        else if ( item.$api.isNumeric( d ) )
        {
            count = d;
        }
        else
        {
            throw "Invalid Data Argument: cannot canonicalize data for 'click': " + JSON.stringify( data );
        }

        if ( count == null || !item.$api.isNumeric( count ) )
        {
            return;
        }


        if ( item.$api.isObject( data[ key ] ) )
        {
            data[ key ].count = count;
            data[ key ].delay = delay;
         }
        else
        {
            data[ key ] = {
                "count": count,
                "delay": delay
            };
        }

        if ( item.$api.isNumeric( x ) && item.$api.isNumeric( y ) )
        {
            data[ key ].x = x;
            data[ key ].y = y;
        }
      },

      canonicalKeys: function( item, key, data )
      {
        var text = null;
        var x = null;
        var y = null;
        var delay = null;

        if ( !data[ key ] )
        {
          return;
        }

        var d = data[ key ];

        if ( item.$api.isObject( d ) )
        {
            //
            if ( !item.$api.isDefined( d.$text ) || !item.$api.isString( d.$text ) )
            {
                // ok - assume its a passing call
                return;
            }

            text = d.$text;

            if ( item.$api.isDefined( d.$x ) && item.$api.isNumeric( d.$x ) )
            {
                x = d.$x;
            }
            if ( item.$api.isDefined( d.$y ) && item.$api.isNumeric( d.$y ) )
            {
                y = d.$y;
            }
        }
        else if ( item.$api.isString( d ) )
        {
            text = d;
        }
        else
        {
            throw "Invalid Data Argument: cannot canonicalize data for 'keys': " + JSON.stringify( data );
        }

        if ( text == null || !item.$api.isString( text ) )
        {
            return;
        }


        if ( item.$api.isObject( data[ key ] ) )
        {
            data[ key ].$text = text;
         }
        else
        {
            data[ key ] = {
                "$text": text
            };
        }

        if ( item.$api.isNumeric( x ) && item.$api.isNumeric( y ) )
        {
            data[ key ].$x = x;
            data[ key ].$y = y;
        }
      },

      maybeWaitFor : function( item, data )
      {
          if ( data )
          {
              var until = data.until;
              var timeout = data.timeout;

              log( "maybeWaitFor: item=[" + item.$fullname() + "], path=[" + item + "], until=" + until + ", timeout=[" + timeout + " s].");

              item.$driver.waitFor( item, until, timeout );

              // consume the data value
              delete data.until;
              delete data.timeout;
          }
      },

      maybeDelay : function( item, data )
      {
        if ( data && data.$delay )
        {
          if ( item.$api.isNumeric( data.$delay ) )
          {
              var timeout = data.$delay;

              var Waiter = com.brentcroft.gtd.utilities.Waiter;

              log( "Delaying for [" + timeout + "] seconds." );

              Waiter.delaySeconds( timeout );

              delete data.$delay;
          }
          else
          {
              throw "Invalid Data Argument: value for '$delay' is not numeric: " + JSON.stringify( data );
          }
        }
      },


      maybeExists : function( item, data )
      {
        if ( data )
        {
          if ( item.$api.isNumeric( data ) )
          {
            var timeout = data;

            log( "maybeExists: timeout=[" + timeout + " seconds], item=[" + item.$fullname() + "]." );

            // force conversion for double arg
            try
            {
                if ( item.exists( timeout ) )
                {
                  return;
                }
            }
            catch ( e )
            {
                log( "item.exists failed: " + e);
            }

            throw "Item [" + item.$fullname() + "] did not exist after [" + timeout + " seconds].";
          }
         throw "Invalid Data Error: '$exists' argument must be numeric: " + JSON.stringify( data );
        }
      },

      maybeAnything: function( item, data )
      {
        if ( data && item.$api.isObject( data ) )
        {
            if ( data.$delay )
            {
                item.$api.maybeDelay( item, data );
                delete data.$delay;
            }

            if ( data.$exists )
            {
                item.$api.maybeExists( item, data.$exists );
                delete data.$exists;
            }

            if ( data.$waitFor )
            {
                item.$api.canonicalWaitFor( item, data );
                item.$api.maybeWaitFor(item, data.$waitFor );
                delete data.$waitFor;
            }

            if ( data.$click )
            {
                item.$api.canonicalClick( item, "$click", data );
                item.$api.maybeClick( item, data.$click );
                delete data.$click;
            }

            if ( data.$keys )
            {
                item.$api.canonicalKeys( item, "$keys", data );
                item.$api.maybeKeys(item, data.$keys);
                delete data.$keys;
            }
         }
      },

      maybeAnythingAfter: function( item, data )
      {
        if ( data && item.$api.isObject( data ) )
        {
            if ( data.$checkFor )
            {
                item.$api.canonicalCheckFor( item, data );
                item.$api.maybeWaitFor( item, data.$checkFor )
                delete data.$checkFor;
            }
        }
      },

      maybeClick: function( item, data )
      {
        var count = null;
        var delay = null;
        var x = null;
        var y = null;

        if ( item.$api.isObject( data ) )
        {
            //
            if ( item.$api.isDefined( data.count ) && item.$api.isNumeric( data.count ) )
            {
                count = data.count;
                delete data.count;
            }
            else
            {
                // ok - assume its a passing call
                return;
            }

            if ( item.$api.isNumeric( data.x ) && item.$api.isNumeric( data.y ) )
            {
                x = data.x;
                y = data.y;
            }
        }
        else if ( item.$api.isNumeric( data ) )
        {
            count = data;
        }

        if ( count == null || !item.$api.isNumeric( count ) )
        {
            return;
        }


        if ( count > 0 )
        {
            for (var i = 0; i < count; i++ )
            {
                if ( item.$api.isNumeric( x ) && item.$api.isNumeric( y ) )
                {
                   item.robotClickPoint( x, y );
                }
                else
                {
                   item.click( );
                }

                if ( item.$api.isObject( data ) )
                {
                    item.$api.maybeDelay( item, data );
                }
            }
        }
        else if ( count < 1 )
        {
          // click up to a given number of times
          // or until there's an exception
          // presumably because target has disappeared
          for (var i = 0; i > count; i-- )
          {
              try
              {
                if ( item.$api.isNumeric( x ) && item.$api.isNumeric( y ) )
                {
                   item.robotClickPoint( x, y );
                }
                else
                {
                   item.robotClick( );
                }

                item.$api.maybeDelay( item, data );
              }
              catch ( e )
              {
                  // quit looping
                  log( "maybeClick[ .., " + i + "]: quit looping: " + e );
                  break;
              }
          }
        }
      },

      maybeKeys: function( item, data )
      {
        var text = null;
        var x = null;
        var y = null;

        if ( item.$api.isObject( data ) )
        {
            //
            if ( item.$api.isDefined( data.$text ) && item.$api.isString( data.$text ) )
            {
                text = data.$text;
                delete data.$text;
            }
            else
            {
                // ok - assume its a passing call
                return;
            }

            if ( item.$api.isNumeric( data.$x ) && item.$api.isNumeric( data.$y ) )
            {
                x = data.$x;
                y = data.$y;
            }
        }
        else if ( item.$api.isString( data ) )
        {
            text = data;
        }

        if ( text == null || !item.$api.isString( text ) )
        {
            return;
        }

        if ( item.$api.isNumeric( x ) && item.$api.isNumeric( y ) )
        {
           item.robotKeysPoint( text, x, y );

           log( "maybeKeys: $keys=[" + text + "] control=[" + item.$fullname() + "], xy=[" + x + "," + y + "]." );
        }
        else
        {
           item.robotKeys( text );

           log( "maybeKeys: $keys=[" + text + "] control=[" + item.$fullname() + "]." );
        }
      },


      maybeSetText: function( item, data )
      {
        var text = null;
        var append = false;
        var delay = null;

        if ( item.$api.isObject( data ) )
        {
          if ( data.append )
          {
              append = data.append;
              delete data.append;
          }

          if ( data.$text && item.$api.isString( data.$text ))
          {
              text = data.$text;
              delete data.$text;
          }
          else
          {
              // ok - assume its a passing call
              return;
          }
        }
        else if ( item.$api.isString( data ) )
        {
          text = data;
        }

        if ( text )
        {
            if (append)
            {
              item.setText( item.getText() + text );

              log( "appendText: text=[" + text + "] control=[" + item.$fullname() + "]." );
            }
            else
            {
              item.setText(text);

              log( "setText: text=[" + text + "] control=[" + item.$fullname() + "]." );
            }
        }

        item.$api.maybeDelay( item, data );
      },

      maybeGetText: function( item )
      {
          return { text: item.getText() };
      },


      /*
        INDEX
      */
      maybeSetIndex: function( item, data )
      {
        var index = null;
        var delay = null;

        if ( item.$api.isObject( data ) )
        {
          if ( data.$index && item.$api.isNumeric( data.$index ) )
          {
            index = data.$index;
            delete data.$index;
          }
          else if ( data.$text && item.$api.isString( data.$text ))
          {
            index = item.$api.getIndexForText( item, data.$text );
            delete data.$text;
          }
          else
          {
            // ok - assume its a passing call
            return;
          }
        }
        else if ( item.$api.isNumeric( data ) )
        {
          index = data;
        }
        else if ( item.$api.isString( data ) )
        {
          index = item.$api.getIndexForText( item, data );
        }

        if ( index === null )
        {
          throw "Illegal data argument: could not determine index from: " + JSON.stringify( data )
        }

        item.setIndex( index );

        log( "setIndex: index=[" + index + "] control=[" + item.$fullname() + "]." );

        item.$api.maybeDelay( item, data );
      },

      getIndexForText: function( item, text )
      {
        var xpath = "//c[ contains( @text, '" + XmlUtils.escapeForXmlAttribute( text ) + "' ) ]/@index";

        var index = item.$driver.getComponentResultText(
          item,
          xpath
        );

        log( "getIndexForText: text=[" + text + "] index=[" + index + "] control=[" + item.$fullname() + "] xpath=" + xpath );

        return index;
      },


      maybeGetIndex: function( item )
      {
          var text = null;
          try
          {
              text = item.getText();
          }
          catch ( ignored ) {}

          var data = { index: item.getIndex() };

          if ( text != null )
          {
              data.text = text;
          }

          return data;
      },


      /*
        TABS
      */
      maybeSetTabs: function( item, data )
      {
        var tabIndex = null;

        if ( item.$api.isObject( data ) )
        {
            if ( data.$index && item.$api.isNumeric( data.$index ) )
            {
                tabIndex = data.$index;
                delete data.$index;
            }
            else if ( data.$text && item.$api.isString( data.$text ))
            {
                tabIndex = item.$api.getTabIndexForText( item, data.$text );
                delete data.$text;
            }
            else
            {
                // ok - assume its a passing call
                return;
            }
        }
        else if ( item.$api.isString( data ) )
        {
            tabIndex = item.$api.getTabIndexForText( item, data );
        }
        else if ( item.$api.isNumeric( data ) )
        {
            tabIndex = data;
        }

        if ( tabIndex === null )
        {
            throw "Illegal data argument: could not determine tabsIndex from: " + JSON.stringify( data )
        }

        item.setIndex( tabIndex );

        log( "setTabsIndex: index=[" + index + "] control=[" + item.$fullname() + "]." );

        item.$api.maybeDelay( item, data );
      },


      getTabIndexForText: function( item, text )
      {
        var xpath = "count( //*[ contains( @tab-title, '" + XmlUtils.escapeForXmlAttribute( text ) + "' ) ][ 1 ]/preceding-sibling::*[ @tab-title ] )";

        var index = item.$driver.getComponentResultText(
          item,
          xpath
        );

        log( "getTabIndexForText: text=[" + text + "] index=[" + index + "] control=[" + item.$fullname() + "] xpath=" + xpath );

        return index;
      },


      /*
        TAB
      */
      maybeSetTab: function( tab, data )
      {
        var index = tab.$api.getIndexForTab( tab );

        if ( index === null )
        {
            throw "Illegal data argument: could not determine index from: [" + tab + "]."
        }

        // assuming the tabs ancestor is the tab set
        if ( !tab.$ancestor )
        {
            throw "Illegal data argument: tab has no $ancestor member: [" + tab + "]."
        }

        tab.$ancestor.setIndex( index );

        log( "setTabIndex: index=[" + index + "] control=[" + tab.$fullname() + "]." );

        tab.$api.maybeDelay( tab, data );
      },


      maybeGetTab: function( item )
      {
          return { tab: 0 };
      },


      getIndexForTab: function( tab )
      {
        var xpath = "count( preceding-sibling::*[ @tab-title ] )";
        var index = tab.$driver.getComponentResultText(
          tab,
          xpath
        );

        log( "getIndexForTab: index=[" + index + "] control=[" + tab.$fullname() + "] xpath=" + xpath );

        return index;
      },


      /*
        NODE
      */
      maybeSetNode: function( item, data )
      {
        var path = null;

        if ( item.$api.isObject( data ) )
        {
            if ( data.$path && item.$api.isString( data.$path ) )
            {
                path = data.$path;
                delete data.$path;
            }
            else if ( data.$text && item.$api.isString( data.$text ))
            {
                path = item.$api.getNodePathForText( item, data.$text );
                delete data.$text;
            }
            else
            {
                // ok - assume its a passing call
                return;
            }
        }
        else if ( item.$api.isString( data ) )
        {
            path = item.$api.getNodePathForText( item, data );
        }

        if ( path === null || path == "" )
        {
            throw "Illegal data argument: could not obtain path from: " + JSON.stringify( data )
        }

        item.selectNode( path );


        log( "maybeSetNode: called: " + item.$fullname() + ".setPath( " + path + " )." );


        item.$api.maybeDelay( item, data );
      },


      maybeGetNode: function( item )
      {
          return { path: 1 };
      },


      getNodePathForText: function( item, text )
      {
        var xpath = "//n[ contains( @text, '" + XmlUtils.escapeForXmlAttribute( text ) + "' ) ]/@path";

        var path = item.$driver.getComponentResultText(
          item,
          xpath
        );

        log( "getNodePathForText: path=[" + path + "] control=[" + item.$fullname() + "] text=[" + text + "] xpath=" + xpath );

        return path;
      },



      /*
        TABLE
      */
      maybeSetTable: function( item, data )
      {
        var row = null;
        var column = null;
        var delay = null;

        if ( item.$api.isObject( data ) )
        {
            // data.$row may be 0
            if ( item.$api.isDefined( data.$row ) )
            {
                row = ( item.$api.isNumeric( data.$row ) )
                    ? row = data.$row
                    : item.$api.getRowForText( item, data.$row );

                delete data.$row;

                if ( item.$api.isDefined( data.$column ) )
                {
                    column = ( item.$api.isNumeric( data.$column ) )
                        ? data.$column
                        : "0";

                    delete data.$column;
                }
            }
            else if ( item.$api.isDefined( data.$text ) && item.$api.isString( data.$text ))
            {
                row = item.$api.getRowForText( item, data.$text );
                delete data.$text;
            }
            else
            {
                // ok - assume its a passing call
                return;
            }
        }
        else if ( item.$api.isNumeric( data ) )
        {
            row = data;
        }
        else if ( item.$api.isString( data ) )
        {
            row = item.$api.getRowForText( item, data );
        }

        if ( row === null || row == "" )
        {
            throw "Illegal data argument: could not obtain row from: " + JSON.stringify( data )
        }

        if ( column )
        {
            item.selectCell( row, column );

            log( "maybeSetTable: called: " + item.$fullname() + ".setCell( " + row + ", " + column + " )." );
        }
        else
        {
            item.selectRow( row );

            log( "maybeSetTable: called: " + item.$fullname() + ".setRow( " + row + " )." );
        }


        item.$api.maybeDelay( item, data );
      },


      maybeGetTable: function( item )
      {
          return { row: 1, cell: 1 };
      },


      getRowForText: function( item, text )
      {
        var xpath = "//r[ c[ contains( @text, '" + XmlUtils.escapeForXmlAttribute( text ) + "' ) ] ]/@index";

        var index = item.$driver.getComponentResultText( item, xpath );

        log( "getRowForText: index=[" + index + "] control=[" + item.$fullname() + "] text=[" + text + "] xpath=" + xpath );

        return index;
      }
    }
};


// install api into prototypes
(
    function upgradePrototypes()
    {
        var objectPrototypes = gtdApi.objectPrototypes;

        objectPrototypes.GuiBase.prototype.$api = gtdApi.api;


        objectPrototypes.GuiBase.prototype.set = function( data ) { this.$api.dataDriven( this, data ); };
        objectPrototypes.GuiBase.prototype.get = function() { return this.$api.dataExtractJson( this ); };


        // TODO: remove as replaced by get and set
        /*
        objectPrototypes.GuiObject.prototype.dataDriven = function( data ) { this.$api.dataDriven( this, data ); };
        objectPrototypes.GuiObject.prototype.dataExtract = function() { return this.$api.dataExtractJson( this ); };
        */

        //
        objectPrototypes.GuiObject.prototype.$input = function( data ) { this.$api.maybeAnything( this, data ); this.$api.maybeAnythingAfter( this, data ) };

        objectPrototypes.GuiClickObject.prototype.$input = function( data ) { this.$api.maybeAnything( this, data ); this.$api.maybeClick( this, data ); this.$api.maybeAnythingAfter( this, data ) };
        objectPrototypes.GuiClickObject.prototype.$output = function( data ) { return { count: 0 } };

        objectPrototypes.GuiTextObject.prototype.$input = function( data ) { this.$api.maybeAnything( this, data ); this.$api.maybeSetText( this, data ); this.$api.maybeAnythingAfter( this, data ) };
        objectPrototypes.GuiTextObject.prototype.$output = function() { return $api.maybeGetText( this ); };

        objectPrototypes.GuiIndexObject.prototype.$input = function( data ) { this.$api.maybeAnything( this, data ); this.$api.maybeSetIndex( this, data ); this.$api.maybeAnythingAfter( this, data ) };
        objectPrototypes.GuiIndexObject.prototype.$output = function() { return this.$api.maybeGetIndex( this ); };

        objectPrototypes.GuiTabObject.prototype.$input = function( data ) { this.$api.maybeAnything( this, data ); this.$api.maybeSetTab( this, data ); this.$api.maybeAnythingAfter( this, data ) };
        objectPrototypes.GuiTabObject.prototype.$output = function( data ) { return api.maybeGetTab( this ) };

        // overriding just the input
        objectPrototypes.GuiTabsObject.prototype.$input = function( data ) { this.$api.maybeAnything( this, data );  this.$api.maybeSetTabs( this, data ); this.$api.maybeAnythingAfter( this, data ) };


        objectPrototypes.GuiTableObject.prototype.$input = function( data ) { this.$api.maybeAnything( this, data ); this.$api.maybeSetTable( this, data ); this.$api.maybeAnythingAfter( this, data ) };
        objectPrototypes.GuiTableObject.prototype.$output = function() { return this.$api.maybeGetTable( this ); };

        objectPrototypes.GuiTreeObject.prototype.$input = function( data ) { this.$api.maybeAnything( this, data ); this.$api.maybeSetNode( this, data ); this.$api.maybeAnythingAfter( this, data ) };
        objectPrototypes.GuiTreeObject.prototype.$output = function() { return this.$api.maybeGetNode( this ); };


        log( "Template.Model API: installed data driven api." );

        /*
            collect a new instance of each
            mapped to an action key
        */
        gtdApi.objectPrototypesInstances = {
            "base": new objectPrototypes.GuiBase(),
            "robot": new objectPrototypes.GuiObject(),
            "tab": new objectPrototypes.GuiTabObject(),
            "click": new objectPrototypes.GuiClickObject(),
            "text": new objectPrototypes.GuiTextObject(),
            "richtext": new objectPrototypes.GuiRichTextObject(),
            "index": new objectPrototypes.GuiIndexObject(),
            "table": new objectPrototypes.GuiTableObject(),
            "tabs": new objectPrototypes.GuiTabsObject(),
            "tree": new objectPrototypes.GuiTreeObject()
        };

        gtdApi.applyPrototype = function( item, prototypeKey ) {
            Object.setPrototypeOf( item, this.objectPrototypesInstances[ prototypeKey ] );
        };

        log( "Template.Model API: installed action mappings." );
    }
)();




