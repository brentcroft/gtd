Feature: Verify JComboBox demo

Scenario: Show Individuals

  Given I'm logged in to "SwingSet2"

  And I click on $ss2.toolbar.JComboBox_demo

  Then with object $ss2.tabs.ComboBox_Demo set
  """
  {
    "Presets": 4,
    "Hair": {
      "$text": "Scott",
      "$checkFor": "//*[ contains( @text, 'Scott' ) ]"
    },
     "Eyes_Nose": {
      "$text": "Scott",
      "$checkFor": "//*[ contains( @text, 'Scott' ) ]"
    },
    "Mouth": {
      "$text": "Scott",
      "$checkFor": "//*[ contains( @text, 'Scott' ) ]"
    }
  }
  """