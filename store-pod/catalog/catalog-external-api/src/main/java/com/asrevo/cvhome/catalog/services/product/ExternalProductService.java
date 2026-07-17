package com.asrevo.cvhome.catalog.services.product;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import com.asrevo.cvhome.catalog.model.product.ProductDetails;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

@HttpExchange("/api/v1")
public interface ExternalProductService {

    @GetExchange("/detailed-product")
    ProductDetails getDetailedProduct(StoreMerchantId store, @RequestParam("sku") String sku, LanguageCode lang);

}
