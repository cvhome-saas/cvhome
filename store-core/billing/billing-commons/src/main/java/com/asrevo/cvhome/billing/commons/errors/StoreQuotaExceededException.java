package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The org may not create another store right now.
 *
 * <p>
 * Not a cap on how many stores an org may own — each store carries its own subscription and pays for itself. This
 * guards against stockpiling stores that no one ever pays for.
 * </p>
 */
public class StoreQuotaExceededException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected StoreQuotaExceededException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param org    the org that asked for another store
     * @param reason why it was refused, in the vocabulary of the quota rule that fired
     */
    public static StoreQuotaExceededException of(Object org, Object reason) {
        return new ErrorBuilder<>(BillingErrors.STORE_QUOTA_EXCEEDED, StoreQuotaExceededException::new)
                .detail("Org %s may not create another store: %s.", org, reason)
                .param("org", org)
                .param("reason", reason)
                .build();
    }

}
