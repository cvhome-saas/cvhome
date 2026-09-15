package com.asrevo.cvhome.inventory.reads;

import java.time.Duration;

import com.asrevo.cvhome.cache.CacheRegion;
import com.asrevo.cvhome.inventory.model.SkuInventory;

/**
 * What inventory caches: a sku's stock and price, five seconds, in the service that owns them, so a caller reads a
 * figure at most seconds old and never keeps a copy of its own.
 */
public enum InventoryRegions implements CacheRegion {

    SKU(Names.SKU);

    private final String name;

    InventoryRegions(String name) {
        this.name = name;
    }

    @Override
    public String regionName() {
        return name;
    }

    @Override
    public Class<?> valueType() {
        return SkuInventory.class;
    }

    @Override
    public Duration ttl() {
        return Duration.ofSeconds(5);
    }

    @Override
    public long maxSize() {
        return 50_000;
    }

    /** The names, as {@code @Cacheable} needs a constant. */
    public static final class Names {

        public static final String SKU = "inventory.sku";

        private Names() {
        }
    }
}
