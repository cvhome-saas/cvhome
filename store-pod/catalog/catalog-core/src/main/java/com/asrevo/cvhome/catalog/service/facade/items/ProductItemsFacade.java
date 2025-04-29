package com.asrevo.cvhome.catalog.service.facade.items;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.catalog.model.product.group.ProductGroup;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import java.util.List;

public interface ProductItemsFacade {

    ProductGroup createProductGroup(ProductGroup group, StoreMerchantId store);

    List<ProductGroup> listProductGroups(StoreMerchantId store, LanguageCode language);

    ProductGroup getProductGroup(StoreMerchantId store, String code);

    /**
     * Update product group visible flag
     */
    void updateProductGroup(String code, ProductGroup group, StoreMerchantId store);

    /**
     * List products created in a group, for instance FEATURED group
     */
    ReadableProductList listItemsByGroup(String group, StoreMerchantId store, LanguageCode language)
            throws Exception;

    /**
     * Add product to a group
     */
    ReadableProductList addItemToGroup(
            Product product, String group, StoreMerchantId store, LanguageCode language);

    /**
     * Removes a product from a group
     */
    ReadableProductList removeItemFromGroup(
            Product product, String group, StoreMerchantId store, LanguageCode language)
            throws Exception;

    void deleteGroup(String group, StoreMerchantId store);

    ReadableProductList relatedItems(
            Product product, StoreMerchantId merchantStore, LanguageCode language);

    ReadableProductList addItemToRelatedProduct(
            Product product, Product related, StoreMerchantId store, LanguageCode language);

    ReadableProductList removeItemFromRelated(
            Product product, Product related, StoreMerchantId store, LanguageCode language)
            throws ServiceException;
}
