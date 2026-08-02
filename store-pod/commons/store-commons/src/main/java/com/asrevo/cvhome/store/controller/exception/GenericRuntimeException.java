package com.asrevo.cvhome.store.controller.exception;

import java.io.Serial;
import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.errors.ErrorCodeAware;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.store.errors.LegacyErrors;

import lombok.Getter;

/**
 * Root of the deprecated unchecked hierarchy.
 *
 * <p>
 * Implements {@link ErrorCodeAware} so its remaining throw sites render in the shared format with the status their
 * subclass intends, rather than falling through to a bare 500 as they did before. Each subclass reports its own code by
 * overriding {@link #legacyErrorCode()}.
 * </p>
 *
 * @deprecated use the typed subclasses of {@link BaseException}; this type is removed once every module is migrated.
 */
@Deprecated(since = "error-handling-refactor")
@Getter
public class GenericRuntimeException extends RuntimeException implements ErrorCodeAware {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String errorCode;

    private final String errorMessage;

    public GenericRuntimeException(String errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public GenericRuntimeException(String errorMessage) {
        super(errorMessage);
        this.errorCode = null;
        this.errorMessage = errorMessage;
    }

    public GenericRuntimeException(Throwable exception) {
        super(exception);
        this.errorCode = null;
        this.errorMessage = exception.getMessage();
    }

    public GenericRuntimeException(String errorMessage, Throwable exception) {
        super(errorMessage, exception);
        this.errorCode = null;
        this.errorMessage = errorMessage;
    }

    public GenericRuntimeException(String errorCode, String errorMessage, Throwable exception) {
        super(errorMessage, exception);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    @Override
    public ErrorPayload payload() {
        return new ErrorPayload(legacyErrorCode(), errorMessage, Map.of(), List.of());
    }

    /**
     * The code this exception reports until its throw sites are migrated. Overridden by each subclass so the legacy
     * hierarchy keeps the distinctions it already made.
     */
    protected ErrorCode legacyErrorCode() {
        return LegacyErrors.SERVICE_ERROR;
    }

}
