package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No subscription exists for the addressed store.
 *
 * <p>
 * Distinct from a suspended subscription, which exists and says no. This is a store billing has never seen, which
 * normally means provisioning has not caught up yet rather than that anything is wrong.
 * </p>
 */
public class SubscriptionNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected SubscriptionNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param store the store whose subscription was addressed
     */
    public static SubscriptionNotFoundException forStore(Object store) {
        return new ErrorBuilder<>(BillingErrors.SUBSCRIPTION_NOT_FOUND, SubscriptionNotFoundException::new)
                .detail("No subscription for store %s.", store)
                .param("store", store)
                .build();
    }

}
