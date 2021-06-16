Feature: Insert a coin in the candy machine

  Scenario: Insert a coin in a non existing machine
    Given a park of candy machines
    When the customer inserts a coin in a unknown candy machine
    Then the customer should be notified about the problem

  Scenario: Insert a coin in a locked machine
    Given a park of candy machines
    When a coin is inserted in a locked candy machine
    Then the candy machine should be unlocked

  Scenario: Insert a coin in a unlocked machine
    Given a park of candy machines
    When a coin is inserted in a unlocked candy machine
    Then the coin should be rejected