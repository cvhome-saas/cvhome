package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AccessDeniedStoreException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/**
 * The caller is an OAuth2 client, not a person.
 *
 * <p>
 * A {@code client_credentials} token is a valid principal for the admin API and is refused nothing there; it is only
 * the account endpoints — profile, sessions, password — that have no meaning for it. Forbidden, not unauthenticated:
 * the token is fine, the request is for something the token holder does not have.
 * </p>
 */
public class NotAUserPrincipalException extends AccessDeniedStoreException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected NotAUserPrincipalException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static NotAUserPrincipalException of(String principalName) {
        return new ErrorBuilder<>(UaaErrors.NOT_A_USER_PRINCIPAL, NotAUserPrincipalException::new)
                .detail("The caller is a service client, not a user account.")
                .param("principal", principalName)
                .build();
    }

}
