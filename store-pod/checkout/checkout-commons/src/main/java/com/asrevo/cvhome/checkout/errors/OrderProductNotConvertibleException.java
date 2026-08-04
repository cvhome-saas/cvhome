package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A cart line could not be turned into an order line.
 *
 * <p>
 * The catch-all of the order populators, and deliberately the narrowest one: it names the sku, so the line that
 * blocked the order is in the response rather than only in a stack trace.
 * </p>
 */
public class OrderProductNotConvertibleException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected OrderProductNotConvertibleException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static OrderProductNotConvertibleException of(String sku, Throwable cause) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_PRODUCT_NOT_CONVERTIBLE,
                        OrderProductNotConvertibleException::new)
                .detail("Cart line %s could not be converted to an order line.", sku)
                .param("sku", sku)
                .cause(cause)
                .build();
    }

}
