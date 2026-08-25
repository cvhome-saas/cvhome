package com.asrevo.cvhome.inventory.services;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.errors.EmptyReservationException;
import com.asrevo.cvhome.inventory.errors.InsufficientInventoryException;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationCommitResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReleaseResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReserveResult;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

/**
 * Holds stock for an order under the order's reference. Every call is idempotent on the ref, so checkout may retry.
 */
public interface ReservationService {

    /**
     * Takes the requested quantities out of stock and holds them until the expiry window closes.
     *
     * @throws InsufficientInventoryException a sku is short or not stocked — nothing was taken
     * @throws EmptyReservationException      the request carried no lines
     */
    ProductReservationReserveResult reserve(StoreMerchantId store, String ref, ProductReservationList request)
            throws InsufficientInventoryException, EmptyReservationException;

    /**
     * Keeps the held stock for the order. {@code status} false when there is nothing to commit — unknown ref,
     * expired, or already released.
     */
    ProductReservationCommitResult commit(StoreMerchantId store, String ref);

    /**
     * Gives the held stock back. {@code status} false when there is nothing to release — unknown ref or already
     * committed.
     */
    ProductReservationReleaseResult release(StoreMerchantId store, String ref);
}
