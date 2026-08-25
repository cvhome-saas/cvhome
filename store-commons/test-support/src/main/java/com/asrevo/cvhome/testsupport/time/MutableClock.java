package com.asrevo.cvhome.testsupport.time;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A clock the tests can move, so scheduling can be exercised without sleeping.
 */
public final class MutableClock extends Clock {

    private final AtomicReference<Instant> now = new AtomicReference<>(wallClockInstant());

    @Override
    public ZoneId getZone() {
        return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return this;
    }

    @Override
    public Instant instant() {
        return now.get();
    }

    public void advance(Duration duration) {
        now.updateAndGet(i -> databaseInstant(i.plus(duration)));
    }

    public void set(Instant instant) {
        now.set(databaseInstant(instant));
    }

    /**
     * Back to the current wall-clock instant. A test that moves the clock leaves it moved for every later test in
     * the same context, so reset it in {@code @BeforeEach} rather than at the end of the test that advanced it.
     */
    public void reset() {
        now.set(wallClockInstant());
    }

    private static Instant wallClockInstant() {
        return databaseInstant(Instant.now());
    }

    /**
     * PostgreSQL timestamps have microsecond precision. Keeping the test clock at the same precision prevents a
     * persisted instant from rounding a few nanoseconds into the apparent future while the clock remains fixed.
     */
    private static Instant databaseInstant(Instant instant) {
        return instant.truncatedTo(ChronoUnit.MICROS);
    }

}
