package com.asrevo.cvhome.inventory.api.v1;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.availability.SkuInventory;
import com.asrevo.cvhome.inventory.service.facade.SkuInventoryFacade;
import com.asrevo.cvhome.inventory.services.ExternalInventoryService;
import com.asrevo.cvhome.store.core.constants.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * The bulk read the storefront listing/PDP, the console product grid and the checkout composition all live on.
 *
 * <p>
 * Public like catalog's storefront reads: it serves anonymous shoppers, so the store-scoping query parameter is the
 * tenant boundary, exactly as on catalog's {@code /products}.
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Sku inventory resource (stock and price by skus)")
@Slf4j
@AllArgsConstructor
public class ExternalInventoryApi implements ExternalInventoryService {

    private final SkuInventoryFacade skuInventoryFacade;

    @Override
    @GetMapping(value = "/availability")
    @Operation(method = "GET", description = "Get stock and price for a set of skus",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = SkuInventory.class))))
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "skus", schema = @Schema(name = "skus", type = "string"))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    public List<SkuInventory> getBySkus(StoreMerchantId store, @RequestParam List<String> skus, LanguageCode lang) {
        return skuInventoryFacade.getBySkus(skus, store, lang);
    }

}
