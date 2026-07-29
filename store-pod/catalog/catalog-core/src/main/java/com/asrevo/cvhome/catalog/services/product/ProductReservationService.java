package com.asrevo.cvhome.catalog.services.product;

import com.asrevo.cvhome.catalog.model.product.ProductReservationCommitResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReleaseResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReserveResult;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

public interface ProductReservationService {
    ProductReservationReserveResult reserve(StoreMerchantId store, String ref, ProductReservationList productReservation);

    ProductReservationCommitResult commit(StoreMerchantId store, String ref);

    ProductReservationReleaseResult release(StoreMerchantId store, String ref);

}
