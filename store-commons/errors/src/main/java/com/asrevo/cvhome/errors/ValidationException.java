package com.asrevo.cvhome.errors;

import java.io.Serial;

/**
 * Input failed validation. Renders as HTTP 400 with a populated {@code fieldErrors} array whenever the failure can be
 * attributed to specific properties, which is what lets a client highlight the offending form controls.
 */
public abstract class ValidationException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ValidationException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }


}
