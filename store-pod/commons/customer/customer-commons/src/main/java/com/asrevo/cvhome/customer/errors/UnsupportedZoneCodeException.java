package com.asrevo.cvhome.customer.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ConversionException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A submitted zone (state or province) code has no match in the store's reference data.
 *
 * <p>
 * Separate from {@link UnsupportedCountryCodeException} because the two point at different form controls, and a
 * shopper whose country is fine but whose province is not needs to be told which one to change.
 * </p>
 */
public class UnsupportedZoneCodeException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UnsupportedZoneCodeException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static UnsupportedZoneCodeException of(Object zoneCode) {
        return new ErrorBuilder<>(CustomerErrors.UNSUPPORTED_ZONE_CODE, UnsupportedZoneCodeException::new)
                .detail("%s is not a supported zone code.", zoneCode)
                .param("zoneCode", zoneCode)
                .build();
    }

}
