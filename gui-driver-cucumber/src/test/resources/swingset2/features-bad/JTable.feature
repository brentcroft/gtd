Feature: Verify JTable

  Background:

    Given I'm logged in to "SwingSet2"

    And I click on $ss2.toolbar.JTable_demo

  Scenario: JTable good row search

    Then execute
    """
    var item = ss2.tabs.Table_Demo.JTable;

    item.set( "Zelony" );
    """


  Scenario: JTable bad row searches 1

    Then execute
    """
    var item = ss2.tabs.Table_Demo.JTable;

    var data = [
      "shouldn't exist",
      "doesn't exist",
      "won't exist"
    ];

    item.set( data );
    """

  Scenario: JTable bad row searches 2

    Then execute
    """
    var item = ss2.tabs.Table_Demo.JTable;

    var data = [
      "shouldn't exist either",
      "doesn't exist nope",
      "won't exist I tell you"
    ];

    item.set( data );
    """