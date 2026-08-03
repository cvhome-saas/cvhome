package com.asrevo.cvhome.catalog.service.facade.product;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.InventoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.InventoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductVariantNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductVariantReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.inventory.PersistableInventory;
import com.asrevo.cvhome.catalog.model.product.inventory.ReadableInventory;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

public interface ProductInventoryFacade {

    ReadableInventory get(Long inventoryId, StoreMerchantId store, LanguageCode language)
            throws InventoryNotFoundException, InventoryNotConvertibleException;

    ReadableEntityList<ReadableInventory> get(String sku, StoreMerchantId store, LanguageCode language,
                                              Pageable pageable)
            throws ProductNotFoundException, InventoryNotConvertibleException;

    ReadableInventory add(PersistableInventory inventory, StoreMerchantId store, LanguageCode language)
            throws InventoryNotConvertibleException, InventoryReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductVariantReferenceUnresolvableException, ServiceException;

    void update(PersistableInventory inventory, StoreMerchantId store, LanguageCode language)
            throws InventoryNotFoundException, InventoryNotConvertibleException,
            InventoryReferenceUnresolvableException, ProductNotFoundException, ProductReferenceUnresolvableException,
            ProductVariantNotFoundException, ProductVariantReferenceUnresolvableException, ServiceException;

    void delete(Long productId, Long inventoryId, StoreMerchantId store)
            throws InventoryNotFoundException, ServiceException;

    ReadableEntityList<ReadableInventory> get(Long productId, StoreMerchantId store, LanguageCode language,
                                              Pageable pageable)
            throws InventoryNotConvertibleException;

}
