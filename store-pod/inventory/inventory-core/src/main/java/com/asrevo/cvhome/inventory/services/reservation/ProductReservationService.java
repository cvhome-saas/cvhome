package com.asrevo.cvhome.inventory.services.reservation;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.errors.EmptyReservationException;
import com.asrevo.cvhome.inventory.errors.InsufficientInventoryException;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationCommitResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReleaseResult;
import com.asrevo.cvhome.inventory.model.reservation.ProductReservationReserveResult;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

public interface ProductReservationService {

    /**
     * @throws InsufficientInventoryException the store does not have the stock asked for
     * @throws EmptyReservationException      the reservation carried no lines
     */
    ProductReservationReserveResult reserve(StoreMerchantId store, String ref,
                                            ProductReservationList productReservation)
            throws InsufficientInventoryException, EmptyReservationException;

    ProductReservationCommitResult commit(StoreMerchantId store, String ref);

    ProductReservationReleaseResult release(StoreMerchantId store, String ref);

}
