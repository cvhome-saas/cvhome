package com.asrevo.cvhome.store.service.facade.product;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.inventory.PersistableInventory;
import com.asrevo.cvhome.store.core.model.catalog.product.inventory.ReadableInventory;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

public interface ProductInventoryFacade {

    ReadableInventory get(Long inventoryId, MerchantStore store, Language language);

    ReadableEntityList<ReadableInventory> get(String sku, MerchantStore store, Language language, int page, int count);

    ReadableInventory add(PersistableInventory inventory, MerchantStore store, Language language);

    void update(PersistableInventory inventory, MerchantStore store, Language language);

    void delete(Long productId, Long inventoryId, MerchantStore store);

    ReadableEntityList<ReadableInventory> get(Long productId, MerchantStore store, Language language, int page, int count);


}
