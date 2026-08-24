package com.asrevo.cvhome.inventory.services;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.availability.SkuInventory;

/**
 * Read contract for the inventory service: stock and price for a set of skus in one call.
 *
 * <p>
 * Skus with no inventory record are absent from the answer rather than an error, so a caller composing product data
 * with stock must treat "missing" as "not stocked".
 * </p>
 */
@HttpExchange("/api/v1")
public interface ExternalInventoryService {

    @GetExchange("/availability")
    List<SkuInventory> getBySkus(StoreMerchantId store, @RequestParam("skus") List<String> skus, LanguageCode lang);

}
