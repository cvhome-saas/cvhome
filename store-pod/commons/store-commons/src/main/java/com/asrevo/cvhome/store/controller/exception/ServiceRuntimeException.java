package com.asrevo.cvhome.store.controller.exception;

import java.io.Serial;

import org.apache.commons.lang3.StringUtils;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.store.errors.LegacyErrors;

/**
 * Reports {@link LegacyErrors#BAD_REQUEST} — an un-migrated service failure; the old advice rendered these as 400.
 *
 * @deprecated use the typed subclasses of {@link BaseException}; removed once every module is migrated.
 */
@Deprecated(since = "error-handling-refactor")
public class ServiceRuntimeException extends GenericRuntimeException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public ServiceRuntimeException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ServiceRuntimeException(String message) {
        super(message);
    }

    public ServiceRuntimeException(Throwable exception) {
        super(exception);
    }

    public ServiceRuntimeException(String message, Throwable exception) {
        super(message, exception);
    }

    public ServiceRuntimeException(String errorCode, String message, Throwable exception) {
        super(StringUtils.isBlank(errorCode) ? "500" : errorCode, message, exception);
    }


    @Override
    protected ErrorCode legacyErrorCode() {
        return LegacyErrors.BAD_REQUEST;
    }
}
