package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/** Another account already carries that email address. */
public class EmailTakenException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String FIELD = "email";

    private static final String DETAIL = "That email address belongs to another account.";

    protected EmailTakenException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static EmailTakenException of(String email) {
        return new ErrorBuilder<>(UaaErrors.EMAIL_TAKEN, EmailTakenException::new)
                .detail(DETAIL)
                .param(FIELD, email)
                .fieldError(FIELD, UaaErrors.EMAIL_TAKEN, DETAIL)
                .build();
    }

}
