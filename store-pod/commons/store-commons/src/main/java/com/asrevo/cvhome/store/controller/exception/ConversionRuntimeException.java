package com.asrevo.cvhome.store.controller.exception;

import java.io.Serial;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.store.errors.LegacyErrors;

/**
 * Reports {@link LegacyErrors#CONVERSION} — an un-migrated parse or conversion failure.
 *
 * @deprecated use the typed subclasses of {@link BaseException}; removed once every module is migrated.
 */
@Deprecated(since = "error-handling-refactor")
public class ConversionRuntimeException extends GenericRuntimeException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public ConversionRuntimeException(String errorCode, String message) {
        super(errorCode, message);
    }

    public ConversionRuntimeException(String message) {
        super(message);
    }

    public ConversionRuntimeException(Throwable exception) {
        super(exception);
    }

    public ConversionRuntimeException(String message, Throwable exception) {
        super(message, exception);
    }

    public ConversionRuntimeException(String errorCode, String message, Throwable exception) {
        super(errorCode, message, exception);
    }


    @Override
    protected ErrorCode legacyErrorCode() {
        return LegacyErrors.CONVERSION;
    }
}
