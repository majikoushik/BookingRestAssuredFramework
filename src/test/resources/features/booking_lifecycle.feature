Feature: Booking lifecycle via API

  Background:
    Given I have a valid auth token

  Scenario: Create, get, update and delete a booking
    When I create a booking for "Jim" "Brown"
    Then the booking should be created successfully

    When I fetch the booking by id
    Then the booking details should be "Jim" "Brown"

    When I update the booking to add "Breakfast and Dinner" as additional needs
    Then the updated booking should have "Breakfast and Dinner" as additional needs

    When I delete the booking
    Then the booking should no longer exist
