package com.booking.tests.assertions;


import io.restassured.response.Response;
import static org.assertj.core.api.Assertions.assertThat;

public final class ApiAssert {
    private ApiAssert(){}

    public static void assert2xx(Response r){
        assertThat(r.statusCode()).isBetween(200, 299);
    }

    public static void assertNotFoundOrGone(int status){
        assertThat(status).isIn(404, 410);
    }
}
