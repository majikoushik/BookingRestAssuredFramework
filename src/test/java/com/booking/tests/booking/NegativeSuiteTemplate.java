package com.booking.tests.booking;

import com.booking.tests.core.SpecFactory;
import com.booking.tests.models.BookingClient;
import com.booking.tests.models.BookingModels.Booking;
import com.booking.tests.models.BookingModels.BookingDates;
import com.booking.tests.support.BaseTest;
import com.booking.tests.support.Neg;
import io.qameta.allure.Epic;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;

/**
 * NegativeSuiteTemplate
 * ---------------------
 * Use this file as a staging ground for all negative scenarios.
 * Each @Nested block groups a theme; add methods with clear names & short bodies.
 * Copy/paste a method, tweak the payload/headers, and you have a new negative test.
 */
@Epic("Negative")
@DisplayName("Negative test boilerplate (add scenarios here)")
public class NegativeSuiteTemplate extends BaseTest {

    private static final String BASE = "https://restful-booker.herokuapp.com";
    private final BookingClient client = new BookingClient();

    // ---------------------------------------------------------------------------
    // 0) Test Data Templates (builders for quick payload tweaks)
    // ---------------------------------------------------------------------------
    private static Booking aValidBooking() {
        var b = new Booking();
        b.firstname = "John";
        b.lastname  = "Doe";
        b.totalprice = 150;
        b.depositpaid = true;
        b.bookingdates = new BookingDates(LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 22));
        b.additionalneeds = "Breakfast";
        return b;
    }

    // ---------------------------------------------------------------------------
    // 1) Authentication / Authorization
    // ---------------------------------------------------------------------------
    @Nested @DisplayName("Auth / AuthZ")
    class Authz {

        @Test
        @DisplayName("PUT without token/basic => 401 or 403")
        void put_withoutAuth_shouldBe401or403() {
            var created = client.create(aValidBooking());
            Neg.putNoAuthExpecting(aValidBooking(), "/booking/" + created.bookingid, 401, 403);
        }

        @Test
        @DisplayName("PUT with invalid token => 401 or 403")
        void put_withInvalidToken_shouldBe401or403() {
            var created = client.create(aValidBooking());
            given().spec(SpecFactory.requestJson())
                    .cookie("token", "not-a-real-token")
                    .body(aValidBooking())
                    .when().put("/booking/{id}", created.bookingid)
                    .then().statusCode(Matchers.anyOf(Matchers.is(401), Matchers.is(403)));
        }

        @Test
        @DisplayName("Auth with bad creds returns reason, not token")
        void auth_withBadCreds_shouldReturnReason() {
            given().baseUri(BASE).contentType("application/json")
                    .body("""
                {"username":"admin","password":"wrong"}
                """)
                    .when().post("/auth")
                    .then().statusCode(200)
                    .body(Matchers.not(Matchers.containsString("\"token\"")))
                    .body(Matchers.containsString("reason"));
        }

        // TODO: add: token expiry simulation (if target API supports it)
    }

    // ---------------------------------------------------------------------------
    // 2) Request Validation / Payload Shape
    // ---------------------------------------------------------------------------
    @Nested @DisplayName("Payload validation")
    class PayloadValidation {

        @Test
        @DisplayName("Malformed JSON => 400 or 500")
        void post_malformedJson_shouldFail_400or500() {
            String malformed = "{\"firstname\":\"Typo\",\"lastname\":\"Price\""; // missing brace
            Neg.postExpecting(malformed, "/booking", 400, 500);
        }

        @Test
        @DisplayName("Missing required block => 400/500")
        void post_missingRequired_shouldFail_400or500() {
            String minimal = """
        {"firstname":"X","lastname":"Y"}
        """;
            Neg.postExpecting(minimal, "/booking", 400, 500);
        }

        @Test
        @DisplayName("Non-ISO dates are accepted (document behavior)")
        void post_nonIsoDates_isAccepted_butDocumented() {
            String badPayload = """
              {"firstname":"John","lastname":"Doe","totalprice":150,"depositpaid":true,
               "bookingdates":{"checkin":"20-12-2025","checkout":"22-12-2025"},
               "additionalneeds":"Breakfast"}
              """;

            // Expect 200: playground accepts/mangles non-ISO
            int id = given().spec(SpecFactory.requestJson())
                    .body(badPayload)
                    .when().post("/booking")
                    .then().statusCode(200)
                    .extract().path("bookingid");

            // Prove it isn't proper ISO on GET
            given().spec(SpecFactory.requestJson())
                    .when().get("/booking/{id}", id)
                    .then().statusCode(200)
                    .body("bookingdates.checkin", Matchers.not(Matchers.matchesRegex("^\\d{4}-\\d{2}-\\d{2}$")))
                    .body("bookingdates.checkout", Matchers.not(Matchers.matchesRegex("^\\d{4}-\\d{2}-\\d{2}$")));
        }

        // TODO: add: overlong strings, special characters, boundary dates, negative totalprice, etc.
    }

    // ---------------------------------------------------------------------------
    // 3) Content Negotiation / Headers / CT
    // ---------------------------------------------------------------------------
    @Nested @DisplayName("Headers & Content-Type")
    class HeadersAndContentType {

        @Test
        @DisplayName("Wrong content-type => 400/415/500 (infra variance)")
        void post_wrongContentType_shouldFail_400_415_or_500() {
            String payload = """
        {"firstname":"CT","lastname":"Wrong","totalprice":100,"depositpaid":true,
         "bookingdates":{"checkin":"2025-12-01","checkout":"2025-12-02"}}
        """;
            Neg.postWithContentType(BASE, "text/plain", payload, "/booking", 400, 415, 500);
        }

        // TODO: add: missing Accept header, unexpected charset, gzip mismatch, etc.
    }

    // ---------------------------------------------------------------------------
    // 4) Method semantics / Routing
    // ---------------------------------------------------------------------------
    @Nested @DisplayName("HTTP Method Semantics")
    class MethodSemantics {

        @Test
        @DisplayName("DELETE collection => 405/404")
        void delete_collection_shouldBe405or404() {
            given().spec(SpecFactory.requestJson())
                    .when().delete("/booking")
                    .then().statusCode(Matchers.anyOf(Matchers.is(405), Matchers.is(404)));
        }

        // TODO: add: PUT without body, PATCH non-existent, GET with invalid id format, etc.
    }

    // ---------------------------------------------------------------------------
    // 5) Parameterized placeholders (add cases without new methods)
    // ---------------------------------------------------------------------------
    @Nested @DisplayName("Parameterized negatives")
    class Parameterized {

        /** Supply bodies that should fail; add more strings in the stream. */
        static Stream<String> badBodies() {
            return Stream.of(
                    "{\"firstname\":\"X\"",                                 // malformed
                    "{\"firstname\":true,\"lastname\":false}",              // wrong types
                    "{\"firstname\":\"A\",\"lastname\":\"B\",\"totalprice\":\"NaN\"}" // wrong type
            );
        }

        @ParameterizedTest(name = "[{index}] POST /booking with bad body -> should fail")
        @MethodSource("badBodies")
        void post_withBadBodies_shouldFail(String body) {
            Neg.postExpecting(body, "/booking", 400, 415, 500);
        }

        /** Supply unacceptable content types */
        static Stream<String> badContentTypes() {
            return Stream.of("text/plain", "application/xml", "application/x-www-form-urlencoded");
        }

        @ParameterizedTest(name = "[{index}] POST wrong CT {0} -> should fail")
        @MethodSource("badContentTypes")
        void post_withBadContentTypes_shouldFail(String ct) {
            String payload = """
        {"firstname":"P","lastname":"CT","totalprice":100,"depositpaid":true,
         "bookingdates":{"checkin":"2025-12-01","checkout":"2025-12-02"}}
        """;
            Neg.postWithContentType(BASE, ct, payload, "/booking", 400, 415, 500);
        }
    }

    // ---------------------------------------------------------------------------
    // 6) Placeholders for security-ish probes (FYI-only for playground)
    // ---------------------------------------------------------------------------
    @Nested @DisplayName("Security-ish probes (educational)")
    class Securityish {
        // TODO: add: very large payload (DoS-ish), script tags in names (XSS echo),
        // path traversal in fields, Unicode confusables, SQL-ish strings. Expect 2xx on this API
        // but assert that response body does not reflect dangerous content unescaped.
    }
}
