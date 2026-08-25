package com.asrevo.cvhome.testsupport.time;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MutableClockTest {

    @Test
    void keepsInstantsAtPostgresPrecision() {
        MutableClock clock = new MutableClock();

        clock.set(Instant.parse("2026-08-24T20:00:03.341981798Z"));
        assertThat(clock.instant()).isEqualTo(Instant.parse("2026-08-24T20:00:03.341981Z"));

        clock.advance(Duration.ofNanos(1_999));
        assertThat(clock.instant()).isEqualTo(Instant.parse("2026-08-24T20:00:03.341982Z"));
    }

}
