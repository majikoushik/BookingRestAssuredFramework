package com.booking.tests.core;


import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * Prints request+response only when the status code >= 400.
 * Why: Avoid noisy logs on success, get full details on failure.
 */
public class LogOnFailureFilter implements Filter {
    @Override
    public Response filter(FilterableRequestSpecification req,
                           FilterableResponseSpecification res,
                           FilterContext ctx) {
        Response response = ctx.next(req, res);
        if (response.statusCode() >= 400) {
            System.out.println("=== Request (on failure) ===");
            req.log().all();
            System.out.println("=== Response (on failure) ===");
            response.then().log().all();
        }
        return response;
    }
}

