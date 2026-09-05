package com.asrevo.cvhome.checkout.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.StoreIOException;

/**
 * A money amount could not be rendered in the store's currency — a misconfigured currency code, in practice. Ours to
 * fix, hence 500.
 */
public class PriceNotFormattableException extends StoreIOException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PriceNotFormattableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static PriceNotFormattableException of(Object amount, Object currency, Throwable cause) {
        return new ErrorBuilder<>(CheckoutErrors.PRICE_NOT_FORMATTABLE, PriceNotFormattableException::new)
                .detail("Cannot format %s in currency %s.", amount, currency)
                .param("amount", amount)
                .param("currency", currency)
                .cause(cause)
                .build();
    }

}
