package com.asrevo.cvhome.content.config;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.cache.EntityCommitCacheEviction;
import com.asrevo.cvhome.cache.StoreScopedKeyGenerator;
import com.asrevo.cvhome.content.entity.ContentEntityStore;
import com.asrevo.cvhome.content.facade.CachedStorefront;
import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * The storefront's read caches ({@link CachedStorefront}), each held for ten seconds, bounded, and counted like every
 * other Spring cache. {@code spring.cache.cache-names} fixes the manager's caches, so these are registered by name.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    private static final long STOREFRONT_ENTRIES = 10_000;

    @Bean
    CacheManagerCustomizer<CaffeineCacheManager> storefrontCaches() {
        return manager -> CachedStorefront.CACHES.forEach(name -> manager.registerCustomCache(name,
                Caffeine.newBuilder().expireAfterWrite(CachedStorefront.TTL).maximumSize(STOREFRONT_ENTRIES)
                        .recordStats().build()));
    }

    /** Every storefront read is keyed by its store first, so a store's write can drop its entries alone. */
    @Bean(StoreScopedKeyGenerator.BEAN)
    StoreScopedKeyGenerator storeScopedKeyGenerator() {
        return new StoreScopedKeyGenerator();
    }

    /**
     * An editor's change drops their store's storefront caches when it commits. Content has no change events, and the writes
     * the storefront reads — pages, policies, menus, layouts, banners, site settings — are spread over a dozen services.
     */
    @Bean
    EntityCommitCacheEviction storefrontCacheEviction(EntityManagerFactory entityManagerFactory, CacheManager caches) {
        return new EntityCommitCacheEviction(entityManagerFactory, caches, "com.asrevo.cvhome.content.entity",
                CachedStorefront.CACHES, ContentEntityStore::of);
    }
}
