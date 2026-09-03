package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * A stored signing key whose private half cannot be decrypted — the crypto provider's key changed, or the row was
 * damaged. The key still verifies (its public half is plain) but can no longer sign, and uaa says so rather than
 * failing every token.
 */
public class SigningKeyUnusableException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected SigningKeyUnusableException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static SigningKeyUnusableException of(String kid, Throwable cause) {
        return new ErrorBuilder<>(UaaErrors.SIGNING_KEY_UNUSABLE, SigningKeyUnusableException::new)
                .detail("Signing key %s cannot be read back and is excluded from signing.", kid)
                .param("kid", kid)
                .cause(cause)
                .build();
    }

}
