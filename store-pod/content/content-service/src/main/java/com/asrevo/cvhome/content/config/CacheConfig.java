package com.asrevo.cvhome.content.config;

import org.springframework.boot.cache.autoconfigure.CacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
