package com.asrevo.cvhome.aot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * A native image carries the real OpenTelemetry SDK, and {@code otel.sdk.disabled} keeps its run-time meaning. Before
 * this the first native load test ran with no telemetry: the build froze the default, "disabled".
 */
class OpenTelemetryAotEnvironmentPostProcessorTest {

    private static final String TRUE = "true";

    private static MockEnvironment telemetryOffByDefault() {
        return new MockEnvironment().withProperty(OpenTelemetryAotEnvironmentPostProcessor.SDK_DISABLED, TRUE);
    }

    @AfterEach
    void clearAotFlag() {
        System.clearProperty(OpenTelemetryAotEnvironmentPostProcessor.AOT_PROCESSING);
    }

    @Test
    void duringProcessAotTheSdkIsBuiltWhateverTheDefaultSays() {
        System.setProperty(OpenTelemetryAotEnvironmentPostProcessor.AOT_PROCESSING, TRUE);
        MockEnvironment environment = telemetryOffByDefault();

        new OpenTelemetryAotEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty(OpenTelemetryAotEnvironmentPostProcessor.SDK_DISABLED)).isEqualTo("false");
    }

    @Test
    void atRunTimeTheEnvironmentDecides() {
        MockEnvironment environment = telemetryOffByDefault();

        new OpenTelemetryAotEnvironmentPostProcessor().postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty(OpenTelemetryAotEnvironmentPostProcessor.SDK_DISABLED)).isEqualTo(TRUE);
    }

    @Test
    void itIsRegisteredForEveryApplication() {
        // Spring Boot's own post-processors take constructor arguments this test does not supply; skip those.
        assertThat(SpringFactoriesLoader.forDefaultResourceLocation()
                .load(EnvironmentPostProcessor.class, null,
                        SpringFactoriesLoader.FailureHandler.handleMessage((message, failure) -> { })))
                .anyMatch(OpenTelemetryAotEnvironmentPostProcessor.class::isInstance);
    }

}
