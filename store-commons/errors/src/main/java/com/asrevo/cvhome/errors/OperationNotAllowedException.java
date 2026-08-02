package com.asrevo.cvhome.errors;

import java.io.Serial;

/**
 * The request is well-formed and the caller is permitted, but a business rule refuses to carry it out — deleting a
 * category that still has products, cancelling an already-shipped order. Renders as HTTP 422.
 *
 * <p>
 * Distinct from {@link AccessDeniedStoreException}: this is about the state of the data, not about who is asking.
 * </p>
 */
public abstract class OperationNotAllowedException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected OperationNotAllowedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }


}
