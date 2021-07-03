Feature: Get the statuses of the candy machine

  It should be possible to get the statuses of all the candy machines in the park.

  Background:
    Given a park of candy machines

  Scenario: Get the statuses of all the candy machine in the park
    Given a candy machine
    When checking the statuses of the candy machines in the park
    Then the status of the candy machines should be returned, sorted by id