package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The status change asked for is not legal from the order's current state — a console trying to ship a cancelled
 * order, or to deliver one that was never confirmed. A 409: the request was well formed, the order is simply not
 * where the caller thinks it is.
 */
public class IllegalOrderTransitionException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected IllegalOrderTransitionException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static IllegalOrderTransitionException of(Long orderId, Object from, Object to) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_ILLEGAL_TRANSITION, IllegalOrderTransitionException::new)
                .detail("Order %s cannot go from %s to %s.", orderId, from, to)
                .param("orderId", orderId)
                .param("from", from)
                .param("to", to)
                .build();
    }

}
