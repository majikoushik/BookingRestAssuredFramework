package com.booking.tests.core;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Cookie;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.Arrays;
import java.util.List;

/**
 * SecretMaskingFilter
 * -------------------
 * Purpose:
 *   Prevents sensitive information (tokens, passwords) from leaking into logs.
 *   We scrub request headers (and optionally cookies/query params) BEFORE any
 *   logging filter prints them.
 *
 * How it works:
 *   - If a sensitive header is present, we overwrite its value with "****".
 *   - (Optional) We can also redact cookie values whose names match the list.
 *
 * Notes:
 *   - Use this filter BEFORE your log-on-failure filter in the SpecFactory order.
 *   - This is a lightweight demo. In production, you might add JSON-body masking
 *     for specific fields (e.g., "password") by parsing and rewriting the body.
 */
public class SecretMaskingFilter implements Filter {

    // Header/cookie names to mask (case-insensitive in practice).
    private final List<String> sensitiveKeys;

    public SecretMaskingFilter(List<String> sensitiveKeys) {
        this.sensitiveKeys = sensitiveKeys;
    }

    /** Sensible defaults for many APIs. Extend as needed. */
    public static SecretMaskingFilter defaultSecrets() {
        return new SecretMaskingFilter(Arrays.asList(
                "authorization", "proxy-authorization", "x-api-key",
                "api-key", "apikey", "token", "id-token", "access-token",
                "refresh-token", "password", "set-cookie" // set-cookie isn't sent by client but kept for completeness
        ));
    }

    @Override
    public Response filter(FilterableRequestSpecification req,
                           FilterableResponseSpecification res,
                           FilterContext ctx) {

        // --- 1) Mask sensitive HEADERS ---
        // RA headers collection is case-insensitive; we check each desired key and, if present,
        // we overwrite with a fixed redacted value.
        for (String key : sensitiveKeys) {
            if (req.getHeaders().hasHeaderWithName(key)) {
                // removeHeader is optional; header(name, value) will overwrite if present.
                req.header(key, "****");  // <-- use header(...), NOT addHeader(...)
            }
        }

        // --- 2) (Optional) Mask sensitive COOKIES ---
        // If your tokens are sent as cookies (e.g., "token", "id-token"), redact those too.
        if (req.getCookies() != null && !req.getCookies().asList().isEmpty()) {
            for (Cookie c : req.getCookies().asList()) {
                if (sensitiveKeys.stream().anyMatch(k -> k.equalsIgnoreCase(c.getName()))) {
                    // RA doesn't provide a direct "replaceCookie", so remove + re-add a sanitized one:
                    req.removeCookie(c.getName());
                    req.cookie(c.getName(), "****");
                }
            }
        }

        // --- 3) (Optional) Mask sensitive QUERY PARAMS ---
        // Uncomment if your APIs put secrets in query params (not recommended).
        /*
        if (req.getQueryParams() != null && !req.getQueryParams().isEmpty()) {
            for (String name : new ArrayList<>(req.getQueryParams().keySet())) {
                if (sensitiveKeys.stream().anyMatch(k -> k.equalsIgnoreCase(name))) {
                    req.removeQueryParam(name);
                    req.queryParam(name, "****");
                }
            }
        }
        */

        // Continue down the filter chain.
        return ctx.next(req, res);
    }
}
