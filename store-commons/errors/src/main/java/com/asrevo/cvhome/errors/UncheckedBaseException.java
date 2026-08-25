package com.asrevo.cvhome.errors;

import java.io.Serial;

/**
 * The one unchecked exception in this module. It exists solely to carry a {@link BaseException} across a boundary that
 * cannot declare a checked exception — a {@code java.util.function} lambda, a Spring callback, a stream pipeline.
 *
 * <p>
 * Do not throw it to signal a failure. Throw the checked type that names the condition and let {@link Unchecked} wrap
 * it, so the intent stays visible and the code survives the round trip. The web layer unwraps this carrier before
 * rendering, so a carrier that escapes all the way out still produces the correct status rather than a 500.
 * </p>
 */
public class UncheckedBaseException extends RuntimeException implements ErrorCodeAware {

    @Serial
    private static final long serialVersionUID = 1L;

    private final transient ErrorPayload payload;

    public UncheckedBaseException(BaseException cause) {
        super(cause.getMessage(), cause);
        this.payload = cause.payload();
    }

    /**
     * The wrapped checked exception, always non-null.
     */
    @Override
    public synchronized BaseException getCause() {
        return (BaseException) super.getCause();
    }

    @Override
    public ErrorPayload payload() {
        return payload;
    }

}
