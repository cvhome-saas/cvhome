package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;
import java.util.UUID;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/** An invitation was asked for an account that already has a password; a reset link is the tool for that. */
public class UserNotPendingException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected UserNotPendingException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static UserNotPendingException of(UUID userId) {
        return new ErrorBuilder<>(UaaErrors.USER_NOT_PENDING, UserNotPendingException::new)
                .detail("The account is not pending: it already has a password. Issue a reset link instead.")
                .param("userId", userId)
                .build();
    }

}
