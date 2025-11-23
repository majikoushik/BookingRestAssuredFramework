// This class is in the 'com.booking.tests.models' package.
// Typically, we keep API model classes and API clients here.
package com.booking.tests.models;

// Importing project-specific and library classes used in this client.
import com.booking.tests.config.Config;   // Custom config class to get username/password, base URLs, etc.
import com.booking.tests.core.ApiClient;  // Our base API client with common Rest Assured setup.
import io.qameta.allure.Step;            // Allure annotation for reporting test steps.
import io.restassured.http.Cookie;       // Rest Assured class to represent HTTP cookies.
import io.restassured.response.Response;

import static io.restassured.http.ContentType.JSON;  // Static import for JSON content type.

/**
 * BookingClient is a concrete API client that extends ApiClient.
 *
 * - It encapsulates all API operations related to the "Booking" resource
 *   (create, get, update, delete, auth token).
 *
 * - Each public method represents a specific API call and returns
 *   strongly-typed response models.
 *
 * - This class is used by tests to interact with the Booking API in a clean,
 *   reusable way instead of writing raw Rest Assured code in each test.
 */
public class BookingClient extends ApiClient {

    /**
     * Creates an authentication token by calling the /auth endpoint.
     *
     * The @Step annotation from Allure:
     * - Marks this method as a "step" in Allure reports.
     * - The text "Create auth token" will appear in the report, making it
     *   easier to read the execution flow.
     */
    @Step("Create auth token")
    public String createToken() {

        // Using a Java text block (""" ... """) to define a multiline String.
        // This helps keep JSON bodies readable and formatted nicely.
        //
        // The %s placeholders will be replaced by the username and password
        // from the Config class using .formatted().
        //
        // Example final body:
        // {
        //   "username":"admin",
        //   "password":"password123"
        // }
        var body = """
      {"username":"%s","password":"%s"}
      """.formatted(Config.user(), Config.pass());

        // givenJson(): comes from ApiClient; it returns a base RequestSpecification
        // configured for JSON requests (e.g., base URI, headers).
        //
        // .contentType(JSON): sets "Content-Type: application/json" header.
        // .body(body): sets the request payload.
        // .when().post("/auth"): sends an HTTP POST request to /auth.
        // .then().statusCode(200): asserts the response HTTP status is 200.
        // .extract().path("token"): extracts the "token" field from JSON response.
        return givenJson().contentType(JSON)
                .body(body)
                .when().post("/auth")
                .then().statusCode(200)
                .extract().path("token");
    }

    /**
     * New: low-level "raw" create that returns the full Response.
     * - Does NOT assert the status code.
     * - Allows us to test both positive (200) and negative (400, 500…) flows.
     */
    @Step("Create booking (raw) for {req.firstname} {req.lastname}")
    public Response createRaw(BookingModels.Booking req) {
        return givenJson().contentType(JSON)
                .body(req)
                .when().post("/booking");
    }

    /**
     * Creates a new booking by calling the /booking endpoint.
     *
     * @param req a BookingModels.Booking object that represents the request body.
     *            Rest Assured will automatically serialize this Java object to JSON.
     * @return a CreateBookingResponse object which contains booking id and booking details.
     *
     * The @Step annotation:
     * - The placeholder {firstname} {lastname} will be resolved from the 'req' object
     *   using its getters (e.g., getFirstname(), getLastname()) for Allure reporting.
     */
    @Step("Create booking for {firstname} {lastname}")
    public BookingModels.CreateBookingResponse create(BookingModels.Booking req) {

        // givenJson(): base specification.
        // .contentType(JSON): sending JSON.
        // .body(req): Java POJO -> JSON (via Jackson or Gson under the hood).
        // .when().post("/booking"): POST request to /booking.
        // .then().statusCode(200): Asserts that the API returned 200
        //   (this specific API returns 200 even for a create operation).
        // .extract().as(...): deserializes JSON response to CreateBookingResponse class.
        return givenJson().contentType(JSON)
                .body(req)
                .when().post("/booking")
                .then().statusCode(200) // API returns 200 for create
                .extract().as(BookingModels.CreateBookingResponse.class);
    }

    /**
     * Fetches an existing booking by its id from /booking/{id}.
     *
     * @param id the booking id to retrieve.
     * @return a BookingModels.Booking object representing the response body.
     */
    @Step("Get booking {id}")
    public BookingModels.Booking get(int id) {

        // givenJson(): base request spec.
        // .when().get("/booking/{id}", id):
        //   - Uses a path parameter placeholder {id} in the URL.
        //   - 'id' is passed as an argument and substituted automatically.
        // .then().statusCode(200): expects success.
        // .extract().as(...): maps JSON response into Booking class (POJO).
        return givenJson()
                .when().get("/booking/{id}", id)
                .then().statusCode(200)
                .extract().as(BookingModels.Booking.class);
    }

    /**
     * Updates an existing booking using PUT /booking/{id}.
     *
     * - This endpoint requires a 'token' cookie for authentication.
     *
     * @param id    booking id to update.
     * @param req   the updated booking data.
     * @param token the auth token to be sent as a cookie.
     * @return the updated Booking object.
     */
    @Step("Update booking {id}")
    public BookingModels.Booking update(int id, BookingModels.Booking req, String token) {

        // Create a Cookie object with name "token" and value from parameter.
        // Using Cookie.Builder gives more control (e.g., domain, path, expiry) if needed later.
        Cookie cookie = new Cookie.Builder("token", token).build();

        // givenJson(): base specification.
        // .cookie(cookie): adds the "token" cookie to the request.
        // .contentType(JSON): sending JSON body.
        // .body(req): updated booking data.
        // .when().put("/booking/{id}", id): PUT request to /booking/{id}.
        // .then().statusCode(200): expects OK.
        // .extract().as(...): maps the updated booking from response JSON to Booking class.
        return givenJson().cookie(cookie)
                .contentType(JSON)
                .body(req)
                .when().put("/booking/{id}", id)
                .then().statusCode(200)
                .extract().as(BookingModels.Booking.class);
    }

    /**
     * Deletes a booking using DELETE /booking/{id}.
     *
     * - Requires a 'token' cookie, similar to update.
     * - This method returns void because we only care that the call succeeds
     *   (status code) and not about any response body.
     *
     * @param id    booking id to delete.
     * @param token auth token to be sent as a cookie.
     */
    @Step("Delete booking {id}")
    public void delete(int id, String token) {

        // Build the "token" cookie just like in the update method.
        Cookie cookie = new Cookie.Builder("token", token).build();

        // givenJson(): base spec.
        // .cookie(cookie): attach auth cookie.
        // .when().delete("/booking/{id}", id): DELETE request.
        // .then().statusCode(201): asserts that the API returns 201 (per this API's behavior).
        // No .extract() here because we don't need any response body for delete.
        givenJson().cookie(cookie)
                .when().delete("/booking/{id}", id)
                .then().statusCode(201); // per API behavior
    }
}
