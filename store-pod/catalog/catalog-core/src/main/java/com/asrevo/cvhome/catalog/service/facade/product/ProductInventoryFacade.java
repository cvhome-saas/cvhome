package com.asrevo.cvhome.catalog.service.facade.product;

import org.springframework.data.domain.Pageable;

import com.asrevo.cvhome.catalog.model.product.inventory.PersistableInventory;
import com.asrevo.cvhome.catalog.model.product.inventory.ReadableInventory;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public interface ProductInventoryFacade {

    ReadableInventory get(Long inventoryId, StoreMerchantId store, LanguageCode language);

    ReadableEntityList<ReadableInventory> get(String sku, StoreMerchantId store, LanguageCode language,
                                              Pageable pageable);

    ReadableInventory add(PersistableInventory inventory, StoreMerchantId store, LanguageCode language);

    void update(PersistableInventory inventory, StoreMerchantId store, LanguageCode language);

    void delete(Long productId, Long inventoryId, StoreMerchantId store);

    ReadableEntityList<ReadableInventory> get(Long productId, StoreMerchantId store, LanguageCode language,
                                              Pageable pageable);

}
