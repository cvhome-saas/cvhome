package com.asrevo.cvhome.order.config;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import org.springframework.cache.annotation.Cacheable;

public class CachedExternalMerchantStoreService implements ExternalMerchantStoreService {
    private final ExternalMerchantStoreService externalMerchantStoreService;

    public CachedExternalMerchantStoreService(ExternalMerchantStoreService externalMerchantStoreService) {
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    @Cacheable(value = "STORE",key = "#store.storeMerchantId()", unless = "#result==null")
    @Override
    public ReadableMerchantStore getStore(StoreMerchantId store) {
        System.out.println(store);
        return externalMerchantStoreService.getStore(store);
    }

}
