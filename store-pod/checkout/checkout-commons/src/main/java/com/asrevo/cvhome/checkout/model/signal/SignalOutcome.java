package com.asrevo.cvhome.checkout.model.signal;

import java.io.Serializable;

import com.asrevo.cvhome.checkout.model.order.OrderEventOutcome;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;

/**
 * What a signal did, and where the order stands afterwards. Always a 200: a signal the order cannot use is
 * {@code IGNORED}, never an error, because payment's outbox would otherwise retry a decision that will not change.
 */
public record SignalOutcome(OrderEventOutcome outcome, OrderStatus orderStatus, PaymentStatus paymentStatus)
        implements Serializable {
}
