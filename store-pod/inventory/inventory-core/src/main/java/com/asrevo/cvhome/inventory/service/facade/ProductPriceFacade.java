package com.asrevo.cvhome.inventory.service.facade;

import java.util.List;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.errors.InventoryReferenceUnresolvableException;
import com.asrevo.cvhome.inventory.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.inventory.errors.ProductPriceNotFoundException;
import com.asrevo.cvhome.inventory.errors.SkuReferenceUnresolvableException;
import com.asrevo.cvhome.inventory.model.price.PersistableProductPrice;
import com.asrevo.cvhome.inventory.model.price.ReadableProductPrice;

/**
 * Product price management api
 */
public interface ProductPriceFacade {

    /**
     * Creates a product price
     */
    Long save(PersistableProductPrice price, StoreMerchantId store)
            throws ProductPriceNotConvertibleException, InventoryReferenceUnresolvableException,
            SkuReferenceUnresolvableException;

    /**
     * Product price deletion
     */
    void delete(Long priceId, String sku, StoreMerchantId store)
            throws ProductPriceNotFoundException;

    /**
     * List product prices by sku and inventory
     */
    List<ReadableProductPrice> list(String sku, Long inventoryId, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotConvertibleException;

    /**
     * List product prices by sku
     */
    List<ReadableProductPrice> list(String sku, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotConvertibleException;

    /**
     * Get ProductPrice
     */
    ReadableProductPrice get(String sku, Long productPriceId, StoreMerchantId store, LanguageCode language)
            throws ProductPriceNotFoundException, ProductPriceNotConvertibleException;

}
