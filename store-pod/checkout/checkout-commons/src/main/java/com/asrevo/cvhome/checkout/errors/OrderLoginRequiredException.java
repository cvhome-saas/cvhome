package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AuthenticationRequiredException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The store requires a logged-in shopper to place or track an order, and the request carried none.
 *
 * <p>
 * Replaces a {@code ServiceRuntimeException} whose message read "HTTP 401 Unauthorized — Login required" while the
 * response it produced was a 400: the status the storefront needs in order to send the shopper to the login page was
 * in the text, where nothing could act on it.
 * </p>
 */
public class OrderLoginRequiredException extends AuthenticationRequiredException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected OrderLoginRequiredException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static OrderLoginRequiredException of(Object store) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_LOGIN_REQUIRED, OrderLoginRequiredException::new)
                .detail("Store %s requires an authenticated shopper for this operation.", store)
                .param("store", store)
                .build();
    }

}
