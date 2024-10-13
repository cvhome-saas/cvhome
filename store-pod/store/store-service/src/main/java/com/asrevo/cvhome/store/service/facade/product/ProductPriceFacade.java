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
     * Creates a product price
     *
     */
    Long save(PersistableProductPrice price, MerchantStore store);

    /**
     * Product price deletion
     *
     */
    void delete(Long priceId, String sku, MerchantStore store);

    /**
     * List product prices by product and inventory (product and variants)
     *
     */
    List<ReadableProductPrice> list(
            String sku, Long inventoryId, MerchantStore store, Language language);

    /**
     * List product prices by product
     *
     */
    List<ReadableProductPrice> list(String sku, MerchantStore store, Language language);

    /**
     * Get ProductPrice
     *
     */
    ReadableProductPrice get(
            String sku, Long productPriceId, MerchantStore store, Language language);
}
