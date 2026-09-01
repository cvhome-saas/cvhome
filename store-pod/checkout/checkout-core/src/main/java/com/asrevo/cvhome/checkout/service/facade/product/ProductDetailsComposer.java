package com.asrevo.cvhome.checkout.service.facade.product;

import java.util.Collection;
import java.util.Map;

import com.asrevo.cvhome.checkout.model.product.ProductDetails;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Composes what used to be catalog's one detailed-product answer out of the two services that own the halves since
 * the split: catalog (product data) and inventory (price and stock).
 *
 * <p>
 * The bulk form is the one the cart and order paths use — one catalog call plus one inventory call for the whole
 * sku set, never one pair per line. The single form remains for the one-sku moments (add to cart).
 * </p>
 */
public interface ProductDetailsComposer {

    /**
     * One sku. Throws the catalog's not-found through when the sku is unknown — an add-to-cart of a sku that
     * does not exist is an error, not an empty answer.
     */
    ProductDetails getDetailedProduct(StoreMerchantId store, String sku, LanguageCode language);

    /**
     * A whole sku set in two s2s calls. Skus the catalog no longer knows are absent from the map — a cart line
     * whose product is gone must degrade, not fail the cart; skus without an inventory row come back as
     * not stocked.
     */
    Map<String, ProductDetails> getDetailedProducts(StoreMerchantId store, Collection<String> skus,
                                                    LanguageCode language);
}
