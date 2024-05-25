package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.ProductCriteria;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductList;

import java.util.List;

public interface ProductFacade {


    /**
     */
    Product getProduct(Long id, MerchantStore store);

    /**
     * Reads a product by code
     *
     */
    ReadableProduct getProductByCode(MerchantStore store, String uniqueCode, Language language);

    /**
     * Get a product by sku and store
     *
     */
    ReadableProduct getProduct(MerchantStore store, String sku, Language language) throws Exception;

    /**
     * Get a Product by friendlyUrl (slug), store and language
     *
     */
    ReadableProduct getProductBySeUrl(MerchantStore store, String friendlyUrl, Language language) throws Exception;

    /**
     * Filters a list of product based on criteria
     *
     */
    ReadableProductList getProductListsByCriterias(MerchantStore store, Language language,
                                                   ProductCriteria criterias) throws Exception;

    /**
     * Get related items
     *
     */
    List<ReadableProduct> relatedItems(MerchantStore store, Product product, Language language)
            throws Exception;


}
