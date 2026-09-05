package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;
import java.math.BigDecimal;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * An amount could not be rendered in the store's currency.
 */
public class PriceNotFormattableException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PriceNotFormattableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static PriceNotFormattableException of(BigDecimal amount, Throwable cause) {
        return new ErrorBuilder<>(CheckoutErrors.PRICE_NOT_FORMATTABLE, PriceNotFormattableException::new)
                .detail("Amount %s could not be formatted for this store.", amount)
                .param("amount", amount)
                .cause(cause)
                .build();
    }

}
