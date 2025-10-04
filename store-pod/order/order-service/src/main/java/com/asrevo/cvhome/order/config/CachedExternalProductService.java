package com.asrevo.cvhome.order.config;

import com.asrevo.cvhome.catalog.model.product.*;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;
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
    public ProductReservationStatus reserve(StoreMerchantId store, ProductReservationList productReservation) throws ServiceException {
        return externalProductService.reserve(store, productReservation);
    }
}
