package com.asrevo.cvhome.payment.config;

import org.springframework.cache.annotation.Cacheable;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;

public class CachedExternalMerchantStoreService implements ExternalMerchantStoreService {

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public CachedExternalMerchantStoreService(ExternalMerchantStoreService externalMerchantStoreService) {
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    // Keyed by the argument itself (a record) rather than a SpEL key: the catalog's product mapper asks once per product
    // mapped, 24 times for one product group in the 2026-09-14 load test, and a SpEL key is evaluated on every call.
    @Cacheable(value = "STORE", unless = "#result==null")
    @Override
    public ReadableMerchantStore getStore(StoreMerchantId store) {
        return externalMerchantStoreService.getStore(store);
    }

}
