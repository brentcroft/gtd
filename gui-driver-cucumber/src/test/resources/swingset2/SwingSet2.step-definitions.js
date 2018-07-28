steps = {
	"runADemo": {
      "name" : "^I demo (\\w+)$",
	  "test" : [ 
		"And I demo InternalFramesDemo",
		"And I demo ButtonsDemo"
	  ],
      "step" : function( name ) 
      {
		try {
			demo[ name ].doDemo();
		} catch ( e ) {
			throw "Error running demo [" + name + "] " + e;
		}
      }
	}	
};