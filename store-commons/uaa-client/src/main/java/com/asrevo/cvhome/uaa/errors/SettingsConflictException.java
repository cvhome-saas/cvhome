package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/** The settings changed under the caller; re-read and try again. */
public class SettingsConflictException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected SettingsConflictException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static SettingsConflictException of(long expectedVersion) {
        return new ErrorBuilder<>(UaaErrors.SETTINGS_CONFLICT, SettingsConflictException::new)
                .detail("The settings were changed by someone else; reload them and apply your change again.")
                .param("version", expectedVersion)
                .build();
    }

}
