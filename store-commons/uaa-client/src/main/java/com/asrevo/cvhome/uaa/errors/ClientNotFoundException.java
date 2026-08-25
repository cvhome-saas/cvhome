package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/**
 * No OAuth2 registered client exists with the requested id.
 *
 * <p>
 * A separate type from {@link UserNotFoundException} even though both are 404s: the two are administered by different
 * consoles, and an operator reading {@code UAA.CLIENT.NOT_FOUND} in a log knows immediately that the missing thing is
 * a service registration, not a person.
 * </p>
 */
public class ClientNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ClientNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ClientNotFoundException of(String clientId) {
        return new ErrorBuilder<>(UaaErrors.CLIENT_NOT_FOUND, ClientNotFoundException::new)
                .detail("No registered client exists with id %s.", clientId)
                .param("clientId", clientId)
                .build();
    }

}
