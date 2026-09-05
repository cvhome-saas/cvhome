package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The quantity asked for is outside the sku's per-order bounds ({@code quantityOrderMinimum} ..
 * {@code quantityOrderMaximum}, a maximum of 0 meaning unbounded). Both bounds travel in {@code params}.
 */
public class CartQuantityOutOfRangeException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CartQuantityOutOfRangeException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CartQuantityOutOfRangeException of(String sku, int quantity, int minimum, int maximum) {
        return new ErrorBuilder<>(CheckoutErrors.CART_QUANTITY_OUT_OF_RANGE, CartQuantityOutOfRangeException::new)
                .detail("Quantity %d of %s is outside the allowed range %d..%d.", quantity, sku, minimum, maximum)
                .param("sku", sku)
                .param("quantity", quantity)
                .param("minimum", minimum)
                .param("maximum", maximum)
                .build();
    }

}
