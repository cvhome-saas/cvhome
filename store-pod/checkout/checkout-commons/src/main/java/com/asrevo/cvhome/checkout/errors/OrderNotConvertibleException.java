package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * An order could not be assembled from the submitted payload.
 */
public class OrderNotConvertibleException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected OrderNotConvertibleException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static OrderNotConvertibleException of(Object store, Throwable cause) {
        return new ErrorBuilder<>(CheckoutErrors.ORDER_NOT_CONVERTIBLE, OrderNotConvertibleException::new)
                .detail("The submitted order could not be assembled for store %s.", store)
                .param("store", store)
                .cause(cause)
                .build();
    }

}
