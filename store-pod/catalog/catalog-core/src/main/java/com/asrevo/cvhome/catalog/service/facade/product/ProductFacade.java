package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductVariantParentMissingException;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ProductFacade {

    /**
     *
     */
    Product getProduct(Long id, StoreMerchantId store);

    /**
     * Get a Product by friendlyUrl (slug), store and language
     */
    ReadableProduct getProductBySeUrl(StoreMerchantId store, String friendlyUrl, LanguageCode language)
            throws ProductNotFoundException, ProductNotConvertibleException, ProductPriceNotConvertibleException,
            ProductVariantParentMissingException, InventoryNotConvertibleException;

    /**
     * Filters a list of product based on criteria
     */
    ReadableProductList getProductListsByCriteria(StoreMerchantId store, ProductCriteria criteria)
            throws ProductNotConvertibleException;

    ReadableProductList getBaseProductListsByCriteria(StoreMerchantId merchantStore, ProductCriteria searchCriteria)
            throws ProductNotConvertibleException;

}
