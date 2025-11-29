# BookingRestAssuredFramework

An end-to-end REST Assured training project that exercises the public [Restful Booker](https://restful-booker.herokuapp.com/) API. It is intentionally small but shows how to structure a real test framework with reusable HTTP specs, typed models, negative testing, contract checks, and Cucumber BDD.

> New to API test automation? Read the sections in order. File paths are clickable so you can jump straight into the code.

## Prerequisites
- JDK 23 (set `JAVA_HOME`)
- Maven 3.9+
- Optional: [Allure CLI](https://docs.qameta.io/allure/) for reports (or run `run-allure-report.bat` after tests)

## Quick start
- Install dependencies & run everything (JUnit + Cucumber): `mvn test`
- Run only Cucumber features: `mvn -Dtest=CucumberTestRunner test`
- Run only JUnit tests: `mvn -Dtest=com.booking.tests.booking.* test`
- Filter by JUnit tags (e.g., the e2e flow): `mvn test -DincludeTags=e2e`
- Generate Allure report after a test run: `allure serve allure-results` (or `run-allure-report.bat` on Windows)

## What happens under the hood (HTTP plumbing)
1) **Configuration** – `src/test/java/com/booking/tests/config/Config.java` loads `src/test/resources/application.conf` (base URL, timeouts, auth credentials). You can add `application-qa.conf` etc. and override via `-Dconfig.resource`.
2) **Reusable specs & filters** – `src/test/java/com/booking/tests/core/SpecFactory.java` builds the shared Rest Assured request spec (base URI, JSON defaults, timeouts, logging policy) and response specs. Filters such as:
   - `CorrelationIdFilter` adds `X-Correlation-Id` to every call.
   - `RedactingLogOnFailureFilter` logs request/response only on failure and masks sensitive fields.
   - `RetryFilter` retries 502/503/504 with backoff.
3) **Client base class** – `src/test/java/com/booking/tests/core/ApiClient.java` exposes helpers `givenJson()`, `withToken()`, `withBearer()`, `withBasic()` so tests never reconfigure plumbing.
4) **Typed models & client** – `src/test/java/com/booking/tests/models/BookingModels.java` holds POJOs for requests/responses. `src/test/java/com/booking/tests/models/BookingClient.java` is the single point that wraps `/auth`, `/booking` (create/get/update/delete) using the helpers above.

## Project layout (read me like a map)
- **Config**: `src/test/resources/application.conf`, `src/test/resources/logback-test.xml`
- **Core HTTP**: `core/SpecFactory.java`, `core/ApiClient.java`, `core/CorrelationIdFilter.java`, `core/RedactingLogOnFailureFilter.java`, `core/RetryFilter.java`, `core/SecretMaskingFilter.java`
- **Models & builders**: `models/BookingModels.java`, `models/BookingClient.java`, `builders/BookingBuilder.java`
- **Test utilities**: `support/BaseTest.java` (suite logging), `support/Schemas.java` (JSON schema helpers), `support/Neg.java` (negative helpers)
- **JUnit tests**: `booking/BookingLifecycleTest.java`, `booking/NegativeBookingTest.java`, `booking/NegativeSuiteTemplate.java`, `booking/SchemaContractTest.java`
- **BDD**: runner `bdd/CucumberTestRunner.java`; steps in `bdd/steps/*`; features in `src/test/resources/features/*.feature`; sample JSON template `src/test/resources/payloads/booking_payload.json`
- **Schemas**: `src/test/resources/schemas/*.json` validate auth, booking, and create-booking responses

## Main test flows
- **Happy-path lifecycle** – `BookingLifecycleTest.java`
  - Setup: create token via `BookingClient.createToken()`
  - Create booking, fetch it, update last name, delete booking, and confirm it is gone.
- **Negative coverage** – `NegativeBookingTest.java` + `NegativeSuiteTemplate.java`
  - Auth with wrong creds, update without/with bad token, malformed JSON, wrong content type, method not allowed, and API’s odd acceptance of non-ISO dates.
  - `support/Neg.java` centralizes “expect this status code” helpers.
- **Contract tests** – `SchemaContractTest.java` validates `/auth` and `/booking` responses against JSON schemas via `support/Schemas.java`.
- **Cucumber BDD** (executed by `CucumberTestRunner.java`)
  - `booking_lifecycle.feature` ↔ `steps/BookingLifecycleSteps.java`: token → create → get → update → delete.
  - `booking_negative.feature` ↔ `steps/BookingNegativeSteps.java`: invalid endpoint and malformed JSON.
  - `post_booking.feature` ↔ `steps/PostBookingSteps.java`: scenario outline that fills `payloads/booking_payload.json` placeholders from the Examples table and asserts status/firstname.
  - `post_booking_datatable.feature` ↔ `steps/PostBookingDataTableSteps.java`: builds the booking object directly from a DataTable (no template) and asserts every returned field.

## How to add or extend tests
1) **New endpoint**: add a method to `BookingClient.java` (or a new client class) using `givenJson()` and, if needed, `withToken()` for cookie auth. Keep assertions out of clients.
2) **New positive/negative JUnit test**: create a class under `src/test/java/com/booking/tests/booking/`, extend `BaseTest`, and use the client + `SpecFactory.okJson()`/`Neg` helpers.
3) **New schema check**: drop a schema file under `src/test/resources/schemas/` and expose it via `support/Schemas.java`, then assert with `body(Schemas.yourSchema())`.
4) **New BDD scenario**: add steps in `bdd/steps/` and a matching `.feature` file under `src/test/resources/features/`. Glue is auto-wired via `junit-platform.properties`.

## Configuration tips
- Change base URL, timeouts, or creds in `application.conf`. You can override at runtime: `mvn test -Dconfig.resource=application-qa.conf`.
- Filters are enabled in `SpecFactory`; toggle logging or masking there instead of per test to keep behavior consistent.
- Faker (`net.datafaker.Faker`) is used to generate realistic names/prices in happy-path tests to reduce collisions.

Happy testing! If you are learning, open the referenced files while you read this README to see how each concept is applied in code.
