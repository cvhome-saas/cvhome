package com.asrevo.cvhome.store.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A price string is not in a form the parser accepts — letters, a stray separator, an unparseable decimal.
 *
 * <p>
 * Separate from {@link NonPositivePriceException} because the two need different answers from a seller: one is "this is
 * not a number", the other is "this number is not allowed". Collapsing them into one type, as the previous
 * {@code ServiceException} did, is what made both surface as the same opaque "invalid price format".
 * </p>
 */
public class PriceNotParseableException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PriceNotParseableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static PriceNotParseableException of(String amount) {
        return of(amount, null);
    }

    public static PriceNotParseableException of(String amount, Throwable cause) {
        return new ErrorBuilder<>(StoreErrors.PRICE_NOT_PARSEABLE, PriceNotParseableException::new)
                .detail("Cannot parse %s as a price.", amount)
                .param("amount", amount)
                .cause(cause)
                .build();
    }

}
