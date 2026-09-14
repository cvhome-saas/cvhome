package com.asrevo.cvhome.cache;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.interceptor.KeyGenerator;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Keys a {@code @Cacheable} read by its {@link StoreMerchantId} argument first and everything else after, as a
 * {@link StoreScopedKey}. A read with no store argument has no tenant to scope by and is refused: every storefront
 * read takes one, and a cache that did not would serve one store's answer to another.
 */
public class StoreScopedKeyGenerator implements KeyGenerator {

    /** The bean name a {@code @CacheConfig(keyGenerator = …)} names. */
    public static final String BEAN = "storeScopedKeyGenerator";

    @Override
    public Object generate(Object target, Method method, Object... params) {
        StoreMerchantId store = null;
        List<Object> rest = new ArrayList<>(params.length);
        for (Object param : params) {
            if (store == null && param instanceof StoreMerchantId id) {
                store = id;
            } else {
                rest.add(param);
            }
        }
        if (store == null) {
            throw new IllegalArgumentException(String.format("%s.%s takes no StoreMerchantId to key its cache by",
                    method.getDeclaringClass().getSimpleName(), method.getName()));
        }
        return new StoreScopedKey(store, rest);
    }
}
