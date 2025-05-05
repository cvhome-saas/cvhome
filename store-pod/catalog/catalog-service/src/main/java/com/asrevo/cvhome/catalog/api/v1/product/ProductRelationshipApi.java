package com.asrevo.cvhome.catalog.api.v1.product;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.catalog.service.facade.items.ProductItemsFacade;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

@Controller
@RequestMapping("/api/v1")
@Tag(name = "Product groups management resource (Product Groups Management Api)")
@Slf4j
public class ProductRelationshipApi {
    private final ProductService productService;
    private final ProductItemsFacade productItemsFacade;

    public ProductRelationshipApi(ProductService productService, ProductItemsFacade productItemsFacade) {
        this.productService = productService;
        this.productItemsFacade = productItemsFacade;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/private/products/{id}/related")
    @Operation(method = "GET", description = "Get products by group code",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductList.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableProductList productRelatedProducts(
            @PathVariable final Long id,
            @Parameter(hidden = true) StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {
        Product product = productService.getById(id);
        return productItemsFacade.relatedTinyProducts(product, merchantStore, LanguageCode.nonLanguage());

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/products/{id}/related")
    @Operation(method = "GET", description = "Get products by group code",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductList.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableProductList getProductRelatedProducts(
            @PathVariable final Long id,
            @Parameter(hidden = true) StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {
        Product product = productService.getById(id);
        return productItemsFacade.relatedMinimalProducts(product, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/private/products/{relatedId}/related/{productId}", method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody void addProductToRelatedGroup(
            @PathVariable Long relatedId,
            @PathVariable Long productId,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        Product relatedProduct = productService.findOne(relatedId, merchantStore);

        Product product = productService.findOne(productId, merchantStore);
        productItemsFacade.addItemToRelatedProduct(relatedProduct, product, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/private/products/{relatedId}/related/{productId}", method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody void removeProductToRelatedGroup(
            @PathVariable Long relatedId,
            @PathVariable Long productId,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) throws ServiceException {

        Product relatedProduct = productService.findOne(relatedId, merchantStore);

        Product product = productService.findOne(productId, merchantStore);
        productItemsFacade.removeItemFromRelated(relatedProduct, product, merchantStore, language);
    }

}
