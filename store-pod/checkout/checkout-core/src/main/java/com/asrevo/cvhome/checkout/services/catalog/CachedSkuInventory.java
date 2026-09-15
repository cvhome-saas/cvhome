package com.asrevo.cvhome.checkout.services.catalog;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.cache.StoreScopedKey;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.AvailabilityQuery;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.services.ExternalInventoryService;
import com.github.benmanes.caffeine.cache.Cache;

import lombok.RequiredArgsConstructor;

/**
 * Inventory's price and stock per sku, held for {@link #TTL} so a cart read is answered without a call to inventory.
 *
 * <p>
 * A cart is read far more often than it changes: every page of the storefront reads it, and the 2026-09-15 spike
 * priced 2,634 cart calls with one HTTP round trip to inventory each, on a checkout whose CPU was the wall. A sku's
 * price and stock are held one entry each, keyed by store and sku, so any cart's skus are answered from whatever
 * entries exist plus one read for the rest, and every cart on the task shares them. A sku inventory does not know is
 * answered absent and not held: the next read asks again.
 * </p>
 *
 * <p>
 * No event tells checkout that inventory changed, so the entries are short-lived rather than evicted: five seconds
 * is the staleness a cart's displayed price or "in stock" may have. What must be live never reads through here: an
 * add checks the sku it adds against inventory itself, and a placement prices the whole cart live before it
 * reserves, then {@link #forget forgets} its skus so the reads that follow on this task see the stock it took.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class CachedSkuInventory {

    public static final String CACHE = "INVENTORY_SKU";

    public static final Duration TTL = Duration.ofSeconds(5);

    private final CacheManager caches;

    private final ExternalInventoryService inventory;

    /** The skus' inventory, in the order asked; a sku inventory does not know is absent. */
    public Map<Sku, SkuInventory> stock(StoreMerchantId store, List<Sku> skus) {
        if (skus == null || skus.isEmpty()) {
            return Map.of();
        }
        List<Sku> distinct = skus.stream().distinct().toList();
        Map<StoreScopedKey, SkuInventory> found = cache().getAll(
                distinct.stream().map(sku -> key(store, sku)).toList(), missing -> load(store, missing));
        Map<Sku, SkuInventory> stock = new LinkedHashMap<>();
        for (Sku sku : distinct) {
            SkuInventory entry = found.get(key(store, sku));
            if (entry != null) {
                stock.put(sku, entry);
            }
        }
        return stock;
    }

    /** Drops the skus' entries: the next read asks inventory. */
    public void forget(StoreMerchantId store, Collection<Sku> skus) {
        if (skus == null || skus.isEmpty()) {
            return;
        }
        cache().invalidateAll(skus.stream().distinct().map(sku -> key(store, sku)).toList());
    }

    private Map<StoreScopedKey, SkuInventory> load(StoreMerchantId store, Set<? extends StoreScopedKey> keys) {
        List<Sku> skus = keys.stream().map(key -> (Sku) key.arguments().get(0)).toList();
        Map<StoreScopedKey, SkuInventory> loaded = new LinkedHashMap<>();
        for (SkuInventory entry : inventory.queryBySkus(store, new AvailabilityQuery(skus))) {
            loaded.put(key(store, entry.sku()), entry);
        }
        return loaded;
    }

    private static StoreScopedKey key(StoreMerchantId store, Sku sku) {
        List<Object> arguments = new ArrayList<>(1);
        arguments.add(sku);
        return new StoreScopedKey(store, arguments);
    }

    @SuppressWarnings("unchecked")
    private Cache<StoreScopedKey, SkuInventory> cache() {
        org.springframework.cache.Cache cache = Objects.requireNonNull(caches.getCache(CACHE),
                "the sku inventory cache is registered by CacheConfig");
        return (Cache<StoreScopedKey, SkuInventory>) cache.getNativeCache();
    }
}
