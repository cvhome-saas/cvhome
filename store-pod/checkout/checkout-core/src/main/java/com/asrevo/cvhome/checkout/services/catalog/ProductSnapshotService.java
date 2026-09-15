package com.asrevo.cvhome.checkout.services.catalog;

import java.util.Collection;
import java.util.Map;

import com.asrevo.cvhome.checkout.entity.CartLine;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Reads what a cart or an order line needs about its skus: from catalog (name, slug, image, variant labels) and
 * inventory (price, purchasability). A sku missing from either is absent from the answer — that is what "not
 * purchasable" means here.
 */
public interface ProductSnapshotService {

    /** Both sources, live: for a sku being added to a cart, and for every line of an order being placed. */
    Map<Sku, ProductSnapshot> snapshot(StoreMerchantId store, LanguageCode language, Collection<Sku> skus);

    /**
     * A cart's lines priced for a read: inventory through the per-sku cache ({@link CachedSkuInventory}, a few
     * seconds old at most), the catalogue only for a line that carries no snapshot it may still trust
     * ({@link CartLine#remembers}). The lines it had to ask the catalogue about are refreshed in place, so the
     * caller's write keeps the answer.
     */
    Map<Sku, ProductSnapshot> priced(StoreMerchantId store, LanguageCode language, Collection<CartLine> lines);

    /**
     * Drops what the per-sku cache holds for these skus, for the caller that just changed their stock: an order
     * placed on this task, whose next cart read must not show the units it took.
     */
    void forget(StoreMerchantId store, Collection<Sku> skus);
}
