package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/** No reset link can be used with that token — missing, expired, revoked or spent; one answer for all of them. */
public class ResetTokenNotUsableException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ResetTokenNotUsableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ResetTokenNotUsableException create() {
        return new ErrorBuilder<>(UaaErrors.RESET_TOKEN_NOT_USABLE, ResetTokenNotUsableException::new)
                .detail("This reset link is not valid. Ask an administrator for a new one.")
                .build();
    }

}
