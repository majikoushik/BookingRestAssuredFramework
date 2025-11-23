package com.booking.tests.support;

import org.junit.jupiter.api.*;
import java.time.Instant;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    @BeforeAll
    void beforeAll() {
        System.out.println("=== Test suite start @ " + Instant.now() + " ===");
    }

    @BeforeEach
    void beforeEach(TestInfo info) {
        System.out.println("→ " + info.getDisplayName() + " [tags: " + info.getTags() + "]");
    }

    @AfterEach
    void afterEach(TestInfo info) {
        System.out.println("✓ Finished: " + info.getDisplayName());
    }
}
