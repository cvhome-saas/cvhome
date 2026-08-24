package com.asrevo.cvhome.inventory.service.facade;

import java.util.List;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.availability.SkuInventory;

/**
 * The read side other services and the frontends live on: stock and price for a set of skus in one call.
 */
public interface SkuInventoryFacade {

    /**
     * Answers one {@link SkuInventory} per sku that has inventory in the store; skus without a record are simply
     * absent from the result, never an error — a listing must not fail because one product is not stocked yet.
     */
    List<SkuInventory> getBySkus(List<String> skus, StoreMerchantId store, LanguageCode language);

}
