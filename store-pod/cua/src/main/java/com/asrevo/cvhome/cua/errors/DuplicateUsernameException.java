package com.asrevo.cvhome.cua.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The username a shopper chose is already registered for this client.
 *
 * <p>
 * A distinct type from {@link DuplicateEmailException} because the registration form has to highlight a different
 * field for each. Both were {@code IllegalArgumentException} before, told apart only by their message text, and the
 * controller bound whichever it caught to the {@code username} control — so a shopper whose <em>email</em> was taken
 * was shown the error under their username.
 * </p>
 */
public class DuplicateUsernameException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicateUsernameException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static DuplicateUsernameException of(String clientId, String username) {
        return new ErrorBuilder<>(CuaErrors.USERNAME_TAKEN, DuplicateUsernameException::new)
                .detail("That username is already taken.")
                .param("clientId", clientId)
                .param("username", username)
                .build();
    }

}
