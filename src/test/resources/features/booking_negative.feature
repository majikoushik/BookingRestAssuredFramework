@booking @api @negative
Feature: Negative booking API behaviour

  Scenario: Posting to an invalid booking endpoint should return 404
    When I post a valid booking JSON to path "/booking-invalid"
    Then the negative response status code should be 404

  Scenario: Posting malformed JSON to the booking endpoint should return an error
    When I post malformed JSON to path "/booking"
    Then the negative response status code should be 400



