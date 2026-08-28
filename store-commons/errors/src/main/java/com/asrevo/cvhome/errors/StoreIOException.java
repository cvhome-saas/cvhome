package com.asrevo.cvhome.errors;

import java.io.Serial;

/**
 * Object storage or file I/O failed — an S3 put that was rejected, a bucket that could not be reached, a stream that
 * could not be read. Renders as HTTP 500 under the {@code STORAGE} category.
 *
 * <p>
 * The originating {@link java.io.IOException} belongs in {@link ErrorBuilder#cause(Throwable)}: it is logged with the
 * stack trace and correlated by {@code traceId}, but never rendered into the client response, because bucket names and
 * key paths are internal detail.
 * </p>
 */
public abstract class StoreIOException extends BaseException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected StoreIOException(ErrorPayload payload, Throwable cause) {
        super(payload, cause);
    }


}
