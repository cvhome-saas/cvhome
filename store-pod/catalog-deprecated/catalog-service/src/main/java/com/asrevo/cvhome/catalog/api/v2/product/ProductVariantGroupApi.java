package com.asrevo.cvhome.catalog.api.v2.product;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.ProductVariantGroupNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductVariantNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductVariantParentMissingException;
import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariantGroup;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariantGroup;
import com.asrevo.cvhome.catalog.service.facade.product.ProductVariantGroupFacade;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v2")
@Tags(value = @Tag(name = "Product instances group api"))
public class ProductVariantGroupApi {

    private final ProductVariantGroupFacade productVariantGroupFacade;

    public ProductVariantGroupApi(ProductVariantGroupFacade productVariantGroupFacade) {
        this.productVariantGroupFacade = productVariantGroupFacade;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = {"/private/product/productVariantGroup"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public Entity create(@Valid @RequestBody PersistableProductVariantGroup instanceGroup,
                         StoreMerchantId merchantStore, LanguageCode language) {
        Long id = productVariantGroupFacade.create(instanceGroup, merchantStore, language);

        return new Entity(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/product/productVariantGroup/{id}"})
    @Operation(method = "PUT", description = "Update product instance group",
            responses = @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema())))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void update(@PathVariable Long id, @Valid @RequestBody PersistableProductVariantGroup instance,
                       StoreMerchantId merchantStore, LanguageCode language)
            throws ProductVariantGroupNotFoundException {

        productVariantGroupFacade.update(id, instance, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/productVariantGroup/{id}"})
    @Operation(method = "GET", description = "Get product instance group",
            responses = @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema())))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableProductVariantGroup get(@PathVariable Long id, StoreMerchantId merchantStore,
                                           LanguageCode language)
            throws ProductVariantGroupNotFoundException, ProductVariantParentMissingException, InventoryNotConvertibleException {
        return productVariantGroupFacade.get(id, merchantStore, language);
    }

    // delete

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/product/productVariantGroup/{id}"})
    @Operation(method = "DELETE", description = "Delete product instance group",
            responses = @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema())))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void delete(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language)
            throws ProductVariantGroupNotFoundException, ProductVariantNotFoundException {
        productVariantGroupFacade.delete(id, id, merchantStore);
    }

    // list
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/{id}/productVariantGroup"})
    @Operation(method = "GET", description = "Delete product instance group",
            responses = @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema())))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableEntityList<ReadableProductVariantGroup> list(@PathVariable final Long id,
                                                                StoreMerchantId merchantStore, LanguageCode language, Pageable pageable)
            throws ProductVariantParentMissingException, InventoryNotConvertibleException {
        return productVariantGroupFacade.list(id, merchantStore, language, pageable);
    }

}
