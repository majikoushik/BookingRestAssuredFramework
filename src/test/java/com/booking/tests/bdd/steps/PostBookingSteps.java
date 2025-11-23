package com.booking.tests.bdd.steps;

import com.booking.tests.models.BookingClient;
import com.booking.tests.models.BookingModels;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PostBookingSteps {

    private final BookingClient client = new BookingClient();

    private String jsonTemplate;                                  // RAW template with {{placeholders}}
    private BookingModels.Booking bookingRequest;                 // Request object built from template + Examples data
    private Response rawResponse;                                 // Raw HTTP response (for all status codes)
    private BookingModels.CreateBookingResponse bookingResponse;  // Parsed response (for 200 only)
    private Integer expectedStatusCode;                           // Store from Examples table

    @Given("I have booking template {string}")
    @Step("Load booking template: {templateName}")
    public void i_have_booking_template(String templateName) throws Exception {
        String path = "payloads/" + templateName;

        InputStream is = getClass().getClassLoader().getResourceAsStream(path);
        assertThat(is)
                .as("Template file %s must exist under src/test/resources/payloads", path)
                .isNotNull();

        jsonTemplate = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        assertThat(jsonTemplate).isNotBlank();
    }

    @When("I create a booking with data")
    @Step("Create booking from Scenario Outline data")
    public void i_create_a_booking_with_data(DataTable dataTable) throws Exception {
        // Convert DataTable (key | value) into Map<String, String>
        Map<String, String> data = dataTable.asMap(String.class, String.class);

        // Replace placeholders {{key}} in the JSON template
        String resolvedJson = jsonTemplate;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();       // e.g. "firstname"
            String value = entry.getValue();   // e.g. "John" or "100" or "true"
            String placeholder = "{{" + key + "}}";
            if (value == null) {
                System.out.println("WARNING: DataTable value for key '" + key + "' is null. Replacing with empty string.");
                value = "";
            }
            resolvedJson = resolvedJson.replace(placeholder, value);
        }

        // Map resolved JSON into BookingModels.Booking using Jackson
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        bookingRequest = mapper.readValue(resolvedJson, BookingModels.Booking.class);

        // Call BookingClient.createRaw() so we can assert any status code (200, 400, etc.)
        rawResponse = client.createRaw(bookingRequest);
    }

    @Then("the booking response status code should be {int}")
    @Step("Verify booking response status code = {statusCode}")
    public void the_booking_response_status_code_should_be(Integer statusCode) {
        expectedStatusCode = statusCode;
        assertThat(rawResponse).as("Raw response must not be null").isNotNull();
        rawResponse.then().statusCode(statusCode);
    }

    @Then("the booking firstname should be {string}")
    @Step("Verify booking firstname when status is 200")
    public void the_booking_firstname_should_be(String expectedFirstname) {
        // Only verify firstname for successful (200) responses.
        // For negative cases (400, etc.), we typically don't expect a booking object at all.
        if (expectedStatusCode != null && expectedStatusCode == 200) {
            bookingResponse = rawResponse.then()
                    .extract()
                    .as(BookingModels.CreateBookingResponse.class);

            assertThat(bookingResponse).isNotNull();
            assertThat(bookingResponse.booking).isNotNull();
            assertThat(bookingResponse.booking.firstname).isEqualTo(expectedFirstname);
        } else {
            // For non-200 cases, you can either:
            // - assert that 'booking' is null,
            // - or just document that firstname check is not meaningful.
            // For teaching, keeping it simple is fine:
            assertThat(expectedFirstname.trim())
                    .as("For non-200 responses, firstname in Examples is just a placeholder.")
                    .isEmpty();
        }
    }
}
