package com.booking.tests.core;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.function.Supplier;

/**
 * Adds an X-Correlation-Id header so you can trace a request end-to-end.
 * In a real org, align this name with platform conventions (e.g., x-request-id).
 */
public class CorrelationIdFilter implements Filter {

    private final Supplier<String> idSupplier;
    private final String headerName;

    public CorrelationIdFilter(Supplier<String> idSupplier) {
        this(idSupplier, "X-Correlation-Id");
    }

    public CorrelationIdFilter(Supplier<String> idSupplier, String headerName) {
        this.idSupplier = idSupplier;
        this.headerName = headerName;
    }

    @Override
    public Response filter(FilterableRequestSpecification req,
                           FilterableResponseSpecification res,
                           FilterContext ctx) {
        if (!req.getHeaders().hasHeaderWithName(headerName)) {
            req.header(headerName, idSupplier.get());
        }
        return ctx.next(req, res);
    }
}

