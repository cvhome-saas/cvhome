package com.asrevo.cvhome.checkout.services.order;

import com.asrevo.cvhome.checkout.errors.OrderNotFoundException;
import com.asrevo.cvhome.checkout.model.signal.PaymentSignal;
import com.asrevo.cvhome.checkout.model.signal.ReservationExpiredSignal;
import com.asrevo.cvhome.checkout.model.signal.SignalOutcome;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

/**
 * Applies what payment and inventory report about an order. Idempotent, never a 4xx for a state the order cannot use.
 */
public interface OrderSignalService {

    SignalOutcome paymentSignal(StoreMerchantId store, String orderRef, PaymentSignal signal)
            throws OrderNotFoundException;

    SignalOutcome reservationExpired(StoreMerchantId store, String orderRef, ReservationExpiredSignal signal)
            throws OrderNotFoundException;
}
