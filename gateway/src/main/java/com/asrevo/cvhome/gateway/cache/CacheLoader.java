package com.asrevo.cvhome.gateway.cache;

public interface CacheLoader<K, V> {
	V load(K k);
}
