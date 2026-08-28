package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * An action would take the store past a ceiling its plan grants.
 *
 * <p>
 * Carries the limit and the current count as params so a client can render "you have 25 of 25 products" without
 * parsing the sentence, and so the upsell it shows names the right plan.
 * </p>
 */
public class EntitlementExceededException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected EntitlementExceededException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param store   the store the action was for
     * @param key     the entitlement that would be exceeded
     * @param limit   what the plan grants
     * @param current what the store already holds
     */
    public static EntitlementExceededException of(Object store, Object key, Integer limit, Integer current) {
        return new ErrorBuilder<>(BillingErrors.ENTITLEMENT_EXCEEDED, EntitlementExceededException::new)
                .detail("Store %s is at its %s limit of %s (currently %s).", store, key, limit, current)
                .param("store", store)
                .param("entitlement", key)
                .param("limit", limit)
                .param("current", current)
                .build();
    }

}
