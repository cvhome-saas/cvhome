package com.asrevo.cvhome.inventory.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.cache.CacheRegions;
import com.asrevo.cvhome.cache.event.PriceChanged;
import com.asrevo.cvhome.cache.event.StockChanged;
import com.asrevo.cvhome.cache.eviction.EvictionRules;
import com.asrevo.cvhome.inventory.entity.Inventory;
import com.asrevo.cvhome.inventory.entity.InventoryPrice;
import com.asrevo.cvhome.inventory.reads.InventoryRegions;

/**
 * What inventory caches ({@link InventoryRegions}) and what drops it: a stock row or a price row. A reservation
 * changes the stock row it takes from, so its commit is covered by the same rule; the reservation rows themselves
 * are not read by the storefront. The events the row raises ({@link StockChanged}, {@link PriceChanged}) drop the
 * same region on every task once the outbox drains them.
 */
@Configuration
public class CacheConfig {

    @Bean
    CacheRegions inventoryRegions() {
        return CacheRegions.of(InventoryRegions.values());
    }

    @Bean
    EvictionRules inventoryEvictionRules() {
        return EvictionRules.in("com.asrevo.cvhome.inventory.entity")
                .on(Inventory.class, InventoryPrice.class).evict(InventoryRegions.SKU)
                .onEvent(StockChanged.class, PriceChanged.class).evict(InventoryRegions.SKU)
                .build();
    }
}
