package com.asrevo.cvhome.cache.provider;

import java.time.Duration;

import com.asrevo.cvhome.cache.CacheRegion;

/**
 * A region as it is to be built: its declaration with the configuration applied.
 *
 * @param name      the region's name
 * @param valueType what it holds
 * @param ttl       how long an entry is trusted
 * @param maxSize   the most entries kept
 * @param scope     store-scoped or global
 */
public record RegionSpec(String name, Class<?> valueType, Duration ttl, long maxSize, CacheRegion.Scope scope) {

    public RegionSpec {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("a region has a name");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException(String.format("%s: a region's ttl is positive", name));
        }
        if (maxSize <= 0) {
            throw new IllegalArgumentException(String.format("%s: a region's max-size is positive", name));
        }
    }

    public static RegionSpec of(CacheRegion region) {
        return new RegionSpec(region.regionName(), region.valueType(), region.ttl(), region.maxSize(), region.scope());
    }
}
