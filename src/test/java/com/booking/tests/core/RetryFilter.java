package com.booking.tests.core;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.time.Duration;

/**
 * Retries transient errors (502/503/504) with exponential backoff.
 * Keeps your suite stable in the face of brief upstream hiccups.
 */
public class RetryFilter implements Filter {
    private final int maxRetries;
    private final Duration initialDelay;

    public RetryFilter(int maxRetries, Duration initialDelay) {
        this.maxRetries = Math.max(0, maxRetries);
        this.initialDelay = initialDelay;
    }

    @Override
    public Response filter(FilterableRequestSpecification req,
                           FilterableResponseSpecification res,
                           FilterContext ctx) {

        Response response = ctx.next(req, res);
        int attempt = 0;
        long delay = initialDelay.toMillis();

        while (attempt < maxRetries && isTransient(response)) {
            sleep(delay);
            delay *= 2; // exponential backoff
            attempt++;
            response = ctx.next(req, res);
        }
        return response;
    }

    private boolean isTransient(Response r) {
        int code = r.statusCode();
        return code == 502 || code == 503 || code == 504;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
