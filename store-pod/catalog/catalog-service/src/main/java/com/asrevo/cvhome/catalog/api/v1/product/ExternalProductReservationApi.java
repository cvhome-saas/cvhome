package com.asrevo.cvhome.catalog.api.v1.product;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.catalog.model.product.ProductReservationCommitResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReleaseResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReserveResult;
import com.asrevo.cvhome.catalog.services.product.ExternalProductReservationService;
import com.asrevo.cvhome.catalog.services.product.ProductReservationService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product definition resource (Create update and delete product definition. Serves"
        + " api v1 and v2 with backward compatibility)")
@Slf4j
@AllArgsConstructor
public class ExternalProductReservationApi implements ExternalProductReservationService {

    private final ProductReservationService productReservationService;

    @Override
    @PostMapping(value = "/private/reserve/{ref}")
    @Operation(method = "POST", description = "Update product quantity",
            responses = @ApiResponse(
                    content = @Content(schema = @Schema(implementation = ProductReservationReserveResult.class))))
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "sku", schema = @Schema(name = "sku", type = "string"))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.RESERVE')")
    @SneakyThrows
    public ProductReservationReserveResult reserve(StoreMerchantId merchantStore,
                                                   @PathVariable String ref,
                                                   @RequestBody ProductReservationList productReservation) {
        return productReservationService.reserve(merchantStore, ref, productReservation);
    }

    @Override
    @PostMapping("/private/commit/{ref}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.RESERVE')")
    public ProductReservationCommitResult commit(StoreMerchantId merchantStore, @PathVariable String ref) {
        return productReservationService.commit(merchantStore, ref);
    }

    @Override
    @PostMapping("/private/release/{ref}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.RESERVE')")
    public ProductReservationReleaseResult release(StoreMerchantId merchantStore, @PathVariable String ref) {
        return productReservationService.release(merchantStore, ref);
    }

}
