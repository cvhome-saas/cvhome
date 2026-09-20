package com.asrevo.cvhome.cache.metrics;

import com.asrevo.cvhome.cache.RegionStats;
import com.asrevo.cvhome.cache.spring.RegionSpringCache;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.cache.CacheMeterBinder;

/**
 * A region's counters as Micrometer's standard cache meters: {@code cache.gets{result=hit|miss}}, {@code cache.puts},
 * {@code cache.evictions}, {@code cache.size}, tagged {@code cache=<region name>}. The same series every other Spring
 * cache emits, so the dashboards read a region like any cache; whatever the provider.
 */
public final class RegionCacheMeterBinder extends CacheMeterBinder<RegionSpringCache> {

    public RegionCacheMeterBinder(RegionSpringCache cache, Iterable<Tag> tags) {
        super(cache, cache.getName(), tags);
    }

    @Override
    protected Long size() {
        return stats().size();
    }

    @Override
    protected long hitCount() {
        return stats().hits();
    }

    @Override
    protected Long missCount() {
        return stats().misses();
    }

    @Override
    protected Long evictionCount() {
        return stats().evictions();
    }

    @Override
    protected long putCount() {
        return stats().puts();
    }

    @Override
    protected void bindImplementationSpecificMetrics(MeterRegistry registry) {
        // The standard five are the whole story of a region.
    }

    private RegionStats stats() {
        return getCache().region().stats();
    }
}
