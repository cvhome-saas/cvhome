package com.asrevo.cvhome.inventory.api.v1;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.PersistableInventory;
import com.asrevo.cvhome.inventory.model.PersistableInventoryBatch;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.services.InventoryService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * The merchant's write side: one upsert per sku, plus cleanup after a catalog product delete. Sku-addressed reads
 * go through the public bulk endpoint in {@link ExternalInventoryApi}; the one read that lives here is
 * product-addressed, because only the console asks "what does this product hold across all its variants".
 */
@RestController
@RequestMapping("/api/v1/private/inventory")
@Tag(name = "Inventory (stock and price per sku)")
@RequiredArgsConstructor
@Validated
public class InventoryApi {

    private final InventoryService inventoryService;

    /**
     * Every sku of the given products — the console list's stock column, which must total a product's variants
     * rather than report the default variant's quantity as though it were the product's. One query for the whole
     * page, served by the {@code (product_id, store_merchant_id)} index.
     */
    @GetMapping("/by-products")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public List<SkuInventory> getByProducts(@RequestParam @Size(max = 200) List<Long> productIds, StoreMerchantId merchantStore) {
        return inventoryService.getByProductIds(merchantStore, productIds);
    }

    @PutMapping("/bulk")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public List<SkuInventory> bulkUpsert(@Valid @RequestBody PersistableInventoryBatch batch,
                                         StoreMerchantId merchantStore) {
        return inventoryService.bulkUpsert(merchantStore, batch.entries());
    }

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

    @DeleteMapping("/{sku}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public void deleteBySku(@PathVariable String sku, StoreMerchantId merchantStore) {
        inventoryService.deleteBySku(merchantStore, sku);
    }
}
