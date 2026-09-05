package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/**
 * The sku cannot be bought: the catalog does not know it, inventory does not stock it, or it is flagged as not
 * purchasable. The sku travels in {@code params} so the storefront can point at the line.
 */
public class ProductNotPurchasableException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ProductNotPurchasableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ProductNotPurchasableException of(String sku) {
        return new ErrorBuilder<>(CheckoutErrors.PRODUCT_NOT_PURCHASABLE, ProductNotPurchasableException::new)
                .detail("Product %s cannot be purchased.", sku)
                .param("sku", sku)
                .build();
    }

}
