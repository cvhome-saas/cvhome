package com.asrevo.cvhome.catalog.api.v1.product;

import com.asrevo.cvhome.catalog.model.product.ProductAvailabilityStatus;
import com.asrevo.cvhome.catalog.model.product.ReadableMinimalProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductAvailability;
import com.asrevo.cvhome.catalog.model.product.product.price.FinalPrice;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductQuantityUpdate;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v1")
@Tag(name =
        "Product definition resource (Create update and delete product definition. Serves api v1 and v2 with backward compatibility)")
@Slf4j
@AllArgsConstructor
public class ExternalProductApi implements ExternalProductService {
    private final ProductService productService;

    @GetMapping(value = "/product-price")
    @Operation(method = "GET", description = "Get Product Price",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = FinalPrice.class))))

    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "sku", schema = @Schema(name = "sku", type = "string")),
    })
    @ConditionalOnApiStatus
    @Override
    public FinalPrice getProductPrice(StoreMerchantId merchant, @RequestParam String sku) throws ServiceException {
        return productService.getProductPrice(merchant, sku);
    }

    @GetMapping(value = "/products-price")
    @Operation(method = "GET", description = "Get Product Price",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = FinalPrice.class))))

    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "sku", schema = @Schema(name = "sku", type = "string")),
    })
    @ConditionalOnApiStatus
    @Override
    public List<FinalPrice> getProductsPrice(StoreMerchantId merchant, @RequestParam List<String> sku) throws ServiceException {
        List<FinalPrice> list = new ArrayList<>();
        for (String it : sku) {
            list.add(productService.getProductPrice(merchant, it));
        }
        return list;
    }

    @GetMapping(value = "/product-availability")
    @Operation(method = "GET", description = "Get Product Availability",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductAvailability.class))))

    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "sku", schema = @Schema(name = "sku", type = "string")),
    })
    @ConditionalOnApiStatus
    @Override
    public ReadableProductAvailability getProductAvailability(StoreMerchantId store, @RequestParam String sku) {
        return productService.getProductAvailability(store, sku);
    }

    @GetMapping(value = "/products-availability")
    @Operation(method = "GET", description = "Get Products Availability",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductAvailability.class))))

    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "sku", schema = @Schema(name = "sku", type = "string")),
    })
    @ConditionalOnApiStatus
    @Override
    public List<ReadableProductAvailability> getProductsAvailability(StoreMerchantId store, @RequestParam List<String> sku) {
        return sku.stream().map(it -> productService.getProductAvailability(store, it)).toList();
    }

    @GetMapping(value = "/full-product")
    @Operation(method = "GET", description = "Get Full Product Details",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProduct.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "sku", schema = @Schema(name = "sku", type = "string")),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ConditionalOnApiStatus
    @Override
    public ReadableProduct getFullProduct(StoreMerchantId store, @RequestParam String sku, LanguageCode lang) {
        return productService.getFullProduct(store, sku, lang);
    }

    @GetMapping(value = "/minimal-product")
    @Operation(method = "GET", description = "Get Minimal Product Details",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableMinimalProduct.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "sku", schema = @Schema(name = "sku", type = "string")),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ConditionalOnApiStatus
    @Override
    public ReadableMinimalProduct getMinimalProduct(StoreMerchantId store, @RequestParam String sku, LanguageCode lang) {
        return productService.getMinimalProduct(store, sku, lang);
    }

    @GetMapping(value = "/minimal-products")
    @Operation(method = "GET", description = "Get Minimal Products Details",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableMinimalProduct.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "sku", schema = @Schema(name = "sku", type = "string")),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ConditionalOnApiStatus
    @Override
    public List<ReadableMinimalProduct> getMinimalProducts(StoreMerchantId store, List<String> sku, LanguageCode lang) {
        return sku.stream().map(it -> productService.getMinimalProduct(store, it, lang)).toList();
    }

    @PostMapping(value = "/product-quantity-update")
    @Operation(method = "GET", description = "Update product quantity",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ProductAvailabilityStatus.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "sku", schema = @Schema(name = "sku", type = "string")),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ConditionalOnApiStatus
    @Override
    public ProductAvailabilityStatus productQuantityUpdate(StoreMerchantId store, @RequestBody ProductQuantityUpdate productQuantityUpdate) throws ServiceException {
        return productService.productQuantityUpdate(store, productQuantityUpdate);
    }
}
