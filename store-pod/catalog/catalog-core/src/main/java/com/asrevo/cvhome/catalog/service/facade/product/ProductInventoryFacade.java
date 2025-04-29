package com.asrevo.cvhome.catalog.service.facade.product;

import com.asrevo.cvhome.catalog.model.product.inventory.PersistableInventory;
import com.asrevo.cvhome.catalog.model.product.inventory.ReadableInventory;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

public interface ProductInventoryFacade {

    ReadableInventory get(Long inventoryId, StoreMerchantId store, LanguageCode language);

    ReadableEntityList<ReadableInventory> get(
            String sku, StoreMerchantId store, LanguageCode language, int page, int count);

    ReadableInventory add(
            PersistableInventory inventory, StoreMerchantId store, LanguageCode language);

    void update(PersistableInventory inventory, StoreMerchantId store, LanguageCode language);

    void delete(Long productId, Long inventoryId, StoreMerchantId store);

    ReadableEntityList<ReadableInventory> get(
            Long productId, StoreMerchantId store, LanguageCode language, int page, int count);
}
