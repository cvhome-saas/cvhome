package com.asrevo.cvhome.testsupport.arch.fixtures;

import org.springframework.cache.CacheManager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

/** A service building its own Caffeine cache and reaching for the CacheManager: refused. */
public class OwnCache {

    private final Cache<String, String> cache = Caffeine.newBuilder().build();

    private final CacheManager manager;

    public OwnCache(CacheManager manager) {
        this.manager = manager;
    }

    public String read(String key) {
        return cache.getIfPresent(key) == null ? manager.getCacheNames().toString() : key;
    }
}
