package com.asrevo.cvhome.inventory.services;

import java.util.Collection;
import java.util.List;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.PersistableInventory;
import com.asrevo.cvhome.inventory.model.SkuInventory;

/**
 * Stock and price per sku. Reads answer only the skus that have a record; a merchant's whole write path is the
 * sku-addressed {@link #upsert}.
 */
public interface InventoryService {

    List<SkuInventory> getBySkus(StoreMerchantId store, Collection<String> skus);

    SkuInventory upsert(StoreMerchantId store, String sku, PersistableInventory inventory);

    /**
     * Orphan cleanup after a catalog product delete. Deleting for a product with no rows is a no-op.
     */
    void deleteByProduct(StoreMerchantId store, Long productId);
}
