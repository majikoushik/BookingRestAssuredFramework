#This feature file use json template for reading the data table value
@booking @api @post
Feature: Post Booking

  Scenario Outline: Create booking with different data
    Given I have booking template "booking_payload.json"
    When I create a booking with data
      | firstname       | <firstname>       |
      | lastname        | <lastname>        |
      | totalprice      | <totalprice>      |
      | depositpaid     | <depositpaid>     |
      | checkin         | <checkin>         |
      | checkout        | <checkout>        |
      | additionalneeds | <additionalneeds> |
    Then the booking response status code should be <status_code>
    Then the booking firstname should be '<firstname>'

    Examples:
      | status_code | firstname | lastname | totalprice | depositpaid | checkin    | checkout   | additionalneeds |
      | 200         | John      | Doe      | 100        | true        | 2025-12-01 | 2025-12-10 | Breakfast       |
      | 200         | Jane      | Smith    | 200        | false       | 2025-12-05 | 2025-12-15 | Lunch           |

