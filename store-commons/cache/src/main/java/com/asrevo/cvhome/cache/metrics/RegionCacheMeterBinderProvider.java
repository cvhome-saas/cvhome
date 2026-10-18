package com.asrevo.cvhome.cache.metrics;

import org.springframework.boot.cache.metrics.CacheMeterBinderProvider;

import com.asrevo.cvhome.cache.spring.RegionSpringCache;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * What Boot's cache metrics auto-configuration asks for the binder of a {@link RegionSpringCache}: every region of
 * the manager is bound when the context starts, like a Caffeine or Redis cache would be.
 */
public final class RegionCacheMeterBinderProvider implements CacheMeterBinderProvider<RegionSpringCache> {

    @Override
    public MeterBinder getMeterBinder(RegionSpringCache cache, Iterable<Tag> tags) {
        return new RegionCacheMeterBinder(cache, tags);
    }
}
