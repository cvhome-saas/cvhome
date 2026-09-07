package com.asrevo.cvhome.gateway.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.AuthenticationRequiredException;
import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;

/** The request reached a session-bound endpoint with no signed-in gateway session. */
public class SessionRequiredException extends AuthenticationRequiredException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected SessionRequiredException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static SessionRequiredException of() {
        return new ErrorBuilder<>(GatewayErrors.SESSION_REQUIRED, SessionRequiredException::new)
                .detail("Sign in to the console first.")
                .build();
    }

}
