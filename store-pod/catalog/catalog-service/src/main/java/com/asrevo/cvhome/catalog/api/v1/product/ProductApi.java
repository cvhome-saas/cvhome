package com.asrevo.cvhome.catalog.api.v1.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.model.product.LightPersistableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.product.PersistableProduct;
import com.asrevo.cvhome.catalog.service.facade.product.ProductCommonFacade;
import com.asrevo.cvhome.catalog.service.facade.product.ProductFacade;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.controller.exception.UnauthorizedException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * API to create, read, update and delete a Product API.
 *
 * @author Carl Samson
 */
@Controller
@RequestMapping("/api/v1")
@Tag(
        name =
                "Product definition resource (Create udtate and delete product definition. Serves"
                        + " api v1 and v2 with backward compatibility)")
@Slf4j
public class ProductApi {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final ProductFacade productFacade;
    private final ProductCommonFacade productCommonFacade;

    public ProductApi(
            CategoryService categoryService,
            ProductService productService,
            ProductFacade productFacade,
            ProductCommonFacade productCommonFacade,
            ImageFilePath imageUtils) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.productFacade = productFacade;
        this.productCommonFacade = productCommonFacade;
    }

    /**
     * Create product
     *
     * @return Entity
     */
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/private/product", method = RequestMethod.POST)
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ResponseBody
    @ConditionalOnApiStatus
    public Entity create(
            @Valid @RequestBody PersistableProduct product,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        Long id = productCommonFacade.saveProduct(merchantStore, product, language);
        Entity returnEntity = new Entity();
        returnEntity.setId(id);
        return returnEntity;
    }

    /**
     * updates price quantity
     **/
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(
            value = "/private/product/{id}",
            produces = {APPLICATION_JSON_VALUE})
    @Operation(
            method = "PATCH",
            description = "Update product inventory",
            summary = "Updates product inventory",
            responses = @ApiResponse(content = @Content(schema = @Schema())))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ConditionalOnApiStatus
    public void update(
            @PathVariable Long id,
            @Valid @RequestBody LightPersistableProduct product,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {
        productCommonFacade.update(id, product, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/private/product/{id}", method = RequestMethod.DELETE)
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void delete(
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        productCommonFacade.deleteProduct(id, merchantStore);
    }

    /**
     * API for getting a product
     *
     * @param lang ?lang=fr|en
     * @return ReadableProduct
     * @throws Exception <p>
     *                   /api/product/123
     */
    @RequestMapping(
            value = {"/product/{friendlyUrl}", "/product/friendly/{friendlyUrl}"},
            method = RequestMethod.GET)
    @Operation(
            method = "GET",
            description = "Get a product by friendlyUrl (slug)",
            summary =
                    "For administration and shop purpose. Specifying ?merchant is "
                            + "required otherwise it falls back to DEFAULT")
    @ApiResponses(
            value = {@ApiResponse(responseCode = "200", description = "Single product found")})
    @ResponseBody
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ConditionalOnApiStatus
    // @TODO check if the friendlyUrl is id because frontend use it with id param
    // @TODO make private api for this
    public ReadableProduct getByfriendlyUrl(
            @PathVariable final String friendlyUrl,
            @RequestParam(value = "lang", required = false) String lang,
            @Parameter(hidden = true) StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language,
            HttpServletResponse response)
            throws Exception {
        ReadableProduct product =
                productFacade.getProductBySeUrl(merchantStore, friendlyUrl, language);

        if (product == null) {
            response.sendError(404, "Product not fount for id " + friendlyUrl);
            return null;
        }

        return product;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(
            value = {"/private/product/unique"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(
            method = "GET",
            description = "Check if product code already exists",
            responses =
                    @ApiResponse(
                            content =
                                    @Content(
                                            schema = @Schema(implementation = EntityExists.class))))
    @ConditionalOnApiStatus
    public ResponseEntity<EntityExists> exists(
            @RequestParam(value = "code") String code,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        boolean exists = productCommonFacade.exists(code, merchantStore);
        return new ResponseEntity<>(new EntityExists(exists), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(
            value = {"/private/product/{productId}/category/{categoryId}"},
            method = RequestMethod.POST)
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ConditionalOnApiStatus
    public void addProductToCategory(
            @PathVariable Long productId,
            @PathVariable Long categoryId,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        try {
            // get the product
            Product product = productService.getById(productId);

            if (product == null) {
                throw new ResourceNotFoundException("Product id [" + productId + "] is not found");
            }

            if (!Objects.equals(product.getStore(), merchantStore)) {
                throw new UnauthorizedException(
                        "Product id ["
                                + productId
                                + "] does not belong to store ["
                                + merchantStore
                                + "]");
            }

            Category category = categoryService.getById(categoryId);

            if (category == null) {
                throw new ResourceNotFoundException(
                        "Category id [" + categoryId + "] is not found");
            }

            if (!Objects.equals(category.getStoreMerchantId(), merchantStore)) {
                throw new UnauthorizedException(
                        "Category id ["
                                + categoryId
                                + "] does not belong to store ["
                                + merchantStore
                                + "]");
            }

            productCommonFacade.addProductToCategory(category, product, language);

        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(
            value = {"/private/product/{productId}/category/{categoryId}"},
            method = RequestMethod.DELETE)
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ConditionalOnApiStatus
    public void removeProductFromCategory(
            @PathVariable Long productId,
            @PathVariable Long categoryId,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        try {
            Product product = productService.getById(productId);

            if (product == null) {
                throw new ResourceNotFoundException("Product id [" + productId + "] is not found");
            }

            if (!Objects.equals(product.getStore(), merchantStore)) {
                throw new UnauthorizedException(
                        "Product id ["
                                + productId
                                + "] does not belong to store ["
                                + merchantStore
                                + "]");
            }

            Category category = categoryService.getById(categoryId);

            if (category == null) {
                throw new ResourceNotFoundException(
                        "Category id [" + categoryId + "] is not found");
            }

            if (!Objects.equals(category.getStoreMerchantId(), merchantStore)) {
                throw new UnauthorizedException(
                        "Category id ["
                                + categoryId
                                + "] does not belong to store ["
                                + merchantStore
                                + "]");
            }

            productCommonFacade.removeProductFromCategory(category, product, language);

        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    /**
     * Change product sort order
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = "/private/product/{id}", method = RequestMethod.PATCH)
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(
            method = "POST",
            description = "Patch product sort order",
            summary = "Change product sortOrder")
    @ConditionalOnApiStatus
    public void changeProductOrder(
            @PathVariable Long id,
            @RequestParam(value = "order", required = false, defaultValue = "0") Integer position,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        try {

            Product p = productService.getById(id);

            if (p == null) {
                throw new ResourceNotFoundException(
                        "Product [" + id + "] not found for merchant [" + merchantStore + "]");
            }

            if (!p.getStore().equals(merchantStore)) {
                throw new ResourceNotFoundException(
                        "Product [" + id + "] not found for merchant [" + merchantStore + "]");
            }

            p.setSortOrder(position);

        } catch (Exception e) {
            log.error("Error while updating Product position", e);
            throw new ServiceRuntimeException("Product [" + id + "] cannot be edited");
        }
    }
}
