package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.OperationNotAllowedException;

/** A public client holds no secret, so there is nothing to rotate or revoke. */
public class ClientNotConfidentialException extends OperationNotAllowedException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ClientNotConfidentialException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static ClientNotConfidentialException of(String clientId) {
        return new ErrorBuilder<>(UaaErrors.CLIENT_NOT_CONFIDENTIAL, ClientNotConfidentialException::new)
                .detail("%s is a public client: it authenticates with PKCE and holds no secret.", clientId)
                .param("clientId", clientId)
                .build();
    }

}
