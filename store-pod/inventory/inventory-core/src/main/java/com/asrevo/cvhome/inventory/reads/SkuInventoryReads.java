package com.asrevo.cvhome.inventory.reads;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.RegionCache;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.services.InventoryService;

/**
 * A sku's stock and price, held one sku at a time for a few seconds: what the storefront asks on every page and
 * checkout on every cart operation. Any subset of skus is answered from the entries that exist plus one read for
 * the rest; a sku with no inventory row is answered absent and not held. A committed write to a row, a reservation
 * included, drops the store's entries.
 */
@Component
public class SkuInventoryReads {

    private final RegionCache<SkuInventory> region;

    private final InventoryService inventory;

    public SkuInventoryReads(CacheRegistry registry, InventoryService inventory) {
        this.region = registry.region(InventoryRegions.SKU, SkuInventory.class);
        this.inventory = inventory;
    }

    public List<SkuInventory> bySkus(StoreMerchantId store, Collection<Sku> skus) {
        if (skus == null || skus.isEmpty()) {
            return List.of();
        }
        List<Sku> distinct = skus.stream().distinct().toList();
        Map<CacheKey, SkuInventory> found = region.getAll(distinct.stream().map(sku -> CacheKey.sku(store, sku)).toList(),
                missing -> load(store, missing));
        List<SkuInventory> lines = new ArrayList<>(distinct.size());
        for (Sku sku : distinct) {
            SkuInventory line = found.get(CacheKey.sku(store, sku));
            if (line != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private Map<CacheKey, SkuInventory> load(StoreMerchantId store, Set<CacheKey> keys) {
        List<Sku> skus = keys.stream().map(key -> (Sku) key.parts().getFirst()).toList();
        Map<CacheKey, SkuInventory> loaded = new LinkedHashMap<>();
        for (SkuInventory line : inventory.getBySkus(store, skus)) {
            loaded.put(CacheKey.sku(store, line.sku()), line);
        }
        return loaded;
    }
}
