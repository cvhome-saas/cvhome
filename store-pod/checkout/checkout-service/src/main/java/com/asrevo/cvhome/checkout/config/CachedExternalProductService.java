package com.asrevo.cvhome.checkout.config;

import org.springframework.cache.annotation.Cacheable;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public class CachedExternalProductService implements ExternalProductService {

    private final ExternalProductService externalProductService;

    public CachedExternalProductService(ExternalProductService externalProductService) {
        this.externalProductService = externalProductService;
    }

    @Cacheable(value = "MINIMAL-DETAILED-PRODUCT", key = "#store.storeMerchantId()+'-'+#sku+'-'+#lang.code()",
            unless = "#result==null")
    @Override
    public ReadableMinimalProduct getDetailedProduct(StoreMerchantId store, String sku, LanguageCode lang) {
        return externalProductService.getDetailedProduct(store, sku, lang);
    }

}
