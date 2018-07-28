Feature: Verify JTree

  Background:

    Given I'm logged in to "SwingSet2"

  Scenario: JTree data driven

    And I click on $ss2.toolbar.JTree_demo

    Then with object $ss2.tabs.Tree_Demo.TreeDemo_1 set
    """
    [
      { "$path": "1:2:3:4" },
      { "$text": "Fly Like An Eagle" },

      "No. 2 - D Minor"
    ]
    """



  Scenario: JTree experiment

    And I click on $ss2.toolbar.JTree_demo

    Then execute
    """
    var item = ss2.tabs.Tree_Demo.TreeDemo_1;

	// this illustrates  a snapshot focused on the tree
    // which is the whole model of the tree.
    // a path can be found for any node that contains
    // specified text
    var options = new java.util.HashMap;
    options.put( "MAX_TREE_DEPTH", 100 );
    print( item.$driver.getSnapshotXmlText( item, options ) );


    // same data object as above
    // but doesn't have to be strict JSON
    var data = [
      { $path: "1:2:3:4" },
      { $text: "Fly Like An Eagle" },

      "No. 2 - D Minor"
    ];

    item.set( data );
    """

