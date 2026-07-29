package com.asrevo.cvhome.catalog.services.product;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.catalog.model.product.ProductReservationCommitResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReleaseResult;
import com.asrevo.cvhome.catalog.model.product.ProductReservationReserveResult;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.catalog.ProductReservationList;

@HttpExchange("/api/v1/private")
public interface ExternalProductReservationService {

    @PostExchange("/reserve/{ref}")
    ProductReservationReserveResult reserve(StoreMerchantId store, @PathVariable("ref") String ref,
                                            @RequestBody ProductReservationList productReservation);

    @PostExchange("/commit/{ref}")
    ProductReservationCommitResult commit(StoreMerchantId store, @PathVariable("ref") String ref);

    @PostExchange("/release/{ref}")
    ProductReservationReleaseResult release(StoreMerchantId store, @PathVariable("ref") String ref);

}
