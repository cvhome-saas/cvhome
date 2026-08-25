package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The requested move is not legal from the subscription's current state — resuming one that was never scheduled to
 * cancel, upgrading one that is already canceled.
 *
 * <p>
 * Moving to the state a subscription is already in never raises this: a redelivered webhook must be a no-op, not a
 * failure.
 * </p>
 */
public class IllegalSubscriptionTransitionException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected IllegalSubscriptionTransitionException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param store the store whose subscription was addressed
     * @param from  the state it is in
     * @param to    the state that was asked for
     */
    public static IllegalSubscriptionTransitionException of(Object store, Object from, Object to) {
        return new ErrorBuilder<>(BillingErrors.SUBSCRIPTION_TRANSITION_ILLEGAL,
                IllegalSubscriptionTransitionException::new)
                .detail("Subscription of store %s cannot move from %s to %s.", store, from, to)
                .param("store", store)
                .param("from", from)
                .param("to", to)
                .build();
    }

}
