package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * A checkout for a cart with nothing in it.
 */
public class CartEmptyException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CartEmptyException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CartEmptyException of(Object code) {
        return new ErrorBuilder<>(CheckoutErrors.CART_EMPTY, CartEmptyException::new)
                .detail("Cart %s has no lines to order.", code)
                .param("code", code)
                .build();
    }

}
