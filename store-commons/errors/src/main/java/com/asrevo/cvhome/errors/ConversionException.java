package com.asrevo.cvhome.errors;

import java.io.Serial;

/**
 * A value could not be parsed or converted — a price string, a country or zone code, a date. Renders as HTTP 400.
 *
 * <p>
 * Keeps the name the codebase already uses for this condition so the intent of existing call sites survives the
 * migration.
 * </p>
 */
public abstract class ConversionException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ConversionException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }


}
