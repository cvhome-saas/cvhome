package com.asrevo.cvhome.errors;

import java.io.Serial;

/**
 * The {@code store} request parameter was present but is not a store id.
 *
 * <p>
 * Rejected at the edge rather than carried inwards. A store id that cannot name a store matches nothing, so without
 * this the request travels as far as the permission layer and is refused there — answering 403 "you may not touch that
 * store" for what is really a malformed request, and sending whoever reads the log hunting for a grant that was never
 * the problem. Renders as 400.
 * </p>
 */
public class MalformedStoreIdException extends ConversionException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected MalformedStoreIdException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }

    public static MalformedStoreIdException of(Object store) {
        return new ErrorBuilder<>(CommonErrors.CONVERSION_FAILED, MalformedStoreIdException::new)
                .detail("%s is not a valid store id.", store)
                .param("store", store)
                .build();
    }

}
