package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The product cannot be added to a cart as asked.
 *
 * <p>
 * Two refusals share the class and are told apart by their code. {@link CheckoutErrors#PRODUCT_NOT_PURCHASABLE}
 * means the item is not sellable at all — unpublished, or with no inventory configured — so re-trying will not
 * help; unlike catalog's {@code InsufficientInventoryException}, the stock is not what ran out.
 * {@link CheckoutErrors#CART_QUANTITY_OUT_OF_RANGE} refuses only the amount, and buying fewer succeeds.
 * </p>
 */
public class ProductNotPurchasableException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String SKU = "sku";

    protected ProductNotPurchasableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductNotPurchasableException of(String sku) {
        return new ErrorBuilder<>(CheckoutErrors.PRODUCT_NOT_PURCHASABLE, ProductNotPurchasableException::new)
                .detail("Product %s cannot be purchased.", sku)
                .param(SKU, sku)
                .build();
    }

    /**
     * The merchant's per-order quantity floor or ceiling refuses the requested amount. The bounds travel as
     * params because the shopper-facing message is only useful with the numbers in it.
     */
    public static ProductNotPurchasableException quantityOutOfRange(String sku, int quantity, int minimum,
                                                                    int maximum) {
        return new ErrorBuilder<>(CheckoutErrors.CART_QUANTITY_OUT_OF_RANGE, ProductNotPurchasableException::new)
                .detail("Product %s sells between %d and %s per order; %d was asked.", sku, minimum,
                        maximum > 0 ? String.valueOf(maximum) : "unlimited", quantity)
                .param(SKU, sku)
                .param("quantity", quantity)
                .param("minimum", minimum)
                .param("maximum", maximum)
                .build();
    }

}
