package com.asrevo.cvhome.catalog.api.v1.product;

import java.util.Objects;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.billing.commons.errors.EntitlementExceededException;
import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.errors.CategoryAlreadyAttachedException;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ForeignStoreProductAccessException;
import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ManufacturerReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotPersistedException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductTypeReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductVariantSkuConflictException;
import com.asrevo.cvhome.catalog.errors.ProductVariationReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.LightPersistableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.product.PersistableProduct;
import com.asrevo.cvhome.catalog.service.facade.product.ProductCommonFacade;
import com.asrevo.cvhome.catalog.service.facade.product.ProductFacade;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * API to create, read, update and delete a Product API.
 *
 * @author Carl Samson
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product definition resource (Create udtate and delete product definition. Serves")
@Slf4j
public class ProductApi {

    private final CategoryService categoryService;

    private final ProductService productService;

    private final ProductFacade productFacade;

    private final ProductCommonFacade productCommonFacade;

    public ProductApi(CategoryService categoryService, ProductService productService, ProductFacade productFacade,
                      ProductCommonFacade productCommonFacade) {
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
    @PostMapping(value = "/private/product")

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public Entity create(@Valid @RequestBody PersistableProduct product, StoreMerchantId merchantStore,
                         LanguageCode language)
            throws ProductNotConvertibleException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException,
            ProductVariationReferenceUnresolvableException, ProductReferenceUnresolvableException,
            ProductVariantSkuConflictException, ProductPriceNotConvertibleException,
            InventoryNotConvertibleException, ProductNotPersistedException, EntitlementExceededException {

        Long id = productCommonFacade.saveProduct(merchantStore, product, language);
        Entity returnEntity = new Entity();
        returnEntity.setId(id);
        return returnEntity;
    }

    /**
     * updates price quantity
     **/
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/private/product/{id}", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "PATCH", description = "Update product inventory", summary = "Updates product inventory",
            responses = @ApiResponse(content = @Content(schema = @Schema())))

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void update(@PathVariable Long id, @Valid @RequestBody LightPersistableProduct product,
                       StoreMerchantId merchantStore, LanguageCode language) {
        productCommonFacade.update(id, product, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = "/private/product/{id}")

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void delete(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language)
            throws ProductNotFoundException {

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
    @GetMapping(value = {"/product/{friendlyUrl}", "/product/friendly/{friendlyUrl}"})
    @Operation(method = "GET", description = "Get a product by friendlyUrl (slug)",
            summary = "For administration and shop purpose. Specifying ?merchant is required otherwise it falls back to DEFAULT")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Single product found")})


    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    public ReadableProduct getByfriendlyUrl(@PathVariable final String friendlyUrl,
                                            @RequestParam(value = "lang", required = false) String lang, StoreMerchantId merchantStore,
                                            LanguageCode language, HttpServletResponse response) throws Exception {
        ReadableProduct product = productFacade.getProductBySeUrl(merchantStore, friendlyUrl, language);

        if (product == null) {
            response.sendError(404, "Product not fount for id %s".formatted(friendlyUrl));
            return null;
        }

        return product;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @Operation(method = "GET", description = "Check if product code already exists",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ResponseEntity<EntityExists> exists(@RequestParam(value = "code") String code, StoreMerchantId merchantStore,
                                               LanguageCode language) {

        boolean exists = productCommonFacade.exists(code, merchantStore);
        return new ResponseEntity<>(new EntityExists(exists), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = {"/private/product/{productId}/category/{categoryId}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void addProductToCategory(@PathVariable Long productId, @PathVariable Long categoryId,
                                     StoreMerchantId merchantStore, LanguageCode language)
            throws CategoryAlreadyAttachedException, CategoryNotFoundException, ProductNotFoundException,
            ForeignStoreProductAccessException, ProductNotConvertibleException, ProductNotPersistedException {

        Product product = requireProduct(productId, merchantStore);
        Category category = requireCategory(categoryId, merchantStore);

        productCommonFacade.addProductToCategory(category, product, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/product/{productId}/category/{categoryId}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void removeProductFromCategory(@PathVariable Long productId, @PathVariable Long categoryId,
                                          StoreMerchantId merchantStore, LanguageCode language)
            throws CategoryNotFoundException, Exception, ProductNotFoundException {

        Product product = requireProduct(productId, merchantStore);
        Category category = requireCategory(categoryId, merchantStore);

        productCommonFacade.removeProductFromCategory(category, product, language);
    }

    /**
     * Change product sort order
     */
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/private/product/{id}")

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @Operation(method = "POST", description = "Patch product sort order", summary = "Change product sortOrder")

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void changeProductOrder(@PathVariable Long id,
                                   @RequestParam(value = "order", required = false, defaultValue = "0") Integer position,
                                   StoreMerchantId merchantStore, LanguageCode language)
            throws ProductNotFoundException, ForeignStoreProductAccessException {

        Product p = requireProduct(id, merchantStore);
        p.setSortOrder(position);
    }

    /**
     * Resolves a product the caller is entitled to touch.
     *
     * <p>
     * The cross-store check answers 403, replacing an {@code UnauthorizedException} that answered 401. The caller is
     * authenticated and passed {@code @PreAuthorize}; the product simply belongs to another store, and telling a
     * storefront to re-authenticate would send it round a loop that cannot terminate.
     * </p>
     */
    private Product requireProduct(Long productId, StoreMerchantId merchantStore)
            throws ProductNotFoundException, ForeignStoreProductAccessException {
        Product product = productService.getById(productId);
        if (product == null) {
            throw ProductNotFoundException.of(productId, merchantStore);
        }
        if (!Objects.equals(product.getStore(), merchantStore)) {
            throw ForeignStoreProductAccessException.of(productId, merchantStore);
        }
        return product;
    }

    private Category requireCategory(Long categoryId, StoreMerchantId merchantStore)
            throws CategoryNotFoundException, ForeignStoreProductAccessException {
        Category category = categoryService.getById(categoryId);
        if (category == null) {
            throw CategoryNotFoundException.of(categoryId, merchantStore);
        }
        if (!Objects.equals(category.getStoreMerchantId(), merchantStore)) {
            throw ForeignStoreProductAccessException.of(categoryId, merchantStore);
        }
        return category;
    }

}
