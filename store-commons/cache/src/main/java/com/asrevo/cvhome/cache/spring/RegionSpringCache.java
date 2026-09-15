package com.asrevo.cvhome.cache.spring;

import java.util.concurrent.Callable;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.RegionCache;

/**
 * A region as Spring's cache abstraction sees it: what {@code @Cacheable} reads and writes through.
 *
 * <p>
 * The key is always a {@link CacheKey}, because the manager pairs this with {@code StoreScopedKeyGenerator} and
 * nothing else builds keys; any other key is a wiring mistake and is refused. A {@code null} value is never stored:
 * a read that found nothing is asked again next time.
 * </p>
 */
public final class RegionSpringCache implements Cache {

    private final RegionCache<Object> region;

    public RegionSpringCache(RegionCache<Object> region) {
        this.region = region;
    }

    public RegionCache<Object> region() {
        return region;
    }

    @Override
    public String getName() {
        return region.name();
    }

    @Override
    public Object getNativeCache() {
        return region;
    }

    @Override
    public ValueWrapper get(Object key) {
        return region.getIfPresent(keyOf(key)).map(value -> (ValueWrapper) new SimpleValueWrapper(value)).orElse(null);
    }

    @Override
    public <T> T get(Object key, Class<T> type) {
        Object value = region.getIfPresent(keyOf(key)).orElse(null);
        if (value != null && type != null && !type.isInstance(value)) {
            throw new IllegalStateException(String.format("cache region %s holds %s, not %s", region.name(),
                    value.getClass().getName(), type.getName()));
        }
        return type == null ? (T) value : type.cast(value);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        return (T) region.get(keyOf(key), ignored -> {
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new ValueRetrievalException(key, valueLoader, e);
            }
        });
    }

    @Override
    public void put(Object key, Object value) {
        region.put(keyOf(key), value);
    }

    @Override
    public void evict(Object key) {
        region.evict(keyOf(key));
    }

    @Override
    public void clear() {
        region.clear();
    }

    private static CacheKey keyOf(Object key) {
        if (key instanceof CacheKey typed) {
            return typed;
        }
        throw new IllegalArgumentException(String.format(
                "a cached read is keyed by the store-scoped key generator, not by %s", key == null ? "null" : key.getClass().getName()));
    }
}
