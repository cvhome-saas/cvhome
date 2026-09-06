package com.asrevo.cvhome.metrics;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.MultiGauge;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;

import lombok.extern.slf4j.Slf4j;

/**
 * Gauges over the transactional outbox: how many records sit in each status, and how long the oldest unfinished one
 * has waited. Together they answer "is the outbox keeping up, and is anything stuck or failed".
 *
 * <p>
 * The table is read at most once per {@code refresh} and only when a meter is exported, so a busy service pays one
 * small aggregate query a minute and an idle one pays the same. A failed read keeps the previous reading — an outbox
 * dashboard that goes blank the moment the database hiccups would hide exactly the situation it exists for.
 * </p>
 */
@Slf4j
public class OutboxMetrics implements MeterBinder {

    static final String RECORDS = "cvhome.outbox.records";

    static final String OLDEST_PENDING = "cvhome.outbox.oldest_pending.seconds";

    /**
     * Where the numbers come from; the JDBC implementation is {@link JdbcOutboxSource}.
     */
    public interface Source {

        /**
         * Record count per status value.
         */
        Map<String, Long> countsByStatus();

        /**
         * Creation time of the oldest record that is neither completed nor failed.
         */
        Optional<Instant> oldestPending();

    }

    private final Source source;

    private final Duration refresh;

    private final Clock clock;

    private Instant readAt = Instant.MIN;

    private Map<String, Long> counts = Map.of();

    private double oldestPendingSeconds;

    private MultiGauge records;

    public OutboxMetrics(Source source, Duration refresh) {
        this(source, refresh, Clock.systemUTC());
    }

    OutboxMetrics(Source source, Duration refresh, Clock clock) {
        this.source = source;
        this.refresh = refresh;
        this.clock = clock;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        records = MultiGauge.builder(RECORDS)
                .description("Outbox records by status")
                .register(registry);
        Gauge.builder(OLDEST_PENDING, this, OutboxMetrics::oldestPending)
                .description("Age of the oldest outbox record that is neither completed nor failed")
                .baseUnit("seconds")
                .register(registry);
    }

    double oldestPending() {
        refreshIfStale();
        return oldestPendingSeconds;
    }

    synchronized void refreshIfStale() {
        Instant now = clock.instant();
        if (Duration.between(readAt, now).compareTo(refresh) < 0) {
            return;
        }
        readAt = now;
        try {
            counts = source.countsByStatus();
            oldestPendingSeconds = source.oldestPending()
                    .map(created -> (double) Duration.between(created, now).toSeconds())
                    .orElse(0.0);
        } catch (RuntimeException e) {
            log.debug("Outbox metrics not refreshed; keeping the previous reading", e);
            return;
        }
        List<MultiGauge.Row<?>> rows = new ArrayList<>(counts.size());
        counts.forEach((status, count) -> rows.add(MultiGauge.Row.of(Tags.of("status", status), count)));
        records.register(rows, true);
    }

    Map<String, Long> counts() {
        return counts;
    }

}
