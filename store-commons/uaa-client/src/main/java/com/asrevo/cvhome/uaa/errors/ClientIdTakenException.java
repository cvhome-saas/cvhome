package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.DuplicateResourceException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/** Another registration already authenticates with that {@code client_id}. */
public class ClientIdTakenException extends DuplicateResourceException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String FIELD = "clientId";

    private static final String DETAIL = "That client id is already registered.";

    protected ClientIdTakenException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ClientIdTakenException of(String clientId) {
        return new ErrorBuilder<>(UaaErrors.CLIENT_ID_TAKEN, ClientIdTakenException::new)
                .detail(DETAIL)
                .param(FIELD, clientId)
                .fieldError(FIELD, UaaErrors.CLIENT_ID_TAKEN, DETAIL)
                .build();
    }

}
