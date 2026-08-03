package com.asrevo.cvhome.catalog.api.v1.product;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.catalog.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.catalog.errors.InventoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.InventoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductIdParameterMissingException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductVariantNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductVariantReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.inventory.PersistableInventory;
import com.asrevo.cvhome.catalog.model.product.inventory.ReadableInventory;
import com.asrevo.cvhome.catalog.service.facade.product.ProductInventoryFacade;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product inventory resource (Product Inventory Api)")
@Slf4j
public class ProductInventoryApi {

    private final ProductInventoryFacade productInventoryFacade;

    public ProductInventoryApi(ProductInventoryFacade productInventoryFacade) {
        this.productInventoryFacade = productInventoryFacade;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = {"/private/product/{productId}/inventory"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableInventory create(@PathVariable Long productId, @Valid @RequestBody PersistableInventory inventory,
                                    StoreMerchantId merchantStore, LanguageCode language)

            throws InventoryNotConvertibleException, InventoryReferenceUnresolvableException,
            ProductReferenceUnresolvableException, ProductVariantReferenceUnresolvableException, ServiceException {
        inventory.setProductId(productId);
        return productInventoryFacade.add(inventory, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/product/{productId}/inventory/{id}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void update(@PathVariable Long productId, @PathVariable Long id,
                       @Valid @RequestBody PersistableInventory inventory, StoreMerchantId merchantStore, LanguageCode language)

            throws InventoryNotFoundException, InventoryNotConvertibleException, InventoryReferenceUnresolvableException,
            ProductNotFoundException, ProductReferenceUnresolvableException, ProductVariantNotFoundException,
            ProductVariantReferenceUnresolvableException, ServiceException {
        inventory.setId(id);
        inventory.setProductId(inventory.getProductId());
        inventory.setVariant(inventory.getVariant());
        inventory.setProductId(productId);
        productInventoryFacade.update(inventory, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/product/{productId}/inventory/{id}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void delete(@PathVariable Long productId, @PathVariable Long id, StoreMerchantId merchantStore,
                       LanguageCode language)
            throws InventoryNotFoundException, ServiceException {

        productInventoryFacade.delete(productId, id, merchantStore);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/{sku}/inventory"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableEntityList<ReadableInventory> getBySku(@PathVariable String sku, StoreMerchantId merchantStore,
                                                          LanguageCode language, Pageable pageable)
            throws ProductNotFoundException, InventoryNotConvertibleException {

        return productInventoryFacade.get(sku, merchantStore, language, pageable);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/inventory"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableEntityList<ReadableInventory> getByProductId(@RequestParam Long productId,
                                                                StoreMerchantId merchantStore, LanguageCode language, Pageable pageable)
            throws InventoryNotConvertibleException, ProductIdParameterMissingException {

        if (productId == null) {
            throw ProductIdParameterMissingException.of();
        }

        return productInventoryFacade.get(productId, merchantStore, language, pageable);
    }

}
