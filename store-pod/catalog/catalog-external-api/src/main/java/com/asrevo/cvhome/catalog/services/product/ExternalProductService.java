package com.asrevo.cvhome.catalog.services.product;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Read contract for the catalog service: product data for a sku.
 *
 * <p>
 * Since the catalog/inventory split this carries <em>only</em> product data — price and availability come from the
 * inventory service's {@code ExternalInventoryService}, and a caller needing all three composes them (checkout's
 * {@code ProductDetailsComposer}).
 * </p>
 */
@HttpExchange("/api/v1")
public interface ExternalProductService {

    @GetExchange("/detailed-product")
    ReadableMinimalProduct getDetailedProduct(StoreMerchantId store, @RequestParam("sku") String sku,
                                              LanguageCode lang);

}
