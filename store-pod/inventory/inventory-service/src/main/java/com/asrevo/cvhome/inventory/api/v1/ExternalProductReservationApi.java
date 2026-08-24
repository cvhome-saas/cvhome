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
import com.asrevo.cvhome.inventory.services.ReservationService;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * Checkout's service-to-service reservation calls: reserve, then commit or release, keyed by the order ref.
 */
@RestController
@RequestMapping("/api/v1/private")
@Tag(name = "Reservations (hold stock for an order)")
@RequiredArgsConstructor
public class ExternalProductReservationApi implements IProductReservationService {

    private final ReservationService reservationService;

    @Override
    @PostMapping("/reserve/{ref}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.RESERVE')")
    public ProductReservationReserveResult reserve(StoreMerchantId merchantStore, @PathVariable String ref,
                                                   @RequestBody ProductReservationList productReservation)
            throws InsufficientInventoryException, EmptyReservationException {
        return reservationService.reserve(merchantStore, ref, productReservation);
    }

    @Override
    @PostMapping("/commit/{ref}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.RESERVE')")
    public ProductReservationCommitResult commit(StoreMerchantId merchantStore, @PathVariable String ref) {
        return reservationService.commit(merchantStore, ref);
    }

    @Override
    @PostMapping("/release/{ref}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.RESERVE')")
    public ProductReservationReleaseResult release(StoreMerchantId merchantStore, @PathVariable String ref) {
        return reservationService.release(merchantStore, ref);
    }
}
