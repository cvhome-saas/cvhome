package com.asrevo.cvhome.checkout.services.order;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;

@HttpExchange("/api/v1/private/order")
public interface ExternalOrderService {

    @PostExchange("/{orderRef}/payment-status")
    void updatePaymentStatus(StoreMerchantId store, @PathVariable("orderRef") String orderRef,
                             @RequestParam("status") PaymentStatus status);

    @PostExchange("/{orderRef}/reservation-expired")
    void handleReservationExpired(StoreMerchantId store, @PathVariable("orderRef") String orderRef);

}
