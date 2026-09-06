package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AuthenticationRequiredException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The store requires a signed-in shopper for order placement and the request carried no shopper token.
 */
public class OrderLoginRequiredException extends AuthenticationRequiredException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected OrderLoginRequiredException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static OrderLoginRequiredException of(Object store) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_LOGIN_REQUIRED, OrderLoginRequiredException::new)
                .detail("Store %s requires a signed-in shopper to place an order.", store)
                .param("store", store)
                .build();
    }

}
