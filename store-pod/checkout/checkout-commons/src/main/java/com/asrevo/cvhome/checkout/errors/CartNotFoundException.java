package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No cart with that code in this store, or the code was spent: its order reached a terminal state. The storefront
 * treats a 404 as "start a new cart", which is the right answer in both cases.
 */
public class CartNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CartNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CartNotFoundException of(Object code, Object store) {
        return new ErrorBuilder<>(CheckoutErrors.CART_NOT_FOUND, CartNotFoundException::new)
                .detail("No cart %s in store %s.", code, store)
                .param("code", code)
                .param("store", store)
                .build();
    }

}
