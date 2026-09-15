package com.asrevo.cvhome.checkout.config;

import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.checkout.services.catalog.CachedSkuInventory;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * The merchant store read (STORE, common-config.yml) and inventory's price and stock per sku
 * ({@link CachedSkuInventory}, {@value CachedSkuInventory#CACHE}, a few seconds), bounded and counted like every other
 * Spring cache. {@code spring.cache.cache-names} fixes the manager's caches, so the sku cache is registered by name.
 * What must be live (an add's check, a placement's pricing) never reads a cache.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** One entry per sku a task has priced in the last seconds; a store's whole catalogue fits many times over. */
    private static final long SKU_ENTRIES = 50_000;

    @Bean
    CacheManagerCustomizer<CaffeineCacheManager> skuInventoryCache() {
        return manager -> manager.registerCustomCache(CachedSkuInventory.CACHE, Caffeine.newBuilder()
                .expireAfterWrite(CachedSkuInventory.TTL).maximumSize(SKU_ENTRIES).recordStats().build());
    }
}
