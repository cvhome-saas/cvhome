package com.asrevo.cvhome.customer.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A submitted country ISO code has no match in the store's reference data.
 *
 * <p>
 * The offending code travels in {@code params}, so a client can highlight the country control rather than re-parse a
 * sentence out of {@code detail} — which is all the previous
 * {@code ConversionException("Unsupported country code " + code)} offered.
 * </p>
 */
public class UnsupportedCountryCodeException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UnsupportedCountryCodeException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static UnsupportedCountryCodeException of(Object countryCode) {
        return new ErrorBuilder<>(CustomerErrors.UNSUPPORTED_COUNTRY_CODE, UnsupportedCountryCodeException::new)
                .detail("%s is not a supported country code.", countryCode)
                .param("countryCode", countryCode)
                .build();
    }

}
