package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.review.ProductReview;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.*;
import com.asrevo.cvhome.store.core.model.catalog.product.product.PersistableProduct;

import java.util.List;

public interface ProductCommonFacade {


    /**
     * Create / Update product
     *
     */
    Long saveProduct(MerchantStore store, PersistableProduct product,
                     Language language);

    /**
     * Update minimal product details
     *
     */
    void update(Long productId, LightPersistableProduct product, MerchantStore merchant, Language language);

    /**
     * Patch inventory by sku
     *
     */
    void update(String sku, LightPersistableProduct product, MerchantStore merchant, Language language);

    /**
     * Get a Product by id and store
     *
     */
    ReadableProduct getProduct(MerchantStore store, Long id, Language language) throws Exception;

    /**
     */
    Product getProduct(Long id, MerchantStore store);

    /**
     * Reads a product by code
     *
     */
    ReadableProduct getProductByCode(MerchantStore store, String uniqueCode, Language language)
            throws Exception;


    /**
     * Sets a new price to an existing product
     *
     */
    ReadableProduct updateProductPrice(ReadableProduct product, ProductPriceEntity price,
                                       Language language) throws Exception;

    /**
     * Sets a new price to an existing product
     *
     */
    ReadableProduct updateProductQuantity(ReadableProduct product, int quantity, Language language)
            throws Exception;

    /**
     * Deletes a product for a given product id
     *
     */
    void deleteProduct(Product product) throws Exception;

    /**
     * Delete product
     *
     */
    void deleteProduct(Long id, MerchantStore store);


    /**
     * Adds a product to a category
     *
     */
    ReadableProduct addProductToCategory(Category category, Product product, Language language);

    /**
     * Removes item from a category
     *
     */
    ReadableProduct removeProductFromCategory(Category category, Product product, Language language)
            throws Exception;


    /**
     * Saves or updates a Product review
     *
     */
    void saveOrUpdateReview(PersistableProductReview review, MerchantStore store, Language language)
            throws Exception;

    /**
     * Deletes a product review
     *
     */
    void deleteReview(ProductReview review, MerchantStore store, Language language) throws Exception;

    /**
     * Get reviews for a given product
     *
     */
    List<ReadableProductReview> getProductReviews(Product product, MerchantStore store,
                                                  Language language) throws Exception;

    /**
     * validates if product exists
     *
     */
    boolean exists(String sku, MerchantStore store);


}
