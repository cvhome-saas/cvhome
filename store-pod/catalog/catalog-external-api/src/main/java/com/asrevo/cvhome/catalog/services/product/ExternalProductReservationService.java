package com.asrevo.cvhome.catalog.services.product;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.catalog.model.product.ProductReservationResult;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

@HttpExchange("/api/v1/private")
public interface ExternalProductReservationService {

    @PostExchange("/reserve/{ref}")
    ProductReservationResult reserve(StoreMerchantId store, @PathVariable("ref") String ref,
                                     @RequestBody ProductReservationList productReservation);

    @PostExchange("/auto-commit/{ref}")
    ProductReservationResult autoCommit(StoreMerchantId store, @PathVariable("ref") String ref,
                                        @RequestBody ProductReservationList productReservation);

    @PostExchange("/commit/{ref}")
    ProductReservationResult commit(StoreMerchantId store, @PathVariable("ref") String ref);

    @PostExchange("/release/{ref}")
    ProductReservationResult release(StoreMerchantId store, @PathVariable("ref") String ref);

}
