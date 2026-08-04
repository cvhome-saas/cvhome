package com.asrevo.cvhome.catalog.service.facade.product;

import java.util.List;

import com.asrevo.cvhome.catalog.errors.InventoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.PersistableProductPrice;
import com.asrevo.cvhome.catalog.model.product.ReadableProductPrice;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Product price management api
 *
 * @author carlsamson
 */
public interface ProductPriceFacade {

    /**
     * Creates a product price
     */
    Long save(PersistableProductPrice price, StoreMerchantId store)
            throws ProductPriceNotConvertibleException, InventoryReferenceUnresolvableException,
            ProductReferenceUnresolvableException;

    /**
     * Product price deletion
     */
    void delete(Long priceId, String sku, StoreMerchantId store)
            throws ProductPriceNotFoundException;

    /**
     * List product prices by product and inventory (product and variants)
     */
    List<ReadableProductPrice> list(String sku, Long inventoryId, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotConvertibleException;

    /**
     * List product prices by product
     */
    List<ReadableProductPrice> list(String sku, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotConvertibleException;

    /**
     * Get ProductPrice
     */
    ReadableProductPrice get(String sku, Long productPriceId, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotFoundException, ProductPriceNotConvertibleException;

}
