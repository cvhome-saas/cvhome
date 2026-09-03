package com.asrevo.cvhome.uaa.errors;

import java.io.Serial;

import com.asrevo.cvhome.errors.ErrorBuilder;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.errors.ResourceNotFoundException;

/** No session with that id belongs to the account — also the answer for another account's session. */
public class SessionNotFoundException extends ResourceNotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected SessionNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static SessionNotFoundException of(String sessionId) {
        return new ErrorBuilder<>(UaaErrors.SESSION_NOT_FOUND, SessionNotFoundException::new)
                .detail("No such session for this account.")
                .param("sessionId", sessionId)
                .build();
    }

}
