Feature: Add a candy machine to the park

  It should be possible to add an arbitrary amount of candy machines to the park.

  Scenario: Add a machine to the park
    Given a park of candy machines
    When another candy machine is added
    Then the machine should be allocated an unique id
    And the machine should be added to the park