package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/** The password matches one of the account's recent ones. */
public class PasswordReusedException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PasswordReusedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static PasswordReusedException of(int remembered) {
        return new ErrorBuilder<>(UaaErrors.PASSWORD_REUSED, PasswordReusedException::new)
                .detail("The password was used recently; the last %d cannot be reused.", remembered)
                .param("remembered", remembered)
                .build();
    }

}
