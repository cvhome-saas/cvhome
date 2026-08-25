package com.asrevo.cvhome.checkout.service.facade.product;

import com.asrevo.cvhome.checkout.model.product.ProductDetails;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Composes what used to be catalog's one detailed-product answer out of the two services that own the halves since
 * the split: catalog (product data) and inventory (price and stock).
 */
public interface ProductDetailsComposer {

    ProductDetails getDetailedProduct(StoreMerchantId store, String sku, LanguageCode language);

}
