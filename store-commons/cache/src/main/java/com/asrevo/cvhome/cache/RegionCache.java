package com.asrevo.cvhome.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * One region as a provider serves it: what the Spring bridge and the bulk readers call.
 *
 * <p>
 * A loader that returns {@code null}, or a bulk loader that leaves a key out, caches nothing for that key: a sku
 * the catalogue does not know is asked again next time, and a product created a moment later is seen at once.
 * {@link #evictStore} is the one operation every provider must make cheap: a store's write drops every entry of
 * that store in the region, however many.
 * </p>
 *
 * @param <V> the region's value type
 */
public interface RegionCache<V> {

    String name();

    Optional<V> getIfPresent(CacheKey key);

    /** The value under {@code key}, loading and storing it on a miss; a {@code null} from the loader is not kept. */
    V get(CacheKey key, Function<CacheKey, V> loader);

    /** The values under {@code keys}, one bulk load for the keys that miss; a key the loader leaves out is absent. */
    Map<CacheKey, V> getAll(Collection<CacheKey> keys, Function<Set<CacheKey>, Map<CacheKey, V>> loader);

    void put(CacheKey key, V value);

    void evict(CacheKey key);

    void evictAll(Collection<CacheKey> keys);

    /** Drops every entry of {@code store}, at once, without visiting them. */
    void evictStore(StoreMerchantId store);

    void clear();

    RegionStats stats();
}
