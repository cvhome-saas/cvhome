package com.asrevo.cvhome.catalog.api.v2.product;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductVariantNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductVariantParentMissingException;
import com.asrevo.cvhome.catalog.errors.ProductVariantSkuConflictException;
import com.asrevo.cvhome.catalog.errors.ProductVariationOptionsIdenticalException;
import com.asrevo.cvhome.catalog.errors.ProductVariationReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariant;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.catalog.service.facade.product.ProductVariantFacade;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * Api to manage productVariant
 * <p>
 * Product variant allows to specify product size, sku and options related to this product
 * variant
 *
 * @author carlsamson
 */
@RestController
@RequestMapping("/api/v2")
@Tags(value = @Tag(name = "Product variants api"))
@Slf4j
public class ProductVariantApi {

    private final ProductVariantFacade productVariantFacade;

    public ProductVariantApi(ProductVariantFacade productVariantFacade) {
        this.productVariantFacade = productVariantFacade;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = {"/private/product/{productId}/variant"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public Entity create(@Valid @RequestBody PersistableProductVariant variant, @PathVariable Long productId,
                         StoreMerchantId merchantStore, LanguageCode language)

            throws ProductVariationOptionsIdenticalException, ProductVariationReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductVariantSkuConflictException, ProductPriceNotConvertibleException,
            InventoryNotConvertibleException, ServiceException {
        Long id = productVariantFacade.create(variant, productId, merchantStore, language);
        return new Entity(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/product/{id}/variant/{variantId}"})
    @Operation(method = "PUT", description = "Update product variant",
            responses = @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema())))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void update(@PathVariable Long id, @PathVariable Long variantId,
                       @Valid @RequestBody PersistableProductVariant variant, StoreMerchantId merchantStore,
                       LanguageCode language)

            throws ProductVariantNotFoundException, ProductVariationReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductVariantSkuConflictException, ProductPriceNotConvertibleException,
            InventoryNotConvertibleException, ServiceException {
        productVariantFacade.update(variantId, variant, id, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/{id}/variant/{sku}/unique"}, produces = "application/json")

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @Operation(method = "GET", description = "Check if option set code already exists",
            responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class)))})
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ResponseEntity<EntityExists> exists(@PathVariable Long id, @PathVariable String sku,
                                               StoreMerchantId merchantStore, LanguageCode language)

            throws ProductNotFoundException, ProductNotConvertibleException, ProductPriceNotConvertibleException,
            ProductVariantParentMissingException, InventoryNotConvertibleException {

        boolean exist = productVariantFacade.exists(sku, merchantStore, id, language);
        return new ResponseEntity<>(new EntityExists(exist), HttpStatus.OK);
    }

    @GetMapping(value = "/private/product/{id}/variant/{variantId}", produces = "application/json")
    @Operation(method = "GET", description = "Get a productVariant by id",
            summary = "For administration and shop purpose. Specifying ?merchant is required otherwise it falls back to DEFAULT")
    @ApiResponse(responseCode = "200", description = "Single product found",
            content = @Content(schema = @Schema(implementation = ReadableProductVariant.class)))

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableProductVariant get(@PathVariable final Long id, @PathVariable Long variantId,
                                      @RequestParam(value = "lang", required = false) String lang, StoreMerchantId merchantStore,
                                      LanguageCode language)
            throws ProductVariantNotFoundException, ProductVariantParentMissingException, InventoryNotConvertibleException {

        return productVariantFacade.get(variantId, id, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/{id}/variants"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableEntityList<ReadableProductVariant> list(@PathVariable final Long id, StoreMerchantId merchantStore,
                                                           LanguageCode language, Pageable pageable)
            throws ProductNotFoundException, ProductVariantParentMissingException, InventoryNotConvertibleException {

        return productVariantFacade.list(id, merchantStore, language, pageable);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/product/{id}/variant/{variantId}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void delete(@PathVariable Long id, @PathVariable Long variantId, StoreMerchantId merchantStore,
                       LanguageCode language)
            throws ProductVariantNotFoundException, ServiceException {

        productVariantFacade.delete(variantId, id, merchantStore);
    }

}
