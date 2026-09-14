package com.asrevo.cvhome.catalog.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import com.asrevo.cvhome.cache.StoreScopedKey;
import com.asrevo.cvhome.catalog.model.product.ReadableCartLineProduct;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.github.benmanes.caffeine.cache.Cache;

import lombok.RequiredArgsConstructor;

/**
 * Checkout's cart-line read, held one sku at a time.
 *
 * <p>
 * A cart's skus are asked for together, but every subset of them is a different call, so a whole-call cache would
 * hit only on the very same cart. Each sku is its own entry, keyed by store, sku and language, and one call is
 * answered from whatever entries exist plus one read for the skus that have none. A sku the catalogue does not know
 * is answered absent and not cached: the next call asks again, and a product created a moment later is seen at once.
 * The entries live in the {@link CachedStorefrontCatalog#CART_LINE} cache, so a merchant's write drops their store's
 * keys with every other storefront read's.
 * </p>
 */
@Component
@RequiredArgsConstructor
public class CachedCartLines {

    private final CacheManager caches;

    private final ProductService products;

    public List<ReadableCartLineProduct> cartLines(StoreMerchantId store, List<Sku> skus, LanguageCode language) {
        if (skus == null || skus.isEmpty()) {
            return List.of();
        }
        List<Sku> distinct = skus.stream().distinct().toList();
        Map<StoreScopedKey, ReadableCartLineProduct> found = cache().getAll(
                distinct.stream().map(sku -> key(store, sku, language)).toList(),
                missing -> load(store, missing, language));
        List<ReadableCartLineProduct> lines = new ArrayList<>(distinct.size());
        for (Sku sku : distinct) {
            ReadableCartLineProduct line = found.get(key(store, sku, language));
            if (line != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    private Map<StoreScopedKey, ReadableCartLineProduct> load(StoreMerchantId store, Set<? extends StoreScopedKey> keys,
                                                              LanguageCode language) {
        List<Sku> skus = keys.stream().map(key -> (Sku) key.arguments().get(0)).toList();
        Map<StoreScopedKey, ReadableCartLineProduct> loaded = new LinkedHashMap<>();
        for (ReadableCartLineProduct line : products.getCartLines(store, skus, language)) {
            loaded.put(key(store, line.getSku(), language), line);
        }
        return loaded;
    }

    private static StoreScopedKey key(StoreMerchantId store, Sku sku, LanguageCode language) {
        List<Object> arguments = new ArrayList<>(2);
        arguments.add(sku);
        arguments.add(language);
        return new StoreScopedKey(store, arguments);
    }

    @SuppressWarnings("unchecked")
    private Cache<StoreScopedKey, ReadableCartLineProduct> cache() {
        org.springframework.cache.Cache cache = Objects.requireNonNull(
                caches.getCache(CachedStorefrontCatalog.CART_LINE), "the cart-line cache is registered by CacheConfig");
        return (Cache<StoreScopedKey, ReadableCartLineProduct>) cache.getNativeCache();
    }
}
