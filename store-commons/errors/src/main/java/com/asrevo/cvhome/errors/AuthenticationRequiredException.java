package com.asrevo.cvhome.errors;

import java.io.Serial;

/**
 * The operation needs an authenticated caller and there is none — no token, an unauthenticated one, or one that does
 * not belong to the store being addressed. Renders as HTTP 401.
 *
 * <p>
 * The counterpart of {@link AccessDeniedStoreException}, and the distinction is the one a client acts on: 401 means
 * "log in and try again", 403 means "logging in again will not help". A store that requires login for order placement
 * produces the first; a customer reaching for another customer's order produces the second.
 * </p>
 */
public abstract class AuthenticationRequiredException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected AuthenticationRequiredException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

}
