package com.asrevo.cvhome.inventory.api.v1;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.errors.EmptyReservationException;
import com.asrevo.cvhome.inventory.errors.InsufficientInventoryException;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationCommitResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReleaseResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReserveResult;
import com.asrevo.cvhome.inventory.services.IProductReservationService;
import com.asrevo.cvhome.inventory.services.reservation.ProductReservationService;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product reservation resource (reserve, commit and release stock for an order)")
@Slf4j
@AllArgsConstructor
public class ExternalProductReservationApi implements IProductReservationService {

    private final ProductReservationService productReservationService;

    @Override
    @PostMapping(value = "/private/reserve/{ref}")
    @Operation(method = "POST", description = "Reserve product quantity",
            responses = @ApiResponse(
                    content = @Content(schema = @Schema(implementation = ProductReservationReserveResult.class))))
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.RESERVE')")
    public ProductReservationReserveResult reserve(StoreMerchantId merchantStore,
                                                   @PathVariable String ref,
                                                   @RequestBody ProductReservationList productReservation)
            throws InsufficientInventoryException, EmptyReservationException {
        return productReservationService.reserve(merchantStore, ref, productReservation);
    }

    @Override
    @PostMapping("/private/commit/{ref}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.RESERVE')")
    public ProductReservationCommitResult commit(StoreMerchantId merchantStore, @PathVariable String ref) {
        return productReservationService.commit(merchantStore, ref);
    }

    @Override
    @PostMapping("/private/release/{ref}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.RESERVE')")
    public ProductReservationReleaseResult release(StoreMerchantId merchantStore, @PathVariable String ref) {
        return productReservationService.release(merchantStore, ref);
    }

}
