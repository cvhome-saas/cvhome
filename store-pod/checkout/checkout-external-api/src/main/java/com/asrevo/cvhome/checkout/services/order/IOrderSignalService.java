package com.asrevo.cvhome.checkout.services.order;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import com.asrevo.cvhome.checkout.errors.OrderNotFoundException;
import com.asrevo.cvhome.checkout.model.signal.PaymentSignal;
import com.asrevo.cvhome.checkout.model.signal.ReservationExpiredSignal;
import com.asrevo.cvhome.checkout.model.signal.SignalOutcome;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * The server half of the order-signal contract: what {@code ExternalOrderSignalApi} implements, in the vocabulary of
 * this service's own exceptions. Callers never see this interface — they proxy {@link ExternalOrderSignalService},
 * whose paths must match by eye ({@code ExternalOrderSignalServiceContractTest} checks).
 */
public interface IOrderSignalService {

    SignalOutcome signalPayment(StoreMerchantId store, @PathVariable("orderRef") String orderRef,
                                @RequestBody PaymentSignal signal) throws OrderNotFoundException;

    SignalOutcome signalReservationExpired(StoreMerchantId store, @PathVariable("orderRef") String orderRef,
                                           @RequestBody ReservationExpiredSignal signal) throws OrderNotFoundException;
}
