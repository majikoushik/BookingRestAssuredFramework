package com.booking.tests.bdd;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

@Suite
@SelectClasspathResource("features")   // folder under src/test/resources
/*
This tells Cucumber:
“Look for step definitions inside the package com.booking.tests.bdd.steps.”
So all Java classes inside that package become candidates for matching steps.
 */
@ConfigurationParameter(
        key = GLUE_PROPERTY_NAME,
        value = "com.booking.tests.bdd.steps"   // your steps package
)
@ConfigurationParameter(
        key = PLUGIN_PROPERTY_NAME,
        // pretty console + Allure integration
        value = "pretty, io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
)
// Optional: run only tagged scenarios, e.g. @smoke
// @ConfigurationParameter(
//         key = FILTER_TAGS_PROPERTY_NAME,
//         value = "@smoke"
// )
public class CucumberTestRunner {
    // No code needed. The annotations + JUnit Platform run the Cucumber engine.
}
