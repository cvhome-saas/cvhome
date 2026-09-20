package com.asrevo.cvhome.cache.provider;

import com.asrevo.cvhome.cache.RegionCache;

/**
 * Where a region's entries live: the port a provider implements. Caffeine is the one in this module; a remote
 * provider is its own module and a bean of this type, and a region moves to it by naming it in configuration.
 */
public interface CacheProvider {

    /** The name configuration refers to: {@code caffeine}, later {@code redis}. */
    String name();

    <V> RegionCache<V> create(RegionSpec spec);
}
