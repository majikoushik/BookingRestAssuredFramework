package com.booking.tests.booking;



import com.booking.tests.models.BookingClient;
import com.booking.tests.models.BookingModels.*;
import com.booking.tests.support.Schemas;
import io.qameta.allure.Epic;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Contracts")
class SchemaContractTest extends com.booking.tests.support.BaseTest{

    private final BookingClient client = new BookingClient();
    private final Faker faker = new Faker();

    @Test
    void authResponse_matchesSchema() {
        var token = client.createToken();
        assertThat(token).isNotBlank();

        // Re-hit /auth to validate schema on-wire (not just extracted)
        given()
                .baseUri("https://restful-booker.herokuapp.com")
                .contentType("application/json")
                .body("""
        {"username":"admin","password":"password123"}
      """)
                .when().post("/auth")
                .then().statusCode(200)
                .body(Schemas.auth());
    }

    @Test
    void createAndGetBooking_matchSchemas() {
        var req = new Booking();
        req.firstname = faker.name().firstName();
        req.lastname = faker.name().lastName();
        req.totalprice = faker.number().numberBetween(90, 300);
        req.depositpaid = true;
        req.bookingdates = new BookingDates(LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 22));
        req.additionalneeds = "Breakfast";

        // Validate create response
        given()
                .baseUri("https://restful-booker.herokuapp.com")
                .contentType("application/json")
                .body(req)
                .when().post("/booking")
                .then().statusCode(200)
                .body(Schemas.createResp());

        // Fetch the created booking id using typed client then validate GET schema
        var created = new BookingClient().create(req);
        given()
                .baseUri("https://restful-booker.herokuapp.com")
                .when().get("/booking/{id}", created.bookingid)
                .then().statusCode(200)
                .body(Schemas.booking());
    }
}
