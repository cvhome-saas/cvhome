package com.asrevo.cvhome.inventory.services;

import java.util.Collection;
import java.util.List;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.PersistableInventory;
import com.asrevo.cvhome.inventory.model.PersistableSkuInventory;
import com.asrevo.cvhome.inventory.model.SkuInventory;

/**
 * Stock and price per sku. Reads answer only the skus that have a record; a merchant's write path is the
 * sku-addressed {@link #upsert} — one sku at a time or a whole variant matrix at once via {@link #bulkUpsert}.
 */
public interface InventoryService {

    List<SkuInventory> getBySkus(StoreMerchantId store, Collection<String> skus);

    /**
     * Every sku of the given products, in one query. The console's list totals a product's variants from this
     * rather than reporting the default variant's stock as though it were the product's.
     */
    List<SkuInventory> getByProductIds(StoreMerchantId store, Collection<Long> productIds);

    SkuInventory upsert(StoreMerchantId store, String sku, PersistableInventory inventory);

    /**
     * Upserts every entry in one transaction — the console saving a variant matrix. Answers in request order.
     */
    List<SkuInventory> bulkUpsert(StoreMerchantId store, List<PersistableSkuInventory> entries);

    /**
     * Orphan cleanup after a catalog product delete. Deleting for a product with no rows is a no-op.
     */
    void deleteByProduct(StoreMerchantId store, Long productId);

    /**
     * Cleanup for a single retired sku — a variant combination that was removed. No rows is a no-op.
     */
    void deleteBySku(StoreMerchantId store, String sku);
}
