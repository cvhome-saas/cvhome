package com.asrevo.cvhome.store.controller.exception;

import java.io.Serial;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.store.errors.LegacyErrors;

/**
 * Reports {@link LegacyErrors#UNAUTHORIZED} — an un-migrated authorization failure.
 *
 * @deprecated use the typed subclasses of {@link BaseException}; removed once every module is migrated.
 */
@Deprecated(since = "error-handling-refactor")
public class UnauthorizedException extends GenericRuntimeException {

    private static final String ERROR_CODE = "401";

    @Serial
    private static final long serialVersionUID = 1L;

    public UnauthorizedException() {
        super("Not authorized");
    }

    public UnauthorizedException(String errorCode, String message) {
        super(errorCode, message);
    }

    public UnauthorizedException(String message) {
        super(ERROR_CODE, message);
    }


    @Override
    protected ErrorCode legacyErrorCode() {
        return LegacyErrors.UNAUTHORIZED;
    }
}
