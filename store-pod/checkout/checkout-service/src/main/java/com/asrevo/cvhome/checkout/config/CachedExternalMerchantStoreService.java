package com.asrevo.cvhome.checkout.config;

import org.springframework.cache.annotation.Cacheable;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

/**
 * A store's currency and login rule change rarely and are read on every cart call; cached per store.
 */
public class CachedExternalMerchantStoreService implements ExternalMerchantStoreService {

    private final ExternalMerchantStoreService delegate;

    public CachedExternalMerchantStoreService(ExternalMerchantStoreService delegate) {
        this.delegate = delegate;
    }

    // Keyed by the argument itself (a record) rather than a SpEL key: the catalog's product mapper asks once per product
    // mapped, 24 times for one product group in the 2026-09-14 load test, and a SpEL key is evaluated on every call.
    @Cacheable(value = "STORE", unless = "#result==null")
    @Override
    public ReadableMerchantStore getStore(StoreMerchantId store) {
        return delegate.getStore(store);
    }
}
