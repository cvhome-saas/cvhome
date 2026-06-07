package com.asrevo.cvhome.catalog.services.product;

import com.asrevo.cvhome.catalog.model.product.ProductReservationResult;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

public interface ProductReservationService {
    ProductReservationResult reserve(StoreMerchantId store, Long orderId, ProductReservationList productReservation)
            throws ServiceException;

    ProductReservationResult autoCommit(StoreMerchantId merchantStore, Long orderId, ProductReservationList productReservation)
            throws ServiceException;

    void commit(StoreMerchantId store, Long orderId) throws ServiceException;

    void release(StoreMerchantId store, Long orderId) throws ServiceException;

    void expire(StoreMerchantId store, Long orderId) throws ServiceException;

}
