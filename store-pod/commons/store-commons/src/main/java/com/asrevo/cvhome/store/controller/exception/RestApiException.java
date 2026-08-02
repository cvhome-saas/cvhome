package com.asrevo.cvhome.store.controller.exception;

import java.io.Serial;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.store.errors.LegacyErrors;

/**
 * Reports {@link LegacyErrors#BAD_REQUEST} — an un-migrated REST-layer failure.
 *
 * @deprecated use the typed subclasses of {@link BaseException}; removed once every module is migrated.
 */
@Deprecated(since = "error-handling-refactor")
public class RestApiException extends GenericRuntimeException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public RestApiException(String message) {
        super(message);
    }

    public RestApiException(Throwable exception) {
        super(exception);
    }


    @Override
    protected ErrorCode legacyErrorCode() {
        return LegacyErrors.BAD_REQUEST;
    }
}
