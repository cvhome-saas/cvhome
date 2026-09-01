package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The quantity asked for is outside the merchant's per-order floor or ceiling for that sku.
 *
 * <p>
 * Its own class rather than a second code on {@link ProductNotPurchasableException}: that one means the item is
 * not sellable at all, so a caller should stop offering it. This one refuses only <em>this amount</em> — the same
 * shopper buying fewer succeeds — and a caller that wants to retry smaller has to be able to branch on the type
 * rather than re-read the code at runtime.
 * </p>
 */
public class CartQuantityOutOfRangeException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CartQuantityOutOfRangeException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    /**
     * The bounds travel as params because the shopper-facing message is only useful with the numbers in it.
     *
     * @param maximum the per-order ceiling, {@code 0} for no limit
     */
    public static CartQuantityOutOfRangeException of(String sku, int quantity, int minimum, int maximum) {
        return new ErrorBuilder<>(CheckoutErrors.CART_QUANTITY_OUT_OF_RANGE, CartQuantityOutOfRangeException::new)
                .detail("Product %s sells between %d and %s per order; %d was asked.", sku, minimum,
                        maximum > 0 ? String.valueOf(maximum) : "unlimited", quantity)
                .param("sku", sku)
                .param("quantity", quantity)
                .param("minimum", minimum)
                .param("maximum", maximum)
                .build();
    }

}
