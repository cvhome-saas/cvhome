package com.asrevo.cvhome.catalog.services.product;

import com.asrevo.cvhome.catalog.model.product.ProductReservationResult;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

public interface ProductReservationService {
    ProductReservationResult reserve(StoreMerchantId store, String ref, ProductReservationList productReservation);

    ProductReservationResult autoCommit(StoreMerchantId merchantStore, String ref, ProductReservationList productReservation);

    ProductReservationResult commit(StoreMerchantId store, String ref);

    ProductReservationResult release(StoreMerchantId store, String ref);

}
