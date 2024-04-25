package com.asrevo.cvhome.store.controller.v1.product;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductList;
import com.asrevo.cvhome.store.core.model.catalog.product.group.ProductGroup;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.service.facade.items.ProductItemsFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_STORE;

/**
 * Used for product grouping such as featured items
 *
 * @author carlsamson
 */
@Controller
@RequestMapping("/api/v1")
@Tag(name = "Product groups management resource (Product Groups Management Api)")
@Slf4j
public class ProductGroupApi {

    private final ProductService productService;
    private final ProductItemsFacade productItemsFacade;

    public ProductGroupApi(ProductService productService, ProductItemsFacade productItemsFacade) {
        this.productService = productService;
        this.productItemsFacade = productItemsFacade;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/private/products/group")
    @Operation(method = "POST", description = "Create product group", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ProductGroup.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ProductGroup creteGroup(
            @RequestBody ProductGroup group,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletResponse response)
            throws Exception {

        return productItemsFacade.createProductGroup(group, merchantStore);

    }

    @ResponseStatus(HttpStatus.OK)
    @PatchMapping("/private/products/group/{code}")
    @Operation(method = "PATCH", description = "Update product group visible flag", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ProductGroup.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void updateGroup(
            @RequestBody ProductGroup group,
            @PathVariable String code,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletResponse response)
            throws Exception {

        productItemsFacade.updateProductGroup(code, group, merchantStore);

    }

    @GetMapping("/private/product/groups")
    @Operation(method = "GET", description = "Get products groups for a given merchant", summary = "", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody java.util.List<ProductGroup> list(
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletResponse response)
            throws Exception {

        return productItemsFacade.listProductGroups(merchantStore, language);

    }


    /**
     * Query for a product group public/product/group/{code}?lang=fr|en no lang it will take session
     * lang or default store lang code can be any code used while creating product group, defeult
     * being FEATURED
     *
     * @param store
     * @param language
     * @param groupCode
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/products/group/{code}")
    @Operation(method = "GET", description = "Get products by group code", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductList.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableProductList getProductItemsByGroup(
            @PathVariable final String code,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletResponse response)
            throws Exception {
        try {
            ReadableProductList list = productItemsFacade.listItemsByGroup(code, merchantStore, language);

            if (list == null) {
                response.sendError(404, "Group not fount for code " + code);
                return null;
            }

            return list;

        } catch (Exception e) {
            log.error("Error while getting products", e);
            response.sendError(503, "An error occured while retrieving products " + e.getMessage());
        }

        return null;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/private/products/{productId}/group/{code}", method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableProductList addProductToGroup(
            @PathVariable Long productId,
            @PathVariable String code,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletResponse response) {


        Product product = null;
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

        ReadableProductList list =
                productItemsFacade.addItemToGroup(product, code, merchantStore, language);

        return list;


    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(
            value = "/private/products/{productId}/group/{code}",
            method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableProductList removeProductFromGroup(
            @PathVariable Long productId,
            @PathVariable String code,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            // get the product
            Product product = productService.getById(productId);

            if (product == null) {
                response.sendError(404, "Product not fount for id " + productId);
                return null;
            }

            ReadableProductList list =
                    productItemsFacade.removeItemFromGroup(product, code, merchantStore, language);

            return list;

        } catch (Exception e) {
            log.error("Error while removing product from category", e);
            try {
                response.sendError(503, "Error while removing product from category " + e.getMessage());
            } catch (Exception ignore) {
            }

            return null;
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping("/products/group/{code}")
    @Operation(method = "DELETE", description = "Delete product group by group code", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void deleteGroup(
            @PathVariable final String code,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletResponse response) {

        productItemsFacade.deleteGroup(code, merchantStore);

    }
}
