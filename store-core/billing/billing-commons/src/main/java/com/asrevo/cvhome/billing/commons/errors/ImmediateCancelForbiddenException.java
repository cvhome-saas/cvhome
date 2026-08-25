package com.asrevo.cvhome.billing.commons.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AccessDeniedStoreException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * Cancelling immediately, rather than at the end of the paid period, is reserved for platform administrators.
 *
 * <p>
 * Self-serve cancellation always runs to period end, because an immediate cancel throws away time the customer has
 * already paid for. The immediate path exists for compliance requests and support intervention.
 * </p>
 */
public class ImmediateCancelForbiddenException extends AccessDeniedStoreException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ImmediateCancelForbiddenException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * @param store the store whose subscription was addressed
     */
    public static ImmediateCancelForbiddenException forStore(Object store) {
        return new ErrorBuilder<>(BillingErrors.IMMEDIATE_CANCEL_FORBIDDEN, ImmediateCancelForbiddenException::new)
                .detail("Immediate cancellation of store %s is restricted to administrators.", store)
                .param("store", store)
                .build();
    }

}
