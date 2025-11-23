package com.booking.tests.core;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

/**
 * Logs request/response ONLY on failure (status >= 400), with basic redaction.
 * IMPORTANT: Never mutate the request; only print a redacted view.
 */
public class RedactingLogOnFailureFilter implements Filter {

    private final Set<String> sensitiveHeaderNames = Set.of(
            "authorization", "proxy-authorization", "x-api-key", "api-key", "apikey"
    );
    private final Set<String> sensitiveCookieNames = Set.of(
            "token", "id-token", "access-token", "refresh-token"
    );

    @Override
    public Response filter(FilterableRequestSpecification req,
                           FilterableResponseSpecification res,
                           FilterContext ctx) {

        // Send the REAL request first
        Response response = ctx.next(req, res);

        if (response.statusCode() >= 400) {
            try {
                System.out.println("=== Request (redacted on failure) ===");
                System.out.printf("%s %s%n", req.getMethod(), req.getURI());

                // Headers (redacted)
                req.getHeaders().asList().forEach(h -> {
                    String name = h.getName();
                    String value = h.getValue();
                    if (isSensitiveHeader(name)) value = "****";
                    System.out.printf("Header: %s: %s%n", name, value);
                });

                // Cookies (redacted)
                if (req.getCookies() != null) {
                    req.getCookies().asList().forEach(c -> {
                        String value = isSensitiveCookie(c.getName()) ? "****" : c.getValue();
                        System.out.printf("Cookie: %s=%s%n", c.getName(), value);
                    });
                }

                // Query params (not redacted here; add if your API puts secrets there)
                if (req.getQueryParams() != null && !req.getQueryParams().isEmpty()) {
                    for (Map.Entry<String, ?> e : req.getQueryParams().entrySet()) {
                        System.out.printf("Query: %s=%s%n", e.getKey(), e.getValue());
                    }
                }

                // Body — safely convert whatever RA stored (char[] / byte[] / String / other)
                Object bodyObj = req.getBody();
                String bodyStr = null;
                if (bodyObj instanceof char[]) {
                    bodyStr = new String((char[]) bodyObj);
                } else if (bodyObj instanceof byte[]) {
                    bodyStr = new String((byte[]) bodyObj, StandardCharsets.UTF_8);
                } else if (bodyObj != null) {
                    bodyStr = bodyObj.toString();
                }
                if (bodyStr != null && !bodyStr.isBlank()) {
                    // naive JSON redaction for common keys (demo-level)
                    bodyStr = bodyStr.replaceAll("(?i)\"password\"\\s*:\\s*\".*?\"", "\"password\":\"****\"");
                    bodyStr = bodyStr.replaceAll("(?i)\"token\"\\s*:\\s*\".*?\"", "\"token\":\"****\"");
                    System.out.println("Body: " + bodyStr);
                }

                System.out.println("=== Response (on failure) ===");
                System.out.printf("Status: %d%n", response.statusCode());
                if (response.getHeaders() != null) {
                    response.getHeaders().asList().forEach(h ->
                            System.out.printf("Header: %s: %s%n", h.getName(), h.getValue()));
                }
                // Response body is safe to print as string
                System.out.println("Body: " + response.asString());

            } catch (Throwable t) {
                // Logging must never break the test flow
                System.out.println("[RedactingLogOnFailureFilter] Logging failed: " + t);
            }
        }

        return response;
    }

    private boolean isSensitiveHeader(String name) {
        return name != null && sensitiveHeaderNames.contains(name.toLowerCase());
    }

    private boolean isSensitiveCookie(String name) {
        return name != null && sensitiveCookieNames.contains(name.toLowerCase());
    }
}
