package com.asrevo.cvhome.order.config;

import com.asrevo.cvhome.catalog.model.product.*;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ReserveProductRequest;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import org.springframework.cache.annotation.Cacheable;


public class CachedExternalProductService implements ExternalProductService {
    private final ExternalProductService externalProductService;

    public CachedExternalProductService(ExternalProductService externalProductService) {
        this.externalProductService = externalProductService;
    }

    @Cacheable(value = "MINIMAL-DETAILED-PRODUCT", key = "#store.storeMerchantId()+'-'+#sku+'-'+#lang.code()", unless = "#result==null")
    @Override
    public ProductDetails getDetailedProduct(StoreMerchantId store, String sku, LanguageCode lang) {
        return externalProductService.getDetailedProduct(store, sku, lang);
    }

    @Override
    public ProductAvailabilityStatus reserveProducts(StoreMerchantId store, ReserveProductRequest reserveProductRequest) throws ServiceException {
        return externalProductService.reserveProducts(store, reserveProductRequest);
    }
}
