package com.asrevo.cvhome.inventory.api.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.AvailabilityQuery;
import com.asrevo.cvhome.inventory.model.SkuInventory;
import com.asrevo.cvhome.inventory.services.ExternalInventoryService;
import com.asrevo.cvhome.inventory.services.InventoryService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * The bulk read the storefront, the console grid and the checkout all use. Public, like catalog's product reads:
 * anonymous shoppers need prices, and the {@code store} parameter is the tenant boundary.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Inventory (stock and price per sku)")
@RequiredArgsConstructor
public class ExternalInventoryApi implements ExternalInventoryService {

    private final InventoryService inventoryService;

    @Override
    @GetMapping("/availability")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public List<SkuInventory> getBySkus(StoreMerchantId store, @RequestParam List<String> skus) {
        return inventoryService.getBySkus(store, skus);
    }

    @Override
    @PostMapping("/availability/query")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public List<SkuInventory> queryBySkus(StoreMerchantId store, @Valid @RequestBody AvailabilityQuery query) {
        return inventoryService.getBySkus(store, query.skus());
    }
}
