package com.asrevo.cvhome.aot;

import java.util.Map;

import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Builds the OpenTelemetry SDK into a native image whatever the build's own configuration says; {@code
 * otel.sdk.disabled} then decides at run time, as it does on the JVM.
 *
 * <p>
 * The OpenTelemetry starter chooses between its real SDK and a no-op one with a condition on {@value #SDK_DISABLED},
 * which {@code common-config.yml} sets {@code true}: telemetry is each environment's choice ({@code
 * OTEL_SDK_DISABLED=false} on the load-testing stack, and on Fargate for flavours with monitoring). Processed ahead
 * of time, that condition is decided once, at build, with the default. The first native load test therefore ran with
 * no telemetry at all — "OpenTelemetry Spring Boot starter has been disabled" in every service, none of them in
 * Prometheus — although the stack set {@code OTEL_SDK_DISABLED=false}.
 * </p>
 *
 * <p>
 * During {@code processAot} only, this sets it {@code false}, so the real SDK's beans are the ones generated. At run
 * time the SDK reads {@value #SDK_DISABLED} itself and builds a no-op SDK when it is {@code true}, so an environment
 * that leaves telemetry off exports nothing, exactly as on the JVM. It is this codebase's rule — no bean exists or not
 * by configuration — applied to a third-party auto-configuration that {@code verifyNoConfigurationSwitchedBeans}
 * cannot see.
 * </p>
 */
public class OpenTelemetryAotEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    static final String SDK_DISABLED = "otel.sdk.disabled";

    static final String AOT_PROCESSING = "spring.aot.processing";

    static final String SOURCE_NAME = "cvhomeOpenTelemetryAot";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!Boolean.getBoolean(AOT_PROCESSING)) {
            return;
        }
        environment.getPropertySources().addFirst(new MapPropertySource(SOURCE_NAME, Map.of(SDK_DISABLED, "false")));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

}
