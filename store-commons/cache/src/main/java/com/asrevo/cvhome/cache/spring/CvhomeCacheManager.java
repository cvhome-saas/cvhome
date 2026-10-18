package com.asrevo.cvhome.cache.spring;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.RegionCache;

/**
 * Spring's {@link CacheManager} over the registry: a cache per declared region, by the region's name, and no cache
 * for a name the service did not declare. Every service that caches has this one manager, so {@code @Cacheable}
 * needs no {@code cacheManager} attribute.
 */
public final class CvhomeCacheManager implements CacheManager {

    private final Map<String, RegionSpringCache> caches = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    public CvhomeCacheManager(CacheRegistry registry) {
        for (RegionCache<?> region : registry.regions()) {
            caches.put(region.name(), new RegionSpringCache((RegionCache<Object>) region));
        }
    }

    @Override
    public Cache getCache(String name) {
        return caches.get(name);
    }

    @Override
    public Collection<String> getCacheNames() {
        return caches.keySet();
    }
}
