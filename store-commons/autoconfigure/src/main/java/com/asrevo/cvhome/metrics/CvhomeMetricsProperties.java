package com.asrevo.cvhome.metrics;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Switches for the meters cvhome adds on top of the framework's own.
 *
 * @param outbox the transactional-outbox gauges; off unless the service has an outbox table
 */
@ConfigurationProperties(prefix = "cvhome.metrics")
public record CvhomeMetricsProperties(@DefaultValue Outbox outbox) {

    /**
     * @param enabled whether to poll the outbox table at all
     * @param table   the outbox table, schema-qualified when the service's default schema is not where it lives
     * @param refresh how old a reading may be before the next export re-queries the table
     */
    public record Outbox(@DefaultValue("false") boolean enabled,
                         @DefaultValue("outbox_record") String table,
                         @DefaultValue("15s") Duration refresh) {
    }

}
