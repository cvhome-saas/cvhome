package com.asrevo.cvhome.catalog.services.product;

import com.asrevo.cvhome.catalog.model.product.ProductAvailabilityStatus;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductAvailability;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductQuantityUpdate;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange("/api/v1")
public interface ExternalProductService {
    @GetExchange("/product-price")
    FinalPrice getProductPrice(StoreMerchantId merchant, @RequestParam("sku") String sku)
            throws ServiceException;

    @GetExchange("/product-availability")
    ReadableProductAvailability getProductAvailability(
            StoreMerchantId store, @RequestParam("sku") String sku);

    @GetExchange("/full-product")
    ReadableProduct getFullProduct(
            StoreMerchantId store, @RequestParam("sku") String sku, LanguageCode lang);

    @GetExchange("/minimal-product")
    ReadableMinimalProduct getMinimalProduct(
            StoreMerchantId store, @RequestParam("sku") String sku, LanguageCode lang);

    @PostExchange("/product-quantity-update")
    ProductAvailabilityStatus productQuantityUpdate(
            StoreMerchantId store, @RequestBody ProductQuantityUpdate productQuantityUpdate)
            throws ServiceException;
}
