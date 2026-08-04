package com.asrevo.cvhome.store.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * A price parsed as a number but was zero or negative, where only a positive amount makes sense.
 *
 * <p>
 * A validation failure rather than a conversion one: the input was understood, it is the value that is refused — which
 * is why it can be attributed to the price field and rendered next to it.
 * </p>
 */
public class NonPositivePriceException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected NonPositivePriceException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static NonPositivePriceException of(String amount) {
        return new ErrorBuilder<>(StoreErrors.PRICE_NOT_POSITIVE, NonPositivePriceException::new)
                .detail("Price %s must be a positive amount.", amount)
                .param("amount", amount)
                .fieldError("price", StoreErrors.PRICE_NOT_POSITIVE, "must be a positive amount")
                .build();
    }

}
