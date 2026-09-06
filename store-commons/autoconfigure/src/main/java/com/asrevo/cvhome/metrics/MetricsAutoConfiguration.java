package com.asrevo.cvhome.metrics;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * The meters cvhome adds on top of Micrometer's and OpenTelemetry's own, in every service that imports this module.
 * The names, their meaning and the dashboards that read them: {@code extra/monitoring/docs/kpis.md}.
 */
@Configuration
@ConditionalOnClass(MeterRegistry.class)
@EnableConfigurationProperties(CvhomeMetricsProperties.class)
public class MetricsAutoConfiguration {

    /**
     * Before Spring Security's chain (whose order is -100) so the filter sees the status the chain decided on.
     */
    static final int AUTH_REJECTION_FILTER_ORDER = -110;

    @Configuration
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(OncePerRequestFilter.class)
    static class ServletMetrics {

        @Bean
        @ConditionalOnMissingBean(AuthRejectionMetricsFilter.class)
        FilterRegistrationBean<AuthRejectionMetricsFilter> authRejectionMetricsFilter(MeterRegistry registry) {
            var registration = new FilterRegistrationBean<>(new AuthRejectionMetricsFilter(registry));
            registration.setOrder(AUTH_REJECTION_FILTER_ORDER);
            return registration;
        }

    }

    @Bean
    @ConditionalOnClass(JdbcTemplate.class)
    @ConditionalOnProperty(prefix = "cvhome.metrics.outbox", name = "enabled", havingValue = "true")
    @ConditionalOnMissingBean(OutboxMetrics.class)
    OutboxMetrics outboxMetrics(CvhomeMetricsProperties properties, ObjectProvider<DataSource> dataSource) {
        CvhomeMetricsProperties.Outbox outbox = properties.outbox();
        // The DataSource is resolved on the first read, not here: this configuration is imported early and the
        // auto-configured DataSource may not exist yet when it is processed.
        OutboxMetrics.Source lazy = new OutboxMetrics.Source() {

            private JdbcOutboxSource delegate;

            private JdbcOutboxSource delegate() {
                if (delegate == null) {
                    delegate = new JdbcOutboxSource(new JdbcTemplate(dataSource.getObject()), outbox.table());
                }
                return delegate;
            }

            @Override
            public java.util.Map<String, Long> countsByStatus() {
                return delegate().countsByStatus();
            }

            @Override
            public java.util.Optional<java.time.Instant> oldestPending() {
                return delegate().oldestPending();
            }

        };
        return new OutboxMetrics(lazy, outbox.refresh());
    }

}
