Feature: Verify JTabbedPane

  Background: 
    Given I'm logged in to "SwingSet2"  
    And I click on $ss2.toolbar.JTabbedPane_demo

  Scenario Outline: Show Tabs
  	And with object $ss2.tabs.TabbedPane_Demo set
    """
    {
      "<place>": 1,
      "tabs": "<tab>"
    }
    """
    
    Examples:
    | place  | tab      |
    | Right  | Laine    |
    | Left   | Ewan     |
    | Top    | Hania    |
    | Bottom | Bouncing |
    | Left   | Hania     |