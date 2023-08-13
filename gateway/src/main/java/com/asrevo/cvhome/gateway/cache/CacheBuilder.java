package com.asrevo.cvhome.gateway.cache;

import com.github.benmanes.caffeine.cache.Expiry;

public class CacheBuilder<K, V> {
    private CacheLoader<K, V> loader;
    private Expiry<K, V> expiry;

    public CacheBuilder() {
    }

    public CacheBuilder<K, V> loader(CacheLoader<K, V> loader) {
        this.loader = loader;
        return this;
    }

    public CacheBuilder<K, V> expiry(Expiry<K, V> expiry) {
        this.expiry = expiry;
        return this;
    }

    public Cache<K, V> build() {
        return new CacheImpl<>(this.loader, this.expiry);
    }
}
