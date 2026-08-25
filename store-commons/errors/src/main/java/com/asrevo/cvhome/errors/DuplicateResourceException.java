package com.asrevo.cvhome.errors;

import java.io.Serial;

/**
 * A uniqueness constraint would be violated — a code, SKU or slug already in use. Renders as HTTP 409.
 */
public abstract class DuplicateResourceException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected DuplicateResourceException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }


}
