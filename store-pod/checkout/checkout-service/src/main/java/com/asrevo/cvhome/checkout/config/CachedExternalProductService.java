package com.asrevo.cvhome.checkout.config;

import org.springframework.cache.annotation.Cacheable;

import com.asrevo.cvhome.catalog.model.product.ProductDetails;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public class CachedExternalProductService implements ExternalProductService {

    private final ExternalProductService externalProductService;

    public CachedExternalProductService(ExternalProductService externalProductService) {
        this.externalProductService = externalProductService;
    }

    @Cacheable(value = "MINIMAL-DETAILED-PRODUCT", key = "#store.storeMerchantId()+'-'+#sku+'-'+#lang.code()",
            unless = "#result==null")
    @Override
    public ProductDetails getDetailedProduct(StoreMerchantId store, String sku, LanguageCode lang) {
        return externalProductService.getDetailedProduct(store, sku, lang);
    }

}
