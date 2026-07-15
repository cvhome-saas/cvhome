package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.model.product.LightPersistableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.product.PersistableProduct;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public interface ProductCommonFacade {

    /**
     * Create / Update product
     */
    Long saveProduct(StoreMerchantId store, PersistableProduct product, LanguageCode language);

    /**
     * Update minimal product details
     */
    void update(Long productId, LightPersistableProduct product, StoreMerchantId merchant, LanguageCode language);

    /**
     * Patch inventory by sku
     */
    void update(String sku, LightPersistableProduct product, StoreMerchantId merchant, LanguageCode language);

    /**
     * Get a Product by id and store
     */
    ReadableProduct getProduct(StoreMerchantId store, Long id, LanguageCode language);

    /**
     * Delete product
     */
    void deleteProduct(Long id, StoreMerchantId store);

    /**
     * Adds a product to a category
     */
    ReadableProduct addProductToCategory(Category category, Product product, LanguageCode language);

    /**
     * Removes item from a category
     */
    ReadableProduct removeProductFromCategory(Category category, Product product, LanguageCode language)
            throws Exception;

    /**
     * validates if product exists
     */
    boolean exists(String sku, StoreMerchantId store);

}
