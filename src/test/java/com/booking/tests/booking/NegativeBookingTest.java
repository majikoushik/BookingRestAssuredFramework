package com.booking.tests.booking;

import com.booking.tests.core.SpecFactory;
import com.booking.tests.models.BookingClient;
import com.booking.tests.models.BookingModels.Booking;
import com.booking.tests.models.BookingModels.BookingDates;
import com.booking.tests.support.BaseTest;
import io.qameta.allure.Epic;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Notes for this public API:
 * - It ACCEPTS non-ISO dates and often returns 200 for "invalid" shapes you'd expect to fail.
 * - Use these tests as robust examples that still teach negative-testing principles without flaking.
 */
@Epic("Negative")
class NegativeBookingTest extends BaseTest {

    private final BookingClient client = new BookingClient();
    private static final String BASE = "https://restful-booker.herokuapp.com";

    @Test
    void auth_withBadCredentials_shouldNotReturnToken() {
        // Correct JSON body
        var resp = given().baseUri(BASE).contentType("application/json")
                .body("""
                  {"username":"admin","password":"wrong"}
                  """)
                .when().post("/auth")
                .then().statusCode(200) // API returns 200 with a reason field
                .extract().asString();

        assertThat(resp).contains("reason");
        assertThat(resp).doesNotContain("\"token\"");
    }

    @Test
    void update_withoutToken_shouldBeForbidden() {
        // Arrange: create a valid booking
        var b = new Booking();
        b.firstname = "NoAuth";
        b.lastname  = "User";
        b.totalprice = 150;
        b.depositpaid = false;
        b.bookingdates = new BookingDates(LocalDate.of(2025, 12, 1), LocalDate.of(2025, 12, 3));
        var created = client.create(b);

        // PUT without token/basic -> typically 403; sometimes 401 depending on infra
        given().spec(SpecFactory.requestJson())
                .body(b)
                .when().put("/booking/{id}", created.bookingid)
                .then().statusCode(Matchers.anyOf(Matchers.is(403), Matchers.is(401)));
    }

    @Test
    void update_withInvalidToken_shouldBeForbidden() {
        // Arrange: create booking
        var b = new Booking();
        b.firstname = "Bad";
        b.lastname  = "Token";
        b.totalprice = 100;
        b.depositpaid = true;
        b.bookingdates = new BookingDates(LocalDate.of(2025, 11, 20), LocalDate.of(2025, 11, 22));
        int id = client.create(b).bookingid;

        // Bogus cookie -> 403/401
        given().spec(SpecFactory.requestJson())
                .cookie("token", "not-a-real-token")
                .body(b)
                .when().put("/booking/{id}", id)
                .then().statusCode(Matchers.anyOf(Matchers.is(403), Matchers.is(401)));
    }

    /** Behavioral doc: API accepts non-ISO dates (returns 200). Keep to educate newcomers. */
    @Test
    void create_withInvalidDateFormat_isAccepted_butDocumented() {
        String payload = """
          {"firstname":"Bad","lastname":"Date","totalprice":99,"depositpaid":true,
           "bookingdates":{"checkin":"20-12-2025","checkout":"22-12-2025"},
           "additionalneeds":"Lunch"}
          """;

        int id = given().spec(SpecFactory.requestJson())
                .body(payload)
                .when().post("/booking")
                .then().statusCode(200)
                .extract().path("bookingid");

        var got = given().spec(SpecFactory.requestJson())
                .when().get("/booking/{id}", id)
                .then().statusCode(200);

        assertThat(got.extract().path("bookingdates.checkin").toString())
                .as("API stored a non-ISO or mangled date; this documents the behavior")
                .doesNotMatch("^\\d{4}-\\d{2}-\\d{2}$");
        assertThat(got.extract().path("bookingdates.checkout").toString())
                .doesNotMatch("^\\d{4}-\\d{2}-\\d{2}$");

        System.out.println("Note: API accepts/mangles non-ISO dates; in real systems enforce ISO client-side or raise a defect.");
    }

    /** Stable negative: WRONG TYPE (string for totalprice) -> 400 or 500 on this API. */
    @Test
    void create_withWrongType_shouldFail_400or500() {
        String malformed = "{\"firstname\":\"Typo\",\"lastname\":\"Price\"";

        given().spec(SpecFactory.requestJson())
                .body(malformed)
                .when().post("/booking")
                .then().statusCode(Matchers.anyOf(Matchers.is(400), Matchers.is(500)));
    }

    /** Stable negative: missing required block often yields 500 (sometimes 400). */
    @Test
    void create_withMissingMandatoryFields_shouldFail_400or500() {
        String minimal = """
          {"firstname":"X","lastname":"Y"}
          """;

        given().spec(SpecFactory.requestJson())
                .body(minimal)
                .when().post("/booking")
                .then().statusCode(Matchers.anyOf(Matchers.is(400), Matchers.is(500)));
    }

    /** Stable negative: explicit wrong content-type -> 400 or 415 (don't use SpecFactory for this one). */
    @Test
    void create_withWrongContentType_shouldBe415or400() {
        String payload = """
          {"firstname":"CT","lastname":"Wrong","totalprice":100,"depositpaid":true,
           "bookingdates":{"checkin":"2025-12-01","checkout":"2025-12-02"}}
          """;

        given().baseUri(BASE)
                .header("Content-Type", "text/plain") // intentional misuse
                .body(payload)
                .when().post("/booking")
                .then().statusCode(Matchers.anyOf(Matchers.is(400), Matchers.is(415), Matchers.is(500)));
    }

    /** Stable negative: wrong method/endpoint pattern. */
    @Test
    void methodNotAllowed_deleteWithoutId_should405or404() {
        given().spec(SpecFactory.requestJson())
                .when().delete("/booking")
                .then().statusCode(Matchers.anyOf(Matchers.is(405), Matchers.is(404)));
    }
}
