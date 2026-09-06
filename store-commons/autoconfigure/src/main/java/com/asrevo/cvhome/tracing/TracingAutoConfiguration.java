package com.asrevo.cvhome.tracing;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import io.opentelemetry.api.trace.Span;

/**
 * Request-scoped trace context for the servlet services: the MDC ids that join an error response, its log line and
 * its trace. Reactive services (the gateway) get the ids on their log records from the OpenTelemetry appender
 * directly and have no ProblemDetail body to stamp.
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({Span.class, OncePerRequestFilter.class})
public class TracingAutoConfiguration {

    /**
     * Right after the OpenTelemetry starter's request filter, which sits at {@code HIGHEST_PRECEDENCE + 1} and is
     * what makes the server span current; at the same order the two would run in undefined order, and earlier there
     * is no span to read.
     */
    static final int MDC_FILTER_ORDER = Ordered.HIGHEST_PRECEDENCE + 2;

    @Bean
    @ConditionalOnMissingBean(TraceContextMdcFilter.class)
    FilterRegistrationBean<TraceContextMdcFilter> traceContextMdcFilter() {
        var registration = new FilterRegistrationBean<>(new TraceContextMdcFilter());
        registration.setOrder(MDC_FILTER_ORDER);
        return registration;
    }

}
