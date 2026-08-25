package com.asrevo.cvhome.cua.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The email address a shopper gave is already registered for this client.
 *
 * <p>
 * See {@link DuplicateUsernameException} for why the two are separate types rather than one message.
 * </p>
 */
public class DuplicateEmailException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicateEmailException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static DuplicateEmailException of(String clientId, String email) {
        return new ErrorBuilder<>(CuaErrors.EMAIL_TAKEN, DuplicateEmailException::new)
                .detail("That email address is already registered.")
                .param("clientId", clientId)
                .param("email", email)
                .build();
    }

}
