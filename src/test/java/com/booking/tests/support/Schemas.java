package com.booking.tests.support;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public final class Schemas {
    private Schemas(){}
    public static final String AUTH  = "schemas/auth.schema.json";
    public static final String BOOK  = "schemas/booking.schema.json";
    public static final String CREATE_RESP = "schemas/create-booking-response.schema.json";

    public static org.hamcrest.Matcher<?> auth(){ return matchesJsonSchemaInClasspath(AUTH); }
    public static org.hamcrest.Matcher<?> booking(){ return matchesJsonSchemaInClasspath(BOOK); }
    public static org.hamcrest.Matcher<?> createResp(){ return matchesJsonSchemaInClasspath(CREATE_RESP); }
}
