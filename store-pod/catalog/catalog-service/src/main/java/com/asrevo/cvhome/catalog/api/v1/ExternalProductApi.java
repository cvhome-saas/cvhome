package com.asrevo.cvhome.catalog.api.v1;

import java.util.List;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.UncheckedBaseException;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * Checkout's service-to-service read: the product behind a cart line, by sku.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Products")
@RequiredArgsConstructor
public class ExternalProductApi implements ExternalProductService {

    private final ProductService productService;

    @Override
    @GetMapping("/detailed-product")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableMinimalProduct getDetailedProduct(StoreMerchantId store, @RequestParam String sku,
                                                     LanguageCode lang) {
        try {
            return productService.getBySku(store, sku, lang);
        } catch (ProductNotFoundException e) {
            // The s2s contract declares nothing checked; the carrier keeps the 404 and its code on the way out.
            throw new UncheckedBaseException(e);
        }
    }

    @Override
    @GetMapping("/detailed-products")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public List<ReadableMinimalProduct> getDetailedProducts(StoreMerchantId store,
                                                            @RequestParam List<String> skus, LanguageCode lang) {
        return productService.getBySkus(store, skus, lang);
    }
}
