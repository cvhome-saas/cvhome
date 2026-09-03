package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/** The password appears in a known breach corpus. */
public class PasswordCompromisedException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected PasswordCompromisedException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static PasswordCompromisedException of() {
        return new ErrorBuilder<>(UaaErrors.PASSWORD_COMPROMISED, PasswordCompromisedException::new)
                .detail("The password appears in a known data breach; choose another.")
                .build();
    }

}
