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
import jakarta.servlet.http.HttpServletResponse;
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

    /**
     * Query for a product group public/product/group/{code}?lang=fr|en no lang it will take session
     * lang or default store lang code can be any code used while creating product group, defeult
     * being FEATURED
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/products/{id}/related")
    @Operation(method = "GET", description = "Get products by group code",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductList.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableProductList getProductRelatedItems(
            @PathVariable final Long id,
            @Parameter(hidden = true) StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language,
            HttpServletResponse response)
            throws Exception {
        try {
            Product product = productService.getById(id);
            if (product == null) {
                response.sendError(404, "Product id " + id + " does not exists");
                return null;
            }


            return productItemsFacade.relatedItems(product, merchantStore, language);

        } catch (Exception e) {
            log.error("Error while getting product reviews", e);
            try {
                response.sendError(503, "Error while getting product reviews" + e.getMessage());
            } catch (Exception ignore) {
            }

            return null;
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/private/products/{relatedId}/related/{productId}", method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableProductList addProductToRelatedGroup(
            @PathVariable Long relatedId,
            @PathVariable Long productId,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language,
            HttpServletResponse response) {

        Product relatedProduct;
        try {
            // get the relatedProduct
            relatedProduct = productService.findOne(relatedId, merchantStore);

            if (relatedProduct == null) {
                response.sendError(404, "Related Product not fount for id " + productId);
                return null;
            }

        } catch (Exception e) {
            log.error("Error while adding related product to group", e);
            try {
                response.sendError(503, "Error while adding related product to group " + e.getMessage());
            } catch (Exception ignore) {
            }

            return null;
        }

        Product product;
        try {
            // get the product
            product = productService.findOne(productId, merchantStore);

            if (product == null) {
                response.sendError(404, "Product not fount for id " + productId);
                return null;
            }

        } catch (Exception e) {
            log.error("Error while adding product to group", e);
            try {
                response.sendError(503, "Error while adding product to group " + e.getMessage());
            } catch (Exception ignore) {
            }

            return null;
        }
        return productItemsFacade.addItemToRelatedProduct(relatedProduct, product, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/private/products/{relatedId}/related/{productId}", method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableProductList removeProductToRelatedGroup(
            @PathVariable Long relatedId,
            @PathVariable Long productId,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language,
            HttpServletResponse response) throws ServiceException {

        Product relatedProduct;
        try {
            // get the relatedProduct
            relatedProduct = productService.findOne(relatedId, merchantStore);

            if (relatedProduct == null) {
                response.sendError(404, "Related Product not fount for id " + productId);
                return null;
            }

        } catch (Exception e) {
            log.error("Error while adding related product to group", e);
            try {
                response.sendError(503, "Error while adding related product to group " + e.getMessage());
            } catch (Exception ignore) {
            }

            return null;
        }

        Product product;
        try {
            // get the product
            product = productService.findOne(productId, merchantStore);

            if (product == null) {
                response.sendError(404, "Product not fount for id " + productId);
                return null;
            }

        } catch (Exception e) {
            log.error("Error while adding product to group", e);
            try {
                response.sendError(503, "Error while adding product to group " + e.getMessage());
            } catch (Exception ignore) {
            }

            return null;
        }
        return productItemsFacade.removeItemFromRelated(relatedProduct, product, merchantStore, language);
    }

}
