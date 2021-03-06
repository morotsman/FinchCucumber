Feature: Insert a coin in the candy machine

  Background:
    Given a park of candy machines

  Scenario: Insert a coin in a locked machine with candies
    Given an locked machine with candies
    When a coin is inserted in the candy machine
    Then the candy machine should be unlocked

  Scenario: Insert a coin in a machine that has not been added to the park
    Given a candy machine
    When the customer inserts a coin in a candy machine that has not been added to the park
    Then the coin should be rejected

  Scenario: Insert a coin in a locked machine without candies
    Given an locked machine without candies
    When a coin is inserted in the candy machine
    Then the coin should be rejected

  Scenario: Insert a coin in an unlocked machine
    Given an unlocked machine
    When a coin is inserted in the candy machine
    Then the coin should be rejected
