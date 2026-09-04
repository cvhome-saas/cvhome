package com.asrevo.cvhome.sso.ratelimit;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

/**
 * A fixed window counter per key, held in memory.
 *
 * <p>
 * Caffeine's expiry is the window: the first hit creates the counter, the last hit within the window is the one that
 * gets refused, and the counter vanishes on its own. In-memory is right for one instance and adequate for a few —
 * the limits are a brake on guessing, not an accounting system.
 * </p>
 */
public class RateLimiter {

    private static final long MAX_KEYS = 100_000;

    private final Cache<String, AtomicInteger> counters;

    private final int limit;

    private final Duration window;

    public RateLimiter(RateLimitProperties.Rule rule) {
        this(rule, Ticker.systemTicker());
    }

    RateLimiter(RateLimitProperties.Rule rule, Ticker ticker) {
        this.limit = rule.limit();
        this.window = rule.window();
        this.counters = Caffeine.newBuilder().expireAfterWrite(window).maximumSize(MAX_KEYS).ticker(ticker).build();
    }

    /** Counts one attempt for {@code key}; {@code true} while it is within the limit. */
    public boolean tryAcquire(String key) {
        AtomicInteger counter = counters.get(key, k -> new AtomicInteger());
        return counter != null && counter.incrementAndGet() <= limit;
    }

    public Duration window() {
        return window;
    }

}
