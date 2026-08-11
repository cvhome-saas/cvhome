package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.billing.commons.errors.EntitlementExceededException;
import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.errors.CategoryAlreadyAttachedException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ManufacturerReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotPersistedException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductTypeReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductVariantSkuConflictException;
import com.asrevo.cvhome.catalog.errors.ProductVariationReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.LightPersistableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.product.PersistableProduct;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface ProductCommonFacade {

    /**
     * Create / Update product
     */
    /**
     * @throws EntitlementExceededException creating this product would take the store past the product ceiling its
     *                                      plan grants. Editing an existing one never can, so this only applies to a
     *                                      new product.
     */
    Long saveProduct(StoreMerchantId store, PersistableProduct product, LanguageCode language)
            throws ProductNotConvertibleException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException,
            ProductVariationReferenceUnresolvableException, ProductReferenceUnresolvableException,
            ProductVariantSkuConflictException, ProductPriceNotConvertibleException,
            InventoryNotConvertibleException, ProductNotPersistedException, EntitlementExceededException;

    /**
     * Update minimal product details
     */
    void update(Long productId, LightPersistableProduct product, StoreMerchantId merchant, LanguageCode language);

    /**
     * Patch inventory by sku
     */
    void update(String sku, LightPersistableProduct product, StoreMerchantId merchant, LanguageCode language)
            throws ProductNotFoundException, ProductNotPersistedException;

    /**
     * Get a Product by id and store
     */
    ReadableProduct getProduct(StoreMerchantId store, Long id, LanguageCode language)
            throws ProductNotFoundException, ProductNotConvertibleException;

    /**
     * Delete product
     */
    void deleteProduct(Long id, StoreMerchantId store)
            throws ProductNotFoundException;

    /**
     * Adds a product to a category
     */
    ReadableProduct addProductToCategory(Category category, Product product, LanguageCode language)
            throws CategoryAlreadyAttachedException, ProductNotConvertibleException,
            ProductNotPersistedException;

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
