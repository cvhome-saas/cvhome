package com.asrevo.cvhome.management;

import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.autoconfigure.endpoint.EndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.env.EnvironmentEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.info.InfoEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementContextAutoConfiguration;
import org.springframework.boot.actuate.endpoint.Show;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointAutoConfiguration;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointProperties;
import org.springframework.boot.health.autoconfigure.contributor.HealthContributorAutoConfiguration;
import org.springframework.boot.health.autoconfigure.registry.HealthContributorRegistryAutoConfiguration;
import org.springframework.boot.http.converter.autoconfigure.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletAutoConfiguration;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * What {@code common-config.yml} maps under {@code /actuator}, asserted over HTTP against the shipped file.
 *
 * <p>
 * The default used to be {@code *}. Every service but uaa serves its actuator anonymously and the gateway forwards
 * {@code /tenancy/actuator/...}, so that default put a heap dump, {@code /env} and {@code /configprops} one GET
 * away from the public edge (authorization audit, A1). The fix is one line in the shared file, and this test is
 * what keeps it there: health, info and prometheus, nothing else, and the health body without its components for
 * an anonymous caller. No security filter is wired here on purpose — the point is that the endpoints do not exist,
 * not that a filter hides them.
 * </p>
 */
class ActuatorExposureTest {

    private static final String HEALTH = "/actuator/health";
    private static final String ENV = "/actuator/env";

    /**
     * The actuator's web stack as a service gets it, with the shared file imported the way every application.yml
     * does. {@code ConfigDataApplicationContextInitializer} runs Boot's config-data loading, so the placeholder
     * defaults resolve exactly as they do at runtime. The file derives {@code server.port} from the application
     * name, so the context is a catalog service as far as the properties are concerned.
     */
    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withInitializer(new ConfigDataApplicationContextInitializer())
            .withSystemProperties("spring.config.import=classpath:common-config.yml",
                    "spring.application.name=catalog")
            .withConfiguration(AutoConfigurations.of(
                    DispatcherServletAutoConfiguration.class, WebMvcAutoConfiguration.class,
                    JacksonAutoConfiguration.class, HttpMessageConvertersAutoConfiguration.class,
                    EndpointAutoConfiguration.class, WebEndpointAutoConfiguration.class,
                    ManagementContextAutoConfiguration.class,
                    HealthContributorAutoConfiguration.class, HealthContributorRegistryAutoConfiguration.class,
                    HealthEndpointAutoConfiguration.class,
                    InfoEndpointAutoConfiguration.class, EnvironmentEndpointAutoConfiguration.class));

    @Test
    void theDefaultExposureIsHealthInfoAndPrometheus() {
        runner.run(context -> {
            assertThat(context.getBean(WebEndpointProperties.class).getExposure().getInclude())
                    .containsExactly("health", "info", "prometheus");
            assertThat(context.getBean(HealthEndpointProperties.class).getShowDetails())
                    .isEqualTo(Show.WHEN_AUTHORIZED);
        });
    }

    @Test
    void healthAndInfoAnswerAndEnvIsNotMapped() {
        runner.run(context -> {
            MockMvcTester mvc = MockMvcTester.from(context);

            assertThat(mvc.get().uri(HEALTH)).hasStatusOk()
                    .bodyJson().extractingPath("$.status").isEqualTo("UP");
            assertThat(mvc.get().uri("/actuator/info")).hasStatusOk();
            assertThat(mvc.get().uri(ENV)).hasStatus(404);
            assertThat(mvc.get().uri("/actuator/heapdump")).hasStatus(404);
            assertThat(mvc.get().uri("/actuator/configprops")).hasStatus(404);
        });
    }

    /** {@code when-authorized}: an anonymous probe reads the status and nothing about what makes it up. */
    @Test
    void anAnonymousHealthProbeSeesTheStatusAndNoComponents() {
        runner.run(context -> {
            MockMvcTester mvc = MockMvcTester.from(context);

            assertThat(mvc.get().uri(HEALTH)).hasStatusOk()
                    .bodyJson().doesNotHavePath("$.components");
        });
    }

    /** The env var is the debugging lever: set, it widens the exposure for that one process. */
    @Test
    void theEnvironmentVariableWidensTheExposure() {
        runner.withSystemProperties("MANAGEMENT_ENDPOINTS_EXPOSURE=*").run(context -> {
            MockMvcTester mvc = MockMvcTester.from(context);

            assertThat(mvc.get().uri(ENV)).hasStatusOk();
        });
    }

}
