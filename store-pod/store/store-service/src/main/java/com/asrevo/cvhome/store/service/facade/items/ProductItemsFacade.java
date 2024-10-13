package com.asrevo.cvhome.store.service.facade.items;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductList;
import com.asrevo.cvhome.store.core.model.catalog.product.group.ProductGroup;
import java.util.List;

public interface ProductItemsFacade {

    /**
     * List items attached to a Manufacturer
     *
     */
    ReadableProductList listItemsByManufacturer(
            MerchantStore store,
            Language language,
            Long manufacturerId,
            int startCount,
            int maxCount)
            throws Exception;

    ProductGroup createProductGroup(ProductGroup group, MerchantStore store);

    List<ProductGroup> listProductGroups(MerchantStore store, Language language);

    ProductGroup getProductGroup(MerchantStore store, String code);

    /**
     * Update product group visible flag
     *
     */
    void updateProductGroup(String code, ProductGroup group, MerchantStore store);

    /**
     * List product items by id
     *
     */
    ReadableProductList listItemsByIds(
            MerchantStore store, Language language, List<Long> ids, int startCount, int maxCount)
            throws Exception;

    /**
     * List products created in a group, for instance FEATURED group
     *
     */
    ReadableProductList listItemsByGroup(String group, MerchantStore store, Language language)
            throws Exception;

    /**
     * Add product to a group
     *
     */
    ReadableProductList addItemToGroup(
            Product product, String group, MerchantStore store, Language language);

    /**
     * Removes a product from a group
     *
     */
    ReadableProductList removeItemFromGroup(
            Product product, String group, MerchantStore store, Language language) throws Exception;

    void deleteGroup(String group, MerchantStore store);
}
