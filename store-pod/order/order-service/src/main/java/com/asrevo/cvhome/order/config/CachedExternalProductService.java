package com.asrevo.cvhome.order.config;

import com.asrevo.cvhome.catalog.model.product.ProductAvailabilityStatus;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductAvailability;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductQuantityUpdate;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public class CachedExternalProductService implements ExternalProductService {
    private final ExternalProductService externalProductService;

    public CachedExternalProductService(ExternalProductService externalProductService) {
        this.externalProductService = externalProductService;
    }

    @Cacheable(value = "PRODUCT-PRICE", key = "#store.storeMerchantId()+'-'+#sku", unless = "#result==null")
    @Override
    public FinalPrice getProductPrice(StoreMerchantId store, String sku) throws ServiceException {
        return externalProductService.getProductPrice(store, sku);
    }

    @Override
    public List<FinalPrice> getProductsPrice(StoreMerchantId store, List<String> skus) throws ServiceException {
        return externalProductService.getProductsPrice(store, skus);
    }

    @Override
    public ReadableProductAvailability getProductAvailability(StoreMerchantId store, String sku) {
        return externalProductService.getProductAvailability(store, sku);
    }

    @Override
    public List<ReadableProductAvailability> getProductsAvailability(StoreMerchantId store, List<String> sku) {
        return externalProductService.getProductsAvailability(store, sku);
    }

    @Cacheable(value = "FULL-PRODUCT", key = "#store.storeMerchantId()+'-'+#sku+'-'+#lang.code()", unless = "#result==null")
    @Override
    public ReadableProduct getFullProduct(StoreMerchantId store, String sku, LanguageCode lang) {
        return externalProductService.getFullProduct(store, sku, lang);
    }

    @Cacheable(value = "MINIMAL-PRODUCT", key = "#store.storeMerchantId()+'-'+#sku+'-'+#lang.code()", unless = "#result==null")
    @Override
    public ReadableMinimalProduct getMinimalProduct(StoreMerchantId store, String sku, LanguageCode lang) {
        return externalProductService.getMinimalProduct(store, sku, lang);
    }

    @Override
    public List<ReadableMinimalProduct> getMinimalProducts(StoreMerchantId store, List<String> sku, LanguageCode lang) {
        return externalProductService.getMinimalProducts(store, sku, lang);
    }

    @Override
    public ProductAvailabilityStatus productQuantityUpdate(StoreMerchantId store, ProductQuantityUpdate productQuantityUpdate) throws ServiceException {
        return externalProductService.productQuantityUpdate(store, productQuantityUpdate);
    }
}
