package com.asrevo.cvhome.uaa.ratelimit;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    private static final String KEY = "10.0.0.1|/login";

    @Test
    void refusesTheAttemptAfterTheLimitUntilTheWindowPasses() {
        AtomicLong nanos = new AtomicLong();
        RateLimiter limiter = new RateLimiter(new RateLimitProperties.Rule(3, Duration.ofMinutes(1)), nanos::get);

        assertThat(limiter.tryAcquire(KEY)).isTrue();
        assertThat(limiter.tryAcquire(KEY)).isTrue();
        assertThat(limiter.tryAcquire(KEY)).isTrue();
        assertThat(limiter.tryAcquire(KEY)).isFalse();
        assertThat(limiter.tryAcquire("10.0.0.2|/login")).isTrue();

        nanos.addAndGet(Duration.ofSeconds(61).toNanos());
        assertThat(limiter.tryAcquire(KEY)).isTrue();
    }

    @Test
    void defaultsAreFilledIn() {
        RateLimitProperties properties = new RateLimitProperties(true, null, null, null);

        assertThat(properties.login().limit()).isEqualTo(10);
        assertThat(properties.token().limit()).isEqualTo(60);
        assertThat(properties.publicApi().window()).isEqualTo(Duration.ofMinutes(1));
    }

}
