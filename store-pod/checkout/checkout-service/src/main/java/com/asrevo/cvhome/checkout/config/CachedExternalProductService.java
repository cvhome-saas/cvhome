package com.asrevo.cvhome.checkout.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Caches the catalog's product reads per sku. The bulk read is cache-aware rather than cached wholesale: hits
 * are served from the same per-sku entries the single read fills, and only the misses travel — so a cart of
 * ten lines costs one catalog call the first time and none the next.
 */
public class CachedExternalProductService implements ExternalProductService {

    private static final String CACHE = "MINIMAL-DETAILED-PRODUCT";

    private final ExternalProductService externalProductService;

    private final CacheManager cacheManager;

    public CachedExternalProductService(ExternalProductService externalProductService, CacheManager cacheManager) {
        this.externalProductService = externalProductService;
        this.cacheManager = cacheManager;
    }

    @Cacheable(value = CACHE, key = "#store.storeMerchantId()+'-'+#sku+'-'+#lang.code()",
            unless = "#result==null")
    @Override
    public ReadableMinimalProduct getDetailedProduct(StoreMerchantId store, String sku, LanguageCode lang) {
        return externalProductService.getDetailedProduct(store, sku, lang);
    }

    @Override
    public List<ReadableMinimalProduct> getDetailedProducts(StoreMerchantId store, List<String> skus,
                                                            LanguageCode lang) {
        Cache cache = cacheManager == null ? null : cacheManager.getCache(CACHE);
        Map<String, ReadableMinimalProduct> bySku = new LinkedHashMap<>();
        List<String> misses = new ArrayList<>();
        for (String sku : new LinkedHashSet<>(skus)) {
            ReadableMinimalProduct cached =
                    cache == null ? null : cache.get(key(store, sku, lang), ReadableMinimalProduct.class);
            if (cached != null) {
                bySku.put(sku, cached);
            } else {
                misses.add(sku);
            }
        }
        if (!misses.isEmpty()) {
            for (ReadableMinimalProduct product : externalProductService.getDetailedProducts(store, misses, lang)) {
                bySku.put(product.getSku(), product);
                if (cache != null) {
                    cache.put(key(store, product.getSku(), lang), product);
                }
            }
        }
        return skus.stream().distinct().map(bySku::get).filter(Objects::nonNull).toList();
    }

    private static String key(StoreMerchantId store, String sku, LanguageCode lang) {
        return "%s-%s-%s".formatted(store.storeMerchantId(), sku, lang.code());
    }
}
