// This class is part of the 'com.booking.tests.core' package.
// Packages in Java are used to group related classes together and avoid name conflicts.
// Typically, for a test automation framework, you organize code into logical packages like:
// core (base classes), tests (test cases), utils (helper classes), etc.
package com.booking.tests.core;

// Importing the RequestSpecification interface from Rest Assured.
// RequestSpecification represents the blueprint/definition of a HTTP request
// (like headers, cookies, body, authentication, etc.) before it is sent.
import io.restassured.specification.RequestSpecification;

// Static import of RestAssured.given() method.
// This allows us to call 'given()' directly without prefixing it with 'RestAssured.' every time.
// 'given()' is the starting point in Rest Assured to build a request.
import static io.restassured.RestAssured.given;

/**
 * ApiClient is an abstract base class for REST API clients.
 *
 * - "abstract" means you cannot create an object of this class directly.
 *   Instead, other concrete classes (e.g., BookingClient, AuthClient) will extend this class.
 *
 * - The purpose of this class is to provide reusable methods for building
 *   common types of requests (JSON requests, token-based auth, etc.).
 *
 * By centralizing this logic here, we avoid duplicating the same code in every test or client.
 */
public abstract class ApiClient {

    /**
     * Builds a base RequestSpecification for JSON-based requests.
     *
     * - This method is 'protected', so it is accessible within this class,
     *   its subclasses, and other classes in the same package.
     *
     * - It returns a RequestSpecification, which represents a "configured request"
     *   that can be further customized (e.g., adding query params, body, etc.)
     *   and then executed (e.g., when().get(...), when().post(...)).
     *
     * - 'given()' is the starting point in Rest Assured for building a request.
     *   Here, we are attaching a predefined specification using SpecFactory.requestJson().
     *   Typically, that spec will set:
     *      * Base URI / Base Path
     *      * Content-Type = application/json
     *      * Common headers, timeouts, etc.
     */
    protected RequestSpecification givenJson() {
        // 'given()' creates a fresh RequestSpecification.
        // '.spec(...)' applies a reusable specification (like a template for requests).
        // SpecFactory.requestJson() is likely a custom method you wrote that returns
        // a RequestSpecification configured for JSON requests.
        return given().spec(SpecFactory.requestJson());
    }

    /**
     * Builds a RequestSpecification that includes a 'token' cookie for authentication.
     *
     * - Many APIs use cookies (like 'token') for session or authentication.
     * - This method starts with the JSON base specification (givenJson())
     *   and then adds a cookie named "token".
     *
     * @param token the token value to be sent as a cookie.
     * @return a RequestSpecification that already includes the cookie.
     *
     * Usage example in a subclass or test:
     *     withToken("abc123").when().get("/booking");
     */
    protected RequestSpecification withToken(String token) {
        // Start with our base JSON request spec, then add a cookie named "token".
        // This returns a new RequestSpecification that we can immediately use or further customize.
        return givenJson().cookie("token", token);
    }

    /**
     * Builds a RequestSpecification that uses Bearer Token (JWT) Authorization.
     *
     * - Many modern APIs use JWT (JSON Web Tokens) in the Authorization header.
     *   The typical format is:
     *       Authorization: Bearer <jwt_token_here>
     *
     * - This method simplifies that pattern: you just pass the JWT, and we
     *   construct the correct header.
     *
     * @param jwt the JSON Web Token string.
     * @return a RequestSpecification with the Authorization header set.
     *
     * Usage example:
     *     withBearer(jwtToken).when().get("/secure-endpoint");
     */
    protected RequestSpecification withBearer(String jwt) {
        // Start with the JSON base spec, then add the Authorization header.
        // "Bearer " + jwt creates the correct value format for the header.
        return givenJson().header("Authorization", "Bearer " + jwt);
    }

    /**
     * Builds a RequestSpecification that uses HTTP Basic Authentication.
     *
     * - Basic Auth sends the username and password encoded in the request header:
     *       Authorization: Basic <base64(user:password)>
     *
     * - '.auth().preemptive().basic(user, pass)' tells Rest Assured to:
     *     * Use HTTP Basic authentication.
     *     * Send credentials with the **initial request** (preemptive),
     *       instead of waiting for a 401 challenge from the server.
     *
     * - This is useful for APIs or endpoints that protect resources with Basic Auth.
     *
     * @param user the username for basic auth.
     * @param pass the password for basic auth.
     * @return a RequestSpecification with preemptive basic authentication configured.
     *
     * Example usage:
     *     withBasic("admin", "password").when().get("/admin/health");
     */
    protected RequestSpecification withBasic(String user, String pass) {
        // Start with the JSON base spec, then configure preemptive basic authentication.
        // 'auth()' switches to the authentication configuration.
        // 'preemptive()' means send the credentials right away, without waiting for a 401.
        // 'basic(user, pass)' sets the username and password.
        return givenJson().auth().preemptive().basic(user, pass);
    }
}
