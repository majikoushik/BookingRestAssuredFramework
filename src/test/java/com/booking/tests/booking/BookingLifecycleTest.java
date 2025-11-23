package com.booking.tests.booking;
import com.booking.tests.models.BookingClient;
import com.booking.tests.models.BookingModels.*;
import net.datafaker.Faker;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDate;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("e2e")
public class BookingLifecycleTest extends com.booking.tests.support.BaseTest {
    static BookingClient client;
    static Faker faker;
    static int bookingId;
    static String token;

    @BeforeAll
    static void init() {
        client = new BookingClient();
        faker = new Faker();
        token = client.createToken();
        assertThat(token).isNotBlank();
    }

    @Test @Order(1)
    void createBooking() {
        var b = new Booking();
        b.firstname = faker.name().firstName();
        b.lastname  = faker.name().lastName();
        b.totalprice = faker.number().numberBetween(80, 400);
        b.depositpaid = true;
        b.bookingdates = new BookingDates(LocalDate.of(2025, 12, 20), LocalDate.of(2025, 12, 22));
        b.additionalneeds = "Breakfast";

        var resp = client.create(b);
        bookingId = resp.bookingid;

        assertThat(bookingId).isPositive();
        assertThat(resp.booking.firstname).isEqualTo(b.firstname);
    }

    @Test @Order(2)
    void getBooking() {
        var got = client.get(bookingId);
        assertThat(got).isNotNull();
        assertThat(got.bookingdates.checkin).isEqualTo(LocalDate.of(2025, 12, 20));
    }

    @Test @Order(3)
    void updateBooking_lastnameChanges() {
        var update = client.get(bookingId);
        update.lastname = "Updated";
        var updated = client.update(bookingId, update, token);
        assertThat(updated.lastname).isEqualTo("Updated");
    }

    @Test @Order(4)
    void deleteBooking_andVerifyGone() {
        client.delete(bookingId, token);
        // A subsequent GET commonly returns 404 or 418/Not Found depending on reset timing;
        // For simplicity, we just try and assert non-200:
        io.restassured.RestAssured.given()
                .when().get("https://restful-booker.herokuapp.com/booking/{id}", bookingId)
                .then().statusCode(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is(404), org.hamcrest.Matchers.is(418), org.hamcrest.Matchers.is(405)
                ));
    }
}
