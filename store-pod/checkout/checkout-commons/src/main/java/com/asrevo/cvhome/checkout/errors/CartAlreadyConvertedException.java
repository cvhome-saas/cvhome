package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The cart already became an order. While that order is open the cart is read-only and a repeat checkout resumes
 * the order instead; once the order is closed the cart code is spent and answers 404.
 */
public class CartAlreadyConvertedException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CartAlreadyConvertedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CartAlreadyConvertedException of(Object code, Long orderId) {
        return new ErrorBuilder<>(CheckoutErrors.CART_ALREADY_CONVERTED, CartAlreadyConvertedException::new)
                .detail("Cart %s already became order %s.", code, orderId)
                .param("code", code)
                .param("orderId", orderId)
                .build();
    }

}
