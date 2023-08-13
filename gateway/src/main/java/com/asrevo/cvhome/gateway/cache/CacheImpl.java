package com.asrevo.cvhome.gateway.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.function.Function;

public class CacheImpl<K, V> implements Cache<K, V> {
	private final CacheLoader<K, V> loader;
	private final Expiry<K, V> expiry;
	private final LoadingCache<K, V> cache;


	public CacheImpl(CacheLoader<K, V> loader, Expiry<K, V> expiry) {
		this.loader = loader;
		this.expiry = expiry;
		this.cache = Caffeine.newBuilder().expireAfter(this.expiry)
				.build(this.loader::load);
	}

	@Override
	public V get(K k) {
		return this.cache.get(k);
	}

	@Override
	public V get(K k, Function<K, V> loader) {
		return this.cache.get(k, loader);
	}
}
