package com.asrevo.cvhome.gateway.cache;

import java.util.function.Function;

public interface Cache<K, V> {
	V get(K k);

	V get(K k, Function<K, V> loader);
}
