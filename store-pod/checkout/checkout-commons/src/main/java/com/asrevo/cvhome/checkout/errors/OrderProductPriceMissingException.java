package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The catalog returned no final price for a cart line, so no order line can be priced from it.
 */
public class OrderProductPriceMissingException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected OrderProductPriceMissingException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static OrderProductPriceMissingException of(String sku) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_PRODUCT_PRICE_MISSING, OrderProductPriceMissingException::new)
                .detail("No final price for sku %s.", sku)
                .param("sku", sku)
                .build();
    }

}
