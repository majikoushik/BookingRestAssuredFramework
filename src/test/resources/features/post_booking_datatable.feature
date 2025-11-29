@booking @api @post @datatable
Feature: Post Booking using DataTable only (no JSON template)

  # This scenario creates a booking using a DataTable (no JSON template)
  # and then verifies that ALL response fields match the input.

  Scenario Outline: Create booking and verify all fields using DataTable only
    When I create a booking without template using data
      | firstname       | <firstname>       |
      | lastname        | <lastname>        |
      | totalprice      | <totalprice>      |
      | depositpaid     | <depositpaid>     |
      | checkin         | <checkin>         |
      | checkout        | <checkout>        |
      | additionalneeds | <additionalneeds> |
    Then the datatable booking response status code should be 200
    And the datatable booking details should match:
      | firstname       | <firstname>       |
      | lastname        | <lastname>        |
      | totalprice      | <totalprice>      |
      | depositpaid     | <depositpaid>     |
      | checkin         | <checkin>         |
      | checkout        | <checkout>        |
      | additionalneeds | <additionalneeds> |

    Examples:
      | firstname | lastname | totalprice | depositpaid | checkin    | checkout   | additionalneeds        |
      | Alice     | Green    | 150        | true        | 2025-12-01 | 2025-12-05 | Breakfast              |
      | Bob       | White    | 300        | false       | 2025-12-10 | 2025-12-12 | Late checkout, Dinner  |
