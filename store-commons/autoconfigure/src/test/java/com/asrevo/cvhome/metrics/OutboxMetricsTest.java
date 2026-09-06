package com.asrevo.cvhome.metrics;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The gauges follow the source, re-read it only once the reading is stale, and keep the last reading when a read
 * fails.
 */
class OutboxMetricsTest {

    private static final Instant T0 = Instant.parse("2026-09-06T10:00:00Z");

    private static final String NEW = "NEW";

    private static final String FAILED = "FAILED";

    private static final String STATUS = "status";

    private final MutableClock clock = new MutableClock(T0);

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();

    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        @Override
        public Instant instant() {
            return now;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

    }

    private static final class FakeSource implements OutboxMetrics.Source {

        final AtomicInteger reads = new AtomicInteger();

        Map<String, Long> counts = Map.of(NEW, 3L, FAILED, 1L);

        Optional<Instant> oldest = Optional.of(T0.minusSeconds(120));

        boolean failing;

        @Override
        public Map<String, Long> countsByStatus() {
            reads.incrementAndGet();
            if (failing) {
                throw new IllegalStateException("db down");
            }
            return counts;
        }

        @Override
        public Optional<Instant> oldestPending() {
            return oldest;
        }

    }

    private double gauge(String name, String... tags) {
        return registry.find(name).tags(tags).gauge().value();
    }

    @Test
    void gaugesReflectTheSource() {
        FakeSource source = new FakeSource();
        new OutboxMetrics(source, Duration.ofSeconds(15), clock).bindTo(registry);

        assertThat(gauge(OutboxMetrics.OLDEST_PENDING)).isEqualTo(120);
        assertThat(gauge(OutboxMetrics.RECORDS, STATUS, NEW)).isEqualTo(3);
        assertThat(gauge(OutboxMetrics.RECORDS, STATUS, FAILED)).isEqualTo(1);
    }

    @Test
    void theSourceIsReadOnceUntilTheReadingIsStale() {
        FakeSource source = new FakeSource();
        OutboxMetrics metrics = new OutboxMetrics(source, Duration.ofSeconds(15), clock);
        metrics.bindTo(registry);

        metrics.oldestPending();
        metrics.oldestPending();
        assertThat(source.reads).hasValue(1);

        clock.now = T0.plusSeconds(16);
        metrics.oldestPending();
        assertThat(source.reads).hasValue(2);
    }

    @Test
    void aFailedReadKeepsThePreviousReading() {
        FakeSource source = new FakeSource();
        OutboxMetrics metrics = new OutboxMetrics(source, Duration.ofSeconds(15), clock);
        metrics.bindTo(registry);
        metrics.oldestPending();

        source.failing = true;
        clock.now = T0.plusSeconds(30);
        metrics.oldestPending();

        assertThat(metrics.counts()).containsEntry(NEW, 3L);
        assertThat(gauge(OutboxMetrics.RECORDS, STATUS, NEW)).isEqualTo(3);
    }

    @Test
    void noPendingRecordMeansZeroAge() {
        FakeSource source = new FakeSource();
        source.oldest = Optional.empty();
        new OutboxMetrics(source, Duration.ofSeconds(15), clock).bindTo(registry);

        assertThat(gauge(OutboxMetrics.OLDEST_PENDING)).isZero();
    }

}
