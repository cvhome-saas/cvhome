package com.asrevo.cvhome.errors;

import java.io.Serial;

/**
 * The caller may do this, and asked too often. Renders as HTTP 429.
 *
 * <p>
 * Distinct from {@link OperationNotAllowedException}: nothing about the request or the data is wrong, and the
 * same call will succeed later. A response in this category should say when — through the {@code Retry-After}
 * header, or a parameter naming the window.
 * </p>
 *
 * <p>
 * {@code ErrorCategory.TOO_MANY_REQUESTS} existed before this base did, and the one place that produced a 429 —
 * the sign-in rate limiter — built its body by hand because it runs in a filter, outside the advice. Anything
 * throttled inside a service throws this instead.
 * </p>
 */
public abstract class TooManyRequestsException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected TooManyRequestsException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

}
