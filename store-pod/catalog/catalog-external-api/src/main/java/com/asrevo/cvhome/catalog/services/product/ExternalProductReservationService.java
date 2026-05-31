package com.asrevo.cvhome.catalog.services.product;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.catalog.model.product.ProductReservationStatus;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

@HttpExchange("/api/v1/private")
public interface ExternalProductReservationService {

    @PostExchange("/reserve/{orderId}")
    ProductReservationStatus reserve(StoreMerchantId store, @PathVariable("orderId") Long orderId,
                                     @RequestBody ProductReservationList productReservation);

    @PostExchange("/auto-commit/{orderId}")
    ProductReservationStatus autoCommit(StoreMerchantId store, @PathVariable("orderId") Long orderId,
                                        @RequestBody ProductReservationList productReservation);

    @PostExchange("/commit/{orderId}")
    void commit(StoreMerchantId store, @PathVariable("orderId") Long orderId);

    @PostExchange("/release/{orderId}")
    void release(StoreMerchantId store, @PathVariable("orderId") Long orderId);

}
