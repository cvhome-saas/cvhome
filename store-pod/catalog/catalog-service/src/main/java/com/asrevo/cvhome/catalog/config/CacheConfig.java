package com.asrevo.cvhome.catalog.config;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.cache.EntityCommitCacheEviction;
import com.asrevo.cvhome.catalog.services.CachedStorefrontCatalog;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * The merchant store read (STORE, common-config.yml) and the storefront's catalog reads ({@link CachedStorefrontCatalog}),
 * each held for thirty seconds, bounded, and counted like every other Spring cache. {@code spring.cache.cache-names}
 * fixes the manager's caches, so the storefront's are registered by name.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final long STOREFRONT_ENTRIES = 10_000;

    @Bean
    CacheManagerCustomizer<CaffeineCacheManager> storefrontCatalogCaches() {
        return manager -> CachedStorefrontCatalog.CACHES.forEach(name -> manager.registerCustomCache(name,
                Caffeine.newBuilder().expireAfterWrite(CachedStorefrontCatalog.TTL).maximumSize(STOREFRONT_ENTRIES)
                        .recordStats().build()));
    }

    /**
     * A merchant's change clears the storefront's catalog caches when it commits. Catalog emits events for product and
     * brand changes only; categories, groups and options change without one.
     */
    @Bean
    EntityCommitCacheEviction storefrontCatalogCacheEviction(EntityManagerFactory entityManagerFactory,
                                                             CacheManager caches) {
        return new EntityCommitCacheEviction(entityManagerFactory, caches, "com.asrevo.cvhome.catalog.entity",
                CachedStorefrontCatalog.CACHES);
    }
}
