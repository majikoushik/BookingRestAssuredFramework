package com.booking.tests.bdd.steps;

import com.booking.tests.core.SpecFactory;
import com.booking.tests.models.BookingClient;
import com.booking.tests.models.BookingModels;
import io.cucumber.java.en.*;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class BookingLifecycleSteps {

    private final BookingClient client = new BookingClient();

    private String token;
    private BookingModels.Booking request;
    private BookingModels.CreateBookingResponse createResp;
    private BookingModels.Booking fetched;
    private int bookingId;

    @Given("I have a valid auth token")
    public void i_have_a_valid_auth_token() {
        token = client.createToken();
        assertThat(token).isNotBlank();
    }

    @When("I create a booking for {string} {string}")
    public void i_create_a_booking_for(String firstname, String lastname) {
        // Build the Booking request using public fields
        request = new BookingModels.Booking();
        request.firstname = firstname;
        request.lastname = lastname;
        request.totalprice = 123;
        request.depositpaid = true;

        // BookingDates is most likely a separate nested class: BookingModels.BookingDates
        BookingModels.BookingDates dates = new BookingModels.BookingDates();
        dates.checkin = LocalDate.of(2025,01,01);
        dates.checkout = LocalDate.of(2025,01,05);
        request.bookingdates = dates;

        request.additionalneeds = "Breakfast";

        // Call BookingClient.create()
        createResp = client.create(request);
        bookingId = createResp.bookingid;  // access public field, not getBookingid()
    }

    @Then("the booking should be created successfully")
    public void the_booking_should_be_created_successfully() {
        assertThat(createResp).isNotNull();
        assertThat(createResp.bookingid).isNotNull();
        assertThat(createResp.booking).isNotNull();
    }

    @When("I fetch the booking by id")
    public void i_fetch_the_booking_by_id() {
        fetched = client.get(bookingId);
    }

    @Then("the booking details should be {string} {string}")
    public void the_booking_details_should_be(String expectedFirst, String expectedLast) {
        assertThat(fetched.firstname).isEqualTo(expectedFirst);
        assertThat(fetched.lastname).isEqualTo(expectedLast);
    }

    @When("I update the booking to add {string} as additional needs")
    public void i_update_the_booking_to_add_as_additional_needs(String additionalNeeds) {
        // Update the field directly
        request.additionalneeds = additionalNeeds;

        // Send update using BookingClient
        BookingModels.Booking updated = client.update(bookingId, request, token);
        fetched = updated; // keep latest state for further assertions
    }

    @Then("the updated booking should have {string} as additional needs")
    public void the_updated_booking_should_have_as_additional_needs(String expectedNeeds) {
        assertThat(fetched.additionalneeds).isEqualTo(expectedNeeds);
    }

    @When("I delete the booking")
    public void i_delete_the_booking() {
        client.delete(bookingId, token);
    }

    @Then("the booking should no longer exist")
    public void the_booking_should_no_longer_exist() {
        // Negative check using raw Rest Assured + SpecFactory
        given()
                .spec(SpecFactory.requestJson())
                .when()
                .get("/booking/{id}", bookingId)
                .then()
                .statusCode(404);
    }
}
