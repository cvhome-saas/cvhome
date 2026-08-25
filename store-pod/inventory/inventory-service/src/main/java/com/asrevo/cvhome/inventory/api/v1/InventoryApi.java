package com.asrevo.cvhome.inventory.api.v1;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.PersistableInventory;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.services.InventoryService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * The merchant's write side: one upsert per sku, plus cleanup after a catalog product delete. Reads go through the
 * public bulk endpoint in {@link ExternalInventoryApi}.
 */
@RestController
@RequestMapping("/api/v1/private/inventory")
@Tag(name = "Inventory (stock and price per sku)")
@RequiredArgsConstructor
public class InventoryApi {

    private final InventoryService inventoryService;

    @PutMapping("/{sku}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public SkuInventory upsert(@PathVariable String sku, @Valid @RequestBody PersistableInventory inventory,
                               StoreMerchantId merchantStore) {
        return inventoryService.upsert(merchantStore, sku, inventory);
    }

    @DeleteMapping("/by-product/{productId}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public void deleteByProduct(@PathVariable Long productId, StoreMerchantId merchantStore) {
        inventoryService.deleteByProduct(merchantStore, productId);
    }
}
