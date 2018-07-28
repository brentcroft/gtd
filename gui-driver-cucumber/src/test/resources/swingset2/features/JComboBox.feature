Feature: Verify JComboBox demo

Scenario Outline: Show Individuals

  Given I'm logged in to "SwingSet2"

  And I click on $ss2.toolbar.JComboBox_demo

  Then with object $ss2.tabs.ComboBox_Demo set
  """
  {
    "Presets": <presets>,
    "Hair": {
      "$text": "<hair>",
      "$checkFor": "//*[ contains( @text, '<hair>' ) ]"
    },
     "Eyes_Nose": {
      "$text": "<eyes_nose>",
      "$checkFor": "//*[ contains( @text, '<eyes_nose>' ) ]"
    },
    "Mouth": {
      "$text": "<mouth>",
      "$checkFor": "//*[ contains( @text, '<mouth>' ) ]"
    }
  }
  """

Examples:
  | presets | hair  | eyes_nose | mouth |
  |  4      | Scott | Lisa      | Lara  |
  |  3      | James | Scott     | Lisa  |
  |  2      | Lara  | James     | Scott |
  |  1      | Lisa  | Lisa      | Lisa  |