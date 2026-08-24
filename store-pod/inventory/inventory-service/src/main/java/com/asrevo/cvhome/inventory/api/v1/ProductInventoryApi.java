package com.asrevo.cvhome.inventory.api.v1;

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

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.errors.InventoryNotConvertibleException;
import com.asrevo.cvhome.inventory.errors.InventoryNotFoundException;
import com.asrevo.cvhome.inventory.errors.InventoryReferenceUnresolvableException;
import com.asrevo.cvhome.inventory.model.inventory.PersistableInventory;
import com.asrevo.cvhome.inventory.model.inventory.ReadableInventory;
import com.asrevo.cvhome.inventory.service.facade.ProductInventoryFacade;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * Inventory CRUD, moved out of the catalog service. Paths are preserved so a client changes only the gateway prefix
 * ({@code /catalog/...} to {@code /inventory/...}).
 */
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
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public ReadableInventory create(@PathVariable Long productId, @Valid @RequestBody PersistableInventory inventory,
                                    StoreMerchantId merchantStore, LanguageCode language)
            throws InventoryNotConvertibleException, InventoryReferenceUnresolvableException {
        inventory.setProductId(productId);
        return productInventoryFacade.add(inventory, merchantStore, language);
    }

    /**
     * Sku-addressed upsert — the console's single-product write path: one call keyed by the sku it already knows,
     * without first resolving an inventory id.
     */
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/inventory/{sku}"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public ReadableInventory upsert(@PathVariable String sku, @Valid @RequestBody PersistableInventory inventory,
                                    StoreMerchantId merchantStore, LanguageCode language)
            throws InventoryNotConvertibleException, InventoryReferenceUnresolvableException {
        inventory.setSku(sku);
        return productInventoryFacade.add(inventory, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/product/{productId}/inventory/{id}"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public void update(@PathVariable Long productId, @PathVariable Long id,
                       @Valid @RequestBody PersistableInventory inventory, StoreMerchantId merchantStore,
                       LanguageCode language)
            throws InventoryNotFoundException, InventoryNotConvertibleException,
            InventoryReferenceUnresolvableException {
        inventory.setId(id);
        inventory.setProductId(productId);
        productInventoryFacade.update(inventory, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/product/{productId}/inventory/{id}"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public void delete(@PathVariable Long productId, @PathVariable Long id, StoreMerchantId merchantStore,
                       LanguageCode language)
            throws InventoryNotFoundException {

        productInventoryFacade.delete(productId, id, merchantStore);
    }

    /**
     * Orphan cleanup after a catalog product delete: removes every inventory row carrying the product id. Best-effort
     * by design — deleting a product whose inventory is already gone is a no-op, not an error.
     */
    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/inventory/by-product/{productId}"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public void deleteByProduct(@PathVariable Long productId, StoreMerchantId merchantStore, LanguageCode language) {
        productInventoryFacade.deleteByProduct(productId, merchantStore);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/{sku}/inventory"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public ReadableEntityList<ReadableInventory> getBySku(@PathVariable String sku, StoreMerchantId merchantStore,
                                                          LanguageCode language, Pageable pageable)
            throws InventoryNotConvertibleException {

        return productInventoryFacade.get(sku, merchantStore, language, pageable);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/inventory"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public ReadableEntityList<ReadableInventory> getByProductId(@RequestParam Long productId,
                                                                StoreMerchantId merchantStore, LanguageCode language,
                                                                Pageable pageable)
            throws InventoryNotConvertibleException {

        return productInventoryFacade.get(productId, merchantStore, language, pageable);
    }

}
