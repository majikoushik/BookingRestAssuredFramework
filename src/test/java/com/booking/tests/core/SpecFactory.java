package com.booking.tests.core;

import com.booking.tests.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.path.json.config.JsonPathConfig;
/*
One place to define how HTTP is done across the suite: base URL, JSON defaults, timeouts/SLAs, logging, retries, tracing, and common headers.
Ensures consistency and DRY: change here → every request/test benefits.
Makes endpoint clients (e.g., BookingClient) tiny and readable: they focus on what an API does, not plumbing.


 */
import io.restassured.config.HttpClientConfig;
import io.restassured.config.JsonConfig;
import io.restassured.config.LogConfig;
import io.restassured.config.MatcherConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.Filter;
import io.restassured.http.ContentType;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.lessThan;

/**
 * SpecFactory is the "control tower" for HTTP behavior:
 * - What base URL and headers we use by default
 * - Which timeouts and SLAs we enforce
 * - How we log (ideally: only on failures in CI)
 * - Cross-cutting filters: retry, masking secrets, correlation IDs, reporting hooks
 *
 * Tests and endpoint clients should NOT re-declare these details; they should always
 * start from this factory to avoid config drift.
 */
public final class SpecFactory {

    private SpecFactory() {} // Utility class; no instances.

    /**
     * Build a reusable JSON RequestSpecification.
     *
     * Key ideas for beginners:
     * 1) A "request spec" is a template describing how we talk to APIs.
     *    We set the base URL, content type, default headers, logging, timeouts, etc.
     * 2) Every API call in the framework should start from this spec so behavior is consistent.
     * 3) We add Filters (middleware) to implement cross-cutting concerns (retry, log-on-failure).
     */
    public static RequestSpecification requestJson() {

        // ---- 1) Build the list of filters we want globally ----
        List<Filter> filters = new ArrayList<>();

        // (a) Add a correlation-id on every request for traceability in logs/APM.
        filters.add(new CorrelationIdFilter(() -> UUID.randomUUID().toString()));

        // (b) Log request/response ONLY if a failure happens (status >= 400).
        //     This keeps CI logs clean but preserves details when you need them.
        //filters.add(new LogOnFailureFilter());

        // (c) Mask sensitive fields if they appear in logs (e.g., password, token).
        //filters.add(SecretMaskingFilter.defaultSecrets());
        filters.add(new RedactingLogOnFailureFilter());

        // (d) Retry transient failures (optional but common in real systems).
        //     Here: retry up to 2 times for 502/503/504 with exponential backoff.
        filters.add(new RetryFilter(2, Duration.ofMillis(250)));

        // (e) (Optional) Integrate reporting, e.g., Allure:
        // filters.add(new io.qameta.allure.restassured.AllureRestAssured());

        // ---- 2) Configure underlying HTTP client + JSON mapping behavior ----
        RestAssuredConfig config = RestAssuredConfig.newConfig()
                // Logging config: do not pretty-print huge payloads unless needed
                .logConfig(LogConfig.logConfig().enablePrettyPrinting(false))

                // JSON config: return numbers as BigDecimal (less rounding surprises),
                // and set default object mapper to Jackson.
                .jsonConfig(JsonConfig.jsonConfig()
                        .numberReturnType(JsonPathConfig.NumberReturnType.BIG_DECIMAL))
                .objectMapperConfig(new ObjectMapperConfig(ObjectMapperType.JACKSON_2)
                        .jackson2ObjectMapperFactory((cls, charset) -> {
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.registerModule(new JavaTimeModule());
                            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                            return mapper;
                        }))

                // Hamcrest/Matcher config: readable assertion errors
                .matcherConfig(MatcherConfig.matcherConfig())

                // HTTP client config: timeouts at socket & connect levels (hard stops)
                .httpClient(HttpClientConfig.httpClientConfig()
                        .setParam("http.socket.timeout", Config.timeoutMs())     // read timeout
                        .setParam("http.connection.timeout", Config.timeoutMs()) // connect timeout
                );

        // ---- 3) Build and return the RequestSpecification ----
        return new RequestSpecBuilder()
                .setBaseUri(Config.baseUrl())                 // base URL from one place (config)
                .setContentType(ContentType.JSON)             // default to JSON requests
                .addHeader("Accept", ContentType.JSON.toString())
                .addHeader("Accept-Charset", StandardCharsets.UTF_8.name())
                // Tip: user-agent helps backend observability and whitelisting in some orgs
                .addHeader("User-Agent", "RA-Tests/1.0 (+https://example.org)")

                // Unified logging policy: Method/URI always useful for quick traces
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)

                // Make all filters effective for every request
                .addFilters(filters)

                // Apply unified RA configuration
                .setConfig(config)

                .build();
    }

    /**
     * A reusable "response spec" that encodes our expectations for
     * content type and performance SLA (response time).
     *
     * Using this everywhere encourages teams to discuss/track API SLAs explicitly.
     */
    public static ResponseSpecification okJson() {
        return new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .expectResponseTime(lessThan((long) Config.timeoutMs()))
                .build();
    }

    /**
     * Variant: expect no JSON body (e.g., 204 No Content endpoints).
     * Demonstrates how you can have multiple standardized response specs.
     */
    public static ResponseSpecification noContent() {
        return new ResponseSpecBuilder()
                .expectStatusCode(204)
                .build();
    }
}
