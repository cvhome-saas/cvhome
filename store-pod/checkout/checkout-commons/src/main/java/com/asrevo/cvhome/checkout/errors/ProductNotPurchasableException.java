package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The product cannot be added to a cart at all — it is unpublished, or has no inventory configured.
 *
 * <p>
 * Distinct from catalog's {@code InsufficientInventoryException}, which means the stock ran out: this one says the
 * product was never sellable, so re-trying later will not help. For a refusal of the <em>amount</em> only, see
 * {@link CartQuantityOutOfRangeException}.
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

}
