package com.asrevo.cvhome.inventory.service.facade;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.inventory.errors.InventoryNotFoundException;
import com.asrevo.cvhome.inventory.errors.InventoryReferenceUnresolvableException;
import com.asrevo.cvhome.inventory.model.inventory.PersistableInventory;
import com.asrevo.cvhome.inventory.model.inventory.ReadableInventory;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

public interface ProductInventoryFacade {

    ReadableInventory get(Long inventoryId, StoreMerchantId store, LanguageCode language)
            throws InventoryNotFoundException, InventoryNotConvertibleException;

    ReadableEntityList<ReadableInventory> get(String sku, StoreMerchantId store, LanguageCode language,
                                              Pageable pageable)
            throws InventoryNotConvertibleException;

    ReadableInventory add(PersistableInventory inventory, StoreMerchantId store, LanguageCode language)
            throws InventoryNotConvertibleException, InventoryReferenceUnresolvableException;

    void update(PersistableInventory inventory, StoreMerchantId store, LanguageCode language)
            throws InventoryNotFoundException, InventoryNotConvertibleException,
            InventoryReferenceUnresolvableException;

    void delete(Long productId, Long inventoryId, StoreMerchantId store)
            throws InventoryNotFoundException;

    void deleteByProduct(Long productId, StoreMerchantId store);

    ReadableEntityList<ReadableInventory> get(Long productId, StoreMerchantId store, LanguageCode language,
                                              Pageable pageable)
            throws InventoryNotConvertibleException;

}
