package com.asrevo.cvhome.inventory.services;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.errors.EmptyReservationException;
import com.asrevo.cvhome.inventory.errors.InsufficientInventoryException;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationCommitResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReleaseResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReserveResult;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

/**
 * The reservation API's HTTP contract, in the inventory service's own vocabulary.
 *
 * <p>
 * Implemented by {@code ExternalProductReservationApi}, the controller, which is why its {@code throws} clauses name
 * <em>server-side</em> exceptions. Callers should depend on {@link ExternalProductReservationService} instead, which
 * restates the same operations in the caller's vocabulary; {@code InventoryApiErrors.INVENTORY} decodes the wire into
 * those caller-side types, which is why they are deliberately absent below.
 * </p>
 */
public interface IProductReservationService {

    /**
     * Takes stock for an order, keyed by the order's reference so a repeated call is idempotent.
     *
     * @throws InsufficientInventoryException the store does not have the stock asked for
     * @throws EmptyReservationException      the reservation carried no lines
     */
    ProductReservationReserveResult reserve(StoreMerchantId store, @PathVariable("ref") String ref,
                                            @RequestBody ProductReservationList productReservation)
            throws InsufficientInventoryException, EmptyReservationException;

    /**
     * Turns a temporary reservation into a permanent one. A reservation that cannot be committed — expired, or gone —
     * comes back as {@code status(false)} rather than an exception: that is a state this service observed, not a
     * failure of the call.
     */
    ProductReservationCommitResult commit(StoreMerchantId store, @PathVariable("ref") String ref);

    /**
     * Returns reserved stock to availability. Reports its outcome the same way {@link #commit} does.
     */
    ProductReservationReleaseResult release(StoreMerchantId store, @PathVariable("ref") String ref);

}
