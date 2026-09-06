package com.asrevo.cvhome.checkout.services.order;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.checkout.api.errors.CheckoutApiUnavailableException;
import com.asrevo.cvhome.checkout.model.signal.PaymentSignal;
import com.asrevo.cvhome.checkout.model.signal.ReservationExpiredSignal;
import com.asrevo.cvhome.checkout.model.signal.SignalOutcome;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Payment and inventory telling checkout what happened to an order: a payment settled, failed, was rejected; a
 * reservation expired. Built with {@code RestClientBuilder.buildClient("checkout", ExternalOrderSignalService.class,
 * CheckoutApiErrors.CATALOG)}; gated by {@code STORE-POD.CHECKOUT.SIGNAL}, so only a service principal of this pod
 * may call it.
 *
 * <p>
 * Both calls are idempotent: a redelivered signal answers {@code DUPLICATE}, a signal the order cannot use answers
 * {@code IGNORED}. Neither is an error. The only failure is 404 for a ref this store never issued.
 * </p>
 */
@HttpExchange("/api/v1/private/orders")
public interface ExternalOrderSignalService {

    @PostExchange("/{orderRef}/signals/payment")
    SignalOutcome signalPayment(StoreMerchantId store, @PathVariable("orderRef") String orderRef,
                                @RequestBody PaymentSignal signal) throws CheckoutApiUnavailableException;

    @PostExchange("/{orderRef}/signals/reservation-expired")
    SignalOutcome signalReservationExpired(StoreMerchantId store, @PathVariable("orderRef") String orderRef,
                                           @RequestBody ReservationExpiredSignal signal)
            throws CheckoutApiUnavailableException;
}
