package com.asrevo.cvhome.catalog.services.product;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.billing.commons.errors.EntitlementExceededException;
import com.asrevo.cvhome.catalog.entity.Product;
import com.asrevo.cvhome.catalog.errors.CategoryAlreadyAttachedException;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ManufacturerReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductTypeReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.LightPersistableProduct;
import com.asrevo.cvhome.catalog.model.product.PersistableProductDefinition;
import com.asrevo.cvhome.catalog.model.product.ProductFilter;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductDefinition;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

/**
 * Products: the listing, the product page, the console's definition, and the small writes around them.
 */
public interface ProductService {

    /**
     * The listing. A single category filter widens to that category's subtree.
     */
    ReadableEntityList<ReadableProduct> list(StoreMerchantId store, ProductFilter filter, LanguageCode language,
                                             Pageable pageable);

    /**
     * The storefront's product page, by slug in the shopper's language.
     */
    ReadableProduct getByFriendlyUrl(StoreMerchantId store, String friendlyUrl, LanguageCode language)
            throws ProductNotFoundException;

    /**
     * The product data a cart or order needs, by sku.
     */
    ReadableMinimalProduct getBySku(StoreMerchantId store, String sku, LanguageCode language)
            throws ProductNotFoundException;

    ReadableProductDefinition getDefinition(StoreMerchantId store, Long id, LanguageCode language)
            throws ProductNotFoundException;

    boolean exists(StoreMerchantId store, String sku);

    /**
     * @throws EntitlementExceededException the store's plan caps products and the cap is reached
     */
    Long create(StoreMerchantId store, PersistableProductDefinition product)
            throws ManufacturerReferenceUnresolvableException, ProductTypeReferenceUnresolvableException,
            CategoryReferenceUnresolvableException, EntitlementExceededException;

    void update(StoreMerchantId store, Long id, PersistableProductDefinition product)
            throws ProductNotFoundException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException;

    /**
     * The console's inline edit: the two switches, nothing else.
     */
    void patch(StoreMerchantId store, Long id, LightPersistableProduct product) throws ProductNotFoundException;

    void addToCategory(StoreMerchantId store, Long productId, Long categoryId)
            throws ProductNotFoundException, CategoryNotFoundException, CategoryAlreadyAttachedException;

    void removeFromCategory(StoreMerchantId store, Long productId, Long categoryId)
            throws ProductNotFoundException, CategoryNotFoundException;

    void delete(StoreMerchantId store, Long id) throws ProductNotFoundException;

    /**
     * Deletes a product already in hand, with its image files. For the category cascade.
     */
    void delete(StoreMerchantId store, Product product);
}
