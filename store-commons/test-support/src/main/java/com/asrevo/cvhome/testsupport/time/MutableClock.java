package com.asrevo.cvhome.testsupport.time;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A clock the tests can move, so scheduling can be exercised without sleeping.
 */
public final class MutableClock extends Clock {

    private final AtomicReference<Instant> now = new AtomicReference<>(Instant.now());

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
        now.updateAndGet(i -> i.plus(duration));
    }

    public void set(Instant instant) {
        now.set(instant);
    }

}
