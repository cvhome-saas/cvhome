package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/** A settings value is outside what the server accepts. */
public class SettingsInvalidException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected SettingsInvalidException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static SettingsInvalidException of(String field, String rule) {
        return new ErrorBuilder<>(UaaErrors.SETTINGS_INVALID, SettingsInvalidException::new)
                .detail("%s: %s.", field, rule)
                .fieldError(field, UaaErrors.SETTINGS_INVALID, rule)
                .build();
    }

}
