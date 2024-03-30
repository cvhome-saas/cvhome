package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.PersistableProductPrice;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductPrice;

import java.util.List;


/**
 * Product price management api
 *
 * @author carlsamson
 */
public interface ProductPriceFacade {

    /**
     * Calculate product price based on specific product options
     *
     * @param id
     * @param priceRequest
     * @param store
     * @param language
     * @return
     */
    /**
     * ReadableProductPrice getProductPrice(Long id, ProductPriceRequest
     * priceRequest, MerchantStore store, Language language);
     **/

    /**
     * Creates a product price
     *
     * @param price
     * @param productId
     * @param inventoryId
     * @param store
     * @return
     */
    Long save(PersistableProductPrice price, MerchantStore store);

    /**
     * Product price deletion
     *
     * @param priceId
     * @param productId
     * @param inventoryId
     * @param store
     */
    void delete(Long priceId, String sku, MerchantStore store);

    /**
     * List product prices by product and inventory (product and variants)
     *
     * @param productId
     * @param inventoryId
     * @param store
     * @return
     */
    List<ReadableProductPrice> list(String sku, Long inventoryId, MerchantStore store, Language language);

    /**
     * List product prices by product
     *
     * @param poductId
     * @param store
     * @return
     */
    List<ReadableProductPrice> list(String sku, MerchantStore store, Language language);

    /**
     * Get ProductPrice
     *
     * @param sku
     * @param productPriceId
     * @param store
     * @param language
     * @return
     */
    ReadableProductPrice get(String sku, Long productPriceId, MerchantStore store, Language language);
}
