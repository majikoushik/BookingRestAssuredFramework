package com.booking.tests.bdd.steps;

import com.booking.tests.core.SpecFactory;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingNegativeSteps {

    private Response response;

    @When("I post a valid booking JSON to path {string}")
    @Step("POST valid booking JSON to path: {path}")
    public void i_post_a_valid_booking_json_to_path(String path) {
        String body = """
        {
          "firstname": "John",
          "lastname": "Doe",
          "totalprice": 123,
          "depositpaid": true,
          "bookingdates": {
            "checkin": "2025-01-01",
            "checkout": "2025-01-03"
          },
          "additionalneeds": "Breakfast"
        }
        """;

        response = given()
                .spec(SpecFactory.requestJson())   // reuse your global HTTP config
                .body(body)
                .when()
                .post(path);
    }

    @When("I post malformed JSON to path {string}")
    @Step("POST malformed JSON to path: {path}")
    public void i_post_malformed_json_to_path(String path) {
        // Deliberately broken JSON (missing quotes/commas etc.)
        String badBody = "{ this is : not valid json";

        response = given()
                .spec(SpecFactory.requestJson())
                .body(badBody)
                .when()
                .post(path);
    }

    @Then("the negative response status code should be {int}")
    @Step("Verify negative response status code = {statusCode}")
    public void the_negative_response_status_code_should_be(int statusCode) {
        assertThat(response).as("Response should not be null").isNotNull();
        response.then().statusCode(statusCode);
    }
}
