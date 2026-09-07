package com.asrevo.cvhome.s2s.config.internal;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import com.asrevo.cvhome.errors.web.ErrorHandlingAutoConfiguration;

import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The filter is registered on a servlet service that carries the shared error handling — the way every service
 * does. Pinned because it once was not: a {@code @ConditionalOnBean(ProblemDetailFactory.class)} on the
 * configuration was evaluated before the imported error configuration had defined the bean, so the filter was
 * silently absent and a read-only session could save. Nothing else in the suite would have noticed.
 */
class ReadOnlyActorConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withUserConfiguration(ReadOnlyActorConfiguration.class, ErrorHandlingAutoConfiguration.class);

    @Test
    void registersTheFilterBehindTheSecurityChain() {
        runner.withPropertyValues("com.asrevo.cvhome.s2s.read-only.allowed-writes=/**/list").run(context -> {
            assertThat(context).hasSingleBean(FilterRegistrationBean.class);
            FilterRegistrationBean<?> registration = context.getBean(FilterRegistrationBean.class);
            assertThat(registration.getFilter()).isInstanceOf(ReadOnlyActorFilter.class);
            assertThat(registration.getOrder()).isEqualTo(ReadOnlyActorConfiguration.ORDER);
            assertThat(context.getBean(ReadOnlyActorProperties.class).allowedWrites()).containsExactly("/**/list");
        });
    }

}
