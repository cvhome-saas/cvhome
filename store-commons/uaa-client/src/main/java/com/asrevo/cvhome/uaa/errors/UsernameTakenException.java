package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/** Another account already signs in with that username. */
public class UsernameTakenException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String FIELD = "username";

    private static final String DETAIL = "That username is already taken.";

    protected UsernameTakenException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static UsernameTakenException of(String username) {
        return new ErrorBuilder<>(UaaErrors.USERNAME_TAKEN, UsernameTakenException::new)
                .detail(DETAIL)
                .param(FIELD, username)
                .fieldError(FIELD, UaaErrors.USERNAME_TAKEN, DETAIL)
                .build();
    }

}
