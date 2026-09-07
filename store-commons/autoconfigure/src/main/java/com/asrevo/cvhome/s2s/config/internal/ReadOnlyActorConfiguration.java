package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.errors.web.ProblemDetailFactory;

import tools.jackson.databind.ObjectMapper;

/**
 * Registers {@link ReadOnlyActorFilter} on every servlet service, just after the security chain.
 *
 * <p>
 * A servlet filter rather than part of each service's {@code SecurityConfig}, so no service can forget it: the
 * order places it behind Spring Security's own filter, where the principal has been resolved and the refusal can
 * be written in the shared problem-detail shape.
 * </p>
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnBean(ProblemDetailFactory.class)
@EnableConfigurationProperties(ReadOnlyActorProperties.class)
public class ReadOnlyActorConfiguration {

    /** Behind Spring Security's filter, which Boot registers at {@code -100}; well ahead of the dispatcher. */
    static final int ORDER = -90;

    @Bean
    FilterRegistrationBean<ReadOnlyActorFilter> readOnlyActorFilter(ReadOnlyActorProperties properties,
                                                                    ProblemDetailFactory problems, ObjectMapper json) {
        FilterRegistrationBean<ReadOnlyActorFilter> registration =
                new FilterRegistrationBean<>(new ReadOnlyActorFilter(properties, problems, json));
        registration.setOrder(ORDER);
        return registration;
    }

}
