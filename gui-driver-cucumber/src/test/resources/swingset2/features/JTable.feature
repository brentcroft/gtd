Feature: Verify JTable

  Background: 

    Given I'm logged in to "SwingSet2"
  
    And I click on $ss2.toolbar.JTable_demo



  Scenario: JTable experiment

    Then execute
    """
    var item = ss2.tabs.Table_Demo.JTable;

	// this illustrates  a snapshot focused on the table
    // which is the whole model of the table.
    // a row or cell or column can be found for any cell that contains
    // specified text
    var options = new java.util.HashMap;   
    
    options.put( "MAX_TABLE_ROWS", 100 );   
    options.put( "MAX_TABLE_COLUMNS", 100 );
    
    print( item.$driver.getSnapshotXmlText( item, options ) );
    
    
    // same data object as above
    // but doesn't have to be strict JSON
    var data = [
      "Spinal",
      "Withnail",
      "(1962)",
      "Fifth"
    ];

    item.set( data );
    """


  Scenario: Cell Selection set data
  	And with object $ss2.tabs.Table_Demo.JTable set
    """
    [
	  { "$column": 9, "$row": 3 },
	  { "$column": 10, "$row": 2 },
	  { "$column": 11, "$row": 1 },
      "Spinal",
      "Withnail",
      "(1962)",
      "Fifth"
    ]
    """




  Scenario: Cell Selection longhand
    And I select cell 1,1 from $ss2.tabs.Table_Demo.JTable
    And I select cell 2,2 from $ss2.tabs.Table_Demo.JTable
    And I select cell 3,3 from $ss2.tabs.Table_Demo.JTable


  Scenario: Row Selection set data
  	And with object $ss2.tabs.Table_Demo.JTable set
    """
    [
	    { "$row": 3 },
	    { "$row": 2 },
	    { "$row": 1 }
    ]
    """





  Scenario: Row Selection longhand
    And I select row 4 from $ss2.tabs.Table_Demo.JTable
    And I select row 3 from $ss2.tabs.Table_Demo.JTable
    And I select row 40 from $ss2.tabs.Table_Demo.JTable
    And I select row 5 from $ss2.tabs.Table_Demo.JTable
    And I select row 37 from $ss2.tabs.Table_Demo.JTable