package com.asrevo.cvhome.errors;

import java.io.Serial;

/**
 * The addressed resource does not exist. Renders as HTTP 404.
 *
 * <p>
 * Prefer identifying the resource through {@link ErrorBuilder#param(String, Object)} rather than only in the detail
 * text, so a client can say <em>which</em> product was missing without parsing a sentence.
 * </p>
 */
public abstract class ResourceNotFoundException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected ResourceNotFoundException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }


}
