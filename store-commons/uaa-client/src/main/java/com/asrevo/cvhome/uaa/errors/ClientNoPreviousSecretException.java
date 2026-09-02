package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/** No previous secret is inside its grace window; there is nothing to end early. */
public class ClientNoPreviousSecretException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ClientNoPreviousSecretException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ClientNoPreviousSecretException of(String clientId) {
        return new ErrorBuilder<>(UaaErrors.CLIENT_NO_PREVIOUS_SECRET, ClientNoPreviousSecretException::new)
                .detail("%s has no previous secret still in its grace window.", clientId)
                .param("clientId", clientId)
                .build();
    }

}
