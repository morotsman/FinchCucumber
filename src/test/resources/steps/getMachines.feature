Feature: Get the statuses of the candy machine

  It should be possible to get the statuses of all the candy machines in the park.

  Scenario: Get the statuses of all the candy machine in the park
    Given a park of candy machines
    And a candy machine
    When checking the statuses of the candy machines in the park
    Then the status of the candy machines should be returned, sorted by id