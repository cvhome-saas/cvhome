package com.asrevo.cvhome.cache.spring;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.cache.CacheKey;
import com.asrevo.cvhome.cache.CacheRegion;
import com.asrevo.cvhome.cache.CacheRegistry;
import com.asrevo.cvhome.cache.PageableKeys;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Builds the {@link CacheKey} of a {@code @Cacheable} method from its typed parameters: the {@link StoreMerchantId}
 * wherever it stands, the {@link LanguageCode} likewise, then the rest in order as parts.
 *
 * <p>
 * The generator is what keeps a cached read honest. A method of a store-scoped region without a store argument,
 * or with an argument that cannot be a key part (a raw id, a shopper's id, a mutable criteria object), fails at the
 * first call with the method and the type named, so a read is never cached under a key that would hand one
 * tenant's answer to another. A method of a global region takes no store and is keyed by the sentinel; an optional
 * argument left null keys as {@link CacheKey#ABSENT}.
 * </p>
 */
public final class StoreScopedKeyGenerator implements KeyGenerator {

    public static final String BEAN = "storeScopedKeys";

    private static final Set<String> SHOPPER_TYPES = Set.of("ShopperId", "CustomerId", "Principal", "Authentication");

    private final CacheRegistry registry;

    public StoreScopedKeyGenerator(CacheRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Object generate(Object target, Method method, Object... params) {
        boolean global = isGlobal(method);
        StoreMerchantId store = null;
        LanguageCode language = null;
        List<Object> parts = new ArrayList<>();
        for (Object param : params) {
            if (param instanceof StoreMerchantId found && store == null) {
                store = found;
            } else if (param instanceof LanguageCode found && language == null) {
                language = found;
            } else {
                parts.add(partOf(method, param));
            }
        }
        if (global) {
            return new CacheKey(CacheKey.GLOBAL, language, parts);
        }
        if (store == null) {
            throw new IllegalArgumentException(String.format(
                    "%s.%s is cached by store but takes no StoreMerchantId", method.getDeclaringClass().getSimpleName(),
                    method.getName()));
        }
        try {
            return new CacheKey(store, language, parts);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("%s.%s: %s", method.getDeclaringClass().getSimpleName(),
                    method.getName(), e.getMessage()), e);
        }
    }

    /** A page reads normalised, an absent optional argument as {@link CacheKey#ABSENT}, the rest as itself. */
    private static Object partOf(Method method, Object param) {
        if (param instanceof Pageable pageable) {
            return PageableKeys.of(pageable);
        }
        if (param == null) {
            return CacheKey.ABSENT;
        }
        refuseShopper(method, param);
        return param;
    }

    private boolean isGlobal(Method method) {
        for (String name : cacheNames(method)) {
            CacheRegion region = registry.declaration(name).orElse(null);
            if (region != null && region.scope() == CacheRegion.Scope.GLOBAL) {
                return true;
            }
        }
        return false;
    }

    private static String[] cacheNames(Method method) {
        Cacheable cacheable = AnnotatedElementUtils.findMergedAnnotation(method, Cacheable.class);
        if (cacheable != null && cacheable.cacheNames().length > 0) {
            return cacheable.cacheNames();
        }
        CacheConfig config = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), CacheConfig.class);
        return config == null ? new String[0] : config.cacheNames();
    }

    private static void refuseShopper(Method method, Object param) {
        if (param != null && SHOPPER_TYPES.contains(param.getClass().getSimpleName())) {
            throw new IllegalArgumentException(String.format(
                    "%s.%s takes a %s: a read that depends on who asks is never cached",
                    method.getDeclaringClass().getSimpleName(), method.getName(), param.getClass().getSimpleName()));
        }
    }
}
