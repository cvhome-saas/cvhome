package com.asrevo.cvhome.cache;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asrevo.cvhome.cache.config.CacheProperties;
import com.asrevo.cvhome.cache.provider.CacheProvider;
import com.asrevo.cvhome.cache.provider.NoOpCacheProvider;
import com.asrevo.cvhome.cache.provider.RegionSpec;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Every region of the service, built once from its declaration, the configuration and the providers present.
 *
 * <p>
 * The registry is where the run-time switches are read: a region's provider, time-to-live, size and whether it is
 * on at all come from {@link CacheProperties} over the enum's defaults. A provider named in configuration that is
 * not a bean stops the service at start-up rather than caching nothing quietly; a region named in configuration
 * that the service does not declare is logged once, because the shared configuration lists every service's.
 * </p>
 */
public final class CacheRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(CacheRegistry.class);

    private final Map<String, RegionCache<?>> caches = new LinkedHashMap<>();

    private final Map<String, CacheRegion> regions = new LinkedHashMap<>();

    public CacheRegistry(CacheRegions declared, Collection<CacheProvider> providers, CacheProperties properties) {
        Map<String, CacheProvider> byName = new LinkedHashMap<>();
        for (CacheProvider provider : providers) {
            byName.put(provider.name(), provider);
        }
        CacheProvider off = new NoOpCacheProvider();
        for (CacheRegion region : declared.regions()) {
            RegionSpec spec = specOf(region, properties);
            CacheProperties.Region override = properties.region(region.regionName());
            CacheProvider provider = off;
            if (override.isEnabled()) {
                String name = override.provider() == null ? properties.defaultProvider() : override.provider();
                provider = byName.get(name);
                if (provider == null) {
                    throw new IllegalStateException(String.format(
                            "cache region %s names provider '%s', which is not on this service: %s",
                            region.regionName(), name, byName.keySet()));
                }
            }
            regions.put(region.regionName(), region);
            caches.put(region.regionName(), provider.create(spec));
            LOG.info("cache region {}: provider={} ttl={} max-size={} scope={}", region.regionName(), provider.name(),
                    spec.ttl(), spec.maxSize(), spec.scope());
        }
        for (String name : properties.configuredNames()) {
            if (!regions.containsKey(name)) {
                LOG.debug("cache region {} is configured but not declared by this service", name);
            }
        }
    }

    /** The region's cache, typed; the type is the region's declared value type. */
    @SuppressWarnings("unchecked")
    public <V> RegionCache<V> region(CacheRegion region, Class<V> valueType) {
        CacheRegion declared = regions.get(region.regionName());
        if (declared == null) {
            throw new IllegalArgumentException(String.format("cache region not declared by this service: %s", region.regionName()));
        }
        if (!valueType.isAssignableFrom(declared.valueType())) {
            throw new IllegalArgumentException(String.format("cache region %s holds %s, not %s", region.regionName(),
                    declared.valueType().getName(), valueType.getName()));
        }
        return (RegionCache<V>) caches.get(region.regionName());
    }

    public Optional<RegionCache<?>> region(String name) {
        return Optional.ofNullable(caches.get(name));
    }

    public Optional<CacheRegion> declaration(String name) {
        return Optional.ofNullable(regions.get(name));
    }

    public Collection<RegionCache<?>> regions() {
        return List.copyOf(caches.values());
    }

    public List<String> names() {
        return List.copyOf(caches.keySet());
    }

    /** Drops {@code store}'s entries from each of {@code regions}, now. */
    public void evictStore(StoreMerchantId store, Collection<? extends CacheRegion> regions) {
        for (CacheRegion region : regions) {
            RegionCache<?> cache = caches.get(region.regionName());
            if (cache != null) {
                cache.evictStore(store);
            }
        }
    }

    private static RegionSpec specOf(CacheRegion region, CacheProperties properties) {
        CacheProperties.Region override = properties.region(region.regionName());
        return new RegionSpec(region.regionName(), region.valueType(),
                override.ttl() == null ? region.ttl() : override.ttl(),
                override.maxSize() == null ? region.maxSize() : override.maxSize(), region.scope());
    }
}
