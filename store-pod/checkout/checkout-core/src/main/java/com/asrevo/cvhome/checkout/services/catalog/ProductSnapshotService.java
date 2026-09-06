package com.asrevo.cvhome.checkout.services.catalog;

import java.util.Collection;
import java.util.Map;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Reads what a cart or an order line needs about its skus, live, from catalog (name, image, variant labels) and
 * inventory (price, purchasability). A sku missing from either is absent from the answer — that is what "not
 * purchasable" means here.
 */
public interface ProductSnapshotService {

    Map<String, ProductSnapshot> snapshot(StoreMerchantId store, LanguageCode language, Collection<String> skus);
}
