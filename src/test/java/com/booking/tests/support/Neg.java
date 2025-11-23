package com.booking.tests.support;

import com.booking.tests.core.SpecFactory;
import io.restassured.response.ValidatableResponse;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import static io.restassured.RestAssured.given;

public final class Neg {
    private Neg() {}

    private static Matcher<Integer> allowed(int... statuses) {
        return Matchers.anyOf(
                java.util.Arrays.stream(statuses).boxed().map(Matchers::is).toArray(Matcher[]::new)
        );
    }

    public static ValidatableResponse postExpecting(Object body, String path, int... allowedStatuses) {
        return given().spec(SpecFactory.requestJson())
                .body(body)
                .when().post(path)
                .then().statusCode(allowed(allowedStatuses));
    }

    public static ValidatableResponse putNoAuthExpecting(Object body, String path, int... allowedStatuses) {
        return given().spec(SpecFactory.requestJson())
                .body(body)
                .when().put(path)
                .then().statusCode(allowed(allowedStatuses));
    }

    public static ValidatableResponse postWithContentType(
            String baseUri, String contentType, String body, String path, int... allowedStatuses) {
        return given().baseUri(baseUri)
                .header("Content-Type", contentType)
                .body(body)
                .when().post(path)
                .then().statusCode(allowed(allowedStatuses));
    }
}

