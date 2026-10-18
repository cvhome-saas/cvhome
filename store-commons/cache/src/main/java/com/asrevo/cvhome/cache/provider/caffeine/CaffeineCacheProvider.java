package com.asrevo.cvhome.cache.provider.caffeine;

import com.asrevo.cvhome.cache.RegionCache;
import com.asrevo.cvhome.cache.provider.CacheProvider;
import com.asrevo.cvhome.cache.provider.RegionSpec;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;

/**
 * In-process regions on Caffeine: bounded, expiring after write, counting hits and misses. One copy per task, so
 * the region's time-to-live is what another task may lag by after a write.
 */
public final class CaffeineCacheProvider implements CacheProvider {

    public static final String NAME = "caffeine";

    private final Ticker ticker;

    public CaffeineCacheProvider() {
        this(Ticker.systemTicker());
    }

    /** With a clock of the test's choosing. */
    public CaffeineCacheProvider(Ticker ticker) {
        this.ticker = ticker;
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public <V> RegionCache<V> create(RegionSpec spec) {
        return new CaffeineRegionCache<>(spec.name(), Caffeine.newBuilder().expireAfterWrite(spec.ttl())
                .maximumSize(spec.maxSize()).ticker(ticker).recordStats().build());
    }
}
