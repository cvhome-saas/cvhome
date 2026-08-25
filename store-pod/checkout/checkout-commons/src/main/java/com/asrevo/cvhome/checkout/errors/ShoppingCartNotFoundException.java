package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No cart exists for that code or id in this store.
 */
public class ShoppingCartNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ShoppingCartNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ShoppingCartNotFoundException byCode(String cartCode) {
        return new ErrorBuilder<>(CheckoutErrors.CART_NOT_FOUND, ShoppingCartNotFoundException::new)
                .detail("No cart with code %s.", cartCode)
                .param("cartCode", cartCode)
                .build();
    }

    /**
     * Replaces a {@code ServiceException} thrown mid-checkout, which reported {@code LEGACY.SERVICE_ERROR} and
     * therefore a 500 — a stale cart id in a request is the caller's problem, not ours.
     */
    public static ShoppingCartNotFoundException byId(Long cartId) {
        return new ErrorBuilder<>(CheckoutErrors.CART_NOT_FOUND, ShoppingCartNotFoundException::new)
                .detail("No cart with id %s.", cartId)
                .param("cartId", cartId)
                .build();
    }

}
