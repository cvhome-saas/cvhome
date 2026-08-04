package com.asrevo.cvhome.catalog.services.product;

import com.asrevo.cvhome.catalog.errors.EmptyReservationException;
import com.asrevo.cvhome.catalog.errors.InsufficientInventoryException;
import com.asrevo.cvhome.catalog.model.product.ProductReservationCommitResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReleaseResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReserveResult;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

public interface ProductReservationService {

    /**
     * @throws InsufficientInventoryException the store does not have the stock asked for
     * @throws EmptyReservationException      the reservation carried no lines
     */
    ProductReservationReserveResult reserve(StoreMerchantId store, String ref, ProductReservationList productReservation)
            throws InsufficientInventoryException, EmptyReservationException;

    ProductReservationCommitResult commit(StoreMerchantId store, String ref);

    ProductReservationReleaseResult release(StoreMerchantId store, String ref);

}
