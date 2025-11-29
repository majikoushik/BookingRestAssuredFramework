package com.booking.tests.bdd.steps;

import com.booking.tests.models.BookingClient;
import com.booking.tests.models.BookingModels;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PostBookingDataTableSteps {

    private final BookingClient client = new BookingClient();

    private BookingModels.Booking bookingRequest;                 // Request built from DataTable
    private Response rawResponse;                                 // Raw HTTP response
    private BookingModels.CreateBookingResponse bookingResponse;  // Parsed response body
    private Integer expectedStatusCode;                           // For later checks

    @When("I create a booking without template using data")
    @Step("Create booking from DataTable without JSON template")
    public void i_create_a_booking_without_template_using_data(DataTable dataTable) {

        // 1) Convert DataTable (key | value) to Map<String, String>
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        // 2) Parse dates from strings (YYYY-MM-DD)
        LocalDate checkin = LocalDate.parse(data.get("checkin"));
        LocalDate checkout = LocalDate.parse(data.get("checkout"));

        BookingModels.BookingDates dates =
                new BookingModels.BookingDates(checkin, checkout);

        // 3) Build BookingModels.Booking directly (no JSON template at all)
        bookingRequest = new BookingModels.Booking();
        bookingRequest.firstname       = data.get("firstname");
        bookingRequest.lastname        = data.get("lastname");
        bookingRequest.totalprice      = Integer.parseInt(data.get("totalprice"));
        bookingRequest.depositpaid     = Boolean.parseBoolean(data.get("depositpaid"));
        bookingRequest.bookingdates    = dates;
        bookingRequest.additionalneeds = data.get("additionalneeds");

        // 4) Call API via BookingClient (raw Response, so we can assert status & body)
        rawResponse = client.createRaw(bookingRequest);
    }

    @Then("the datatable booking response status code should be {int}")
    @Step("Verify datatable booking response status code = {statusCode}")
    public void the_datatable_booking_response_status_code_should_be(Integer statusCode) {
        expectedStatusCode = statusCode;

        assertThat(rawResponse)
                .as("Raw response must not be null")
                .isNotNull();

        rawResponse.then()
                .statusCode(statusCode);
    }

    @Then("the datatable booking details should match:")
    @Step("Verify datatable booking response body matches DataTable")
    public void the_datatable_booking_details_should_match(DataTable expectedTable) {

        assertThat(rawResponse)
                .as("Raw response must not be null before verifying body")
                .isNotNull();

        // Only verify body for successful 200 responses
        if (expectedStatusCode != null && expectedStatusCode == 200) {
            bookingResponse = rawResponse.then()
                    .extract()
                    .as(BookingModels.CreateBookingResponse.class);

            Map<String, String> expected =
                    expectedTable.asMap(String.class, String.class);

            assertThat(bookingResponse).isNotNull();
            assertThat(bookingResponse.booking).isNotNull();

            // Compare simple fields
            assertThat(bookingResponse.booking.firstname)
                    .as("firstname")
                    .isEqualTo(expected.get("firstname"));

            assertThat(bookingResponse.booking.lastname)
                    .as("lastname")
                    .isEqualTo(expected.get("lastname"));

            assertThat(bookingResponse.booking.totalprice)
                    .as("totalprice")
                    .isEqualTo(Integer.parseInt(expected.get("totalprice")));

            assertThat(bookingResponse.booking.depositpaid)
                    .as("depositpaid")
                    .isEqualTo(Boolean.parseBoolean(expected.get("depositpaid")));

            // Compare nested bookingdates
            assertThat(bookingResponse.booking.bookingdates)
                    .as("bookingdates")
                    .isNotNull();

            assertThat(bookingResponse.booking.bookingdates.checkin)
                    .as("checkin")
                    .isEqualTo(LocalDate.parse(expected.get("checkin")));

            assertThat(bookingResponse.booking.bookingdates.checkout)
                    .as("checkout")
                    .isEqualTo(LocalDate.parse(expected.get("checkout")));

            // Compare additionalneeds
            assertThat(bookingResponse.booking.additionalneeds)
                    .as("additionalneeds")
                    .isEqualTo(expected.get("additionalneeds"));
        }
    }
}
