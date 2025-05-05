package com.asrevo.cvhome.catalog.service.facade.items;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.catalog.model.product.group.ProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroupList;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public interface ProductItemsFacade {

    ProductGroup createProductGroup(ProductGroup group, StoreMerchantId store);

    ReadableProductGroupList listProductGroups(StoreMerchantId store, LanguageCode language);

    ProductGroup getProductGroup(StoreMerchantId store, String code);

    /**
     * Update product group visible flag
     */
    void updateProductGroup(String code, ProductGroup group, StoreMerchantId store);

    /**
     * List products created in a group, for instance FEATURED group
     */
    ReadableProductList listTinyProductsGroup(
            String group, StoreMerchantId store, LanguageCode language);

    ReadableProductList listMinimalProductsGroup(
            String group, StoreMerchantId store, LanguageCode language);

    /**
     * Add product to a group
     */
    void addItemToGroup(
            Product product, String group, StoreMerchantId store, LanguageCode language);

    /**
     * Removes a product from a group
     */
    void removeItemFromGroup(
            Product product, String group, StoreMerchantId store, LanguageCode language);

    void deleteGroup(String group, StoreMerchantId store);

    ReadableProductList relatedTinyProducts(
            Product product, StoreMerchantId merchantStore, LanguageCode language);

    ReadableProductList relatedMinimalProducts(
            Product product, StoreMerchantId merchantStore, LanguageCode language);

    void addItemToRelatedProduct(
            Product product, Product related, StoreMerchantId store, LanguageCode language);

    void removeItemFromRelated(
            Product product, Product related, StoreMerchantId store, LanguageCode language);
}
