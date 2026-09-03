package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/** Unlinking would leave an account with no password and no identity: nothing to sign in with. */
public class LastCredentialException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected LastCredentialException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static LastCredentialException create() {
        return new ErrorBuilder<>(UaaErrors.LAST_CREDENTIAL, LastCredentialException::new)
                .detail("This is the account's only way to sign in. Set a password before unlinking it.")
                .build();
    }

}
