package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public interface ProductFacade {

    /**
     *
     */
    Product getProduct(Long id, StoreMerchantId store);

    /**
     * Get a Product by friendlyUrl (slug), store and language
     */
    ReadableProduct getProductBySeUrl(
            StoreMerchantId store, String friendlyUrl, LanguageCode language) throws Exception;

    /**
     * Filters a list of product based on criteria
     */
    ReadableProductList getProductListsByCriteria(StoreMerchantId store, ProductCriteria criteria);

    ReadableProductList getTinyProductListsByCriteria(
            StoreMerchantId merchantStore, ProductCriteria searchCriteria);

    ReadableProductList getBaseProductListsByCriteria(
            StoreMerchantId merchantStore, ProductCriteria searchCriteria);
}
