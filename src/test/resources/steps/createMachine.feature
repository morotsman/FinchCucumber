Feature: Add a candy machine to the park

  It should be possible to add an arbitrary amount of candy machines to the park.

  Background:
    Given a park of candy machines

  Scenario: Add a machine to the park
    Given a candy machine
    When the candy machine is added to the park
    Then the machine should be allocated an unique id
    And the machine should be added to the park