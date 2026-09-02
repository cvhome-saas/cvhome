package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ValidationException;

/**
 * The current password given with a self-service change is wrong. A 400, not a 401: the session is fine.
 */
public class CurrentPasswordMismatchException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected CurrentPasswordMismatchException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static CurrentPasswordMismatchException of() {
        return new ErrorBuilder<>(UaaErrors.CURRENT_PASSWORD_MISMATCH, CurrentPasswordMismatchException::new)
                .detail("The current password is not correct.")
                .fieldError("currentPassword", UaaErrors.CURRENT_PASSWORD_MISMATCH, "does not match")
                .build();
    }

}
