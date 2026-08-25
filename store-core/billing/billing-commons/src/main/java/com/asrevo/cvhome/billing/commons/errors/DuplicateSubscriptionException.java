package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A subscription already exists for this store.
 *
 * <p>
 * Provisioning is idempotent — a repeated provision returns the existing row rather than raising this — so seeing it
 * means two genuinely different subscriptions were asked for on one store.
 * </p>
 */
public class DuplicateSubscriptionException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicateSubscriptionException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param store the store that already has a subscription
     */
    public static DuplicateSubscriptionException forStore(Object store) {
        return new ErrorBuilder<>(BillingErrors.SUBSCRIPTION_DUPLICATE, DuplicateSubscriptionException::new)
                .detail("Store %s already has a subscription.", store)
                .param("store", store)
                .build();
    }

}
