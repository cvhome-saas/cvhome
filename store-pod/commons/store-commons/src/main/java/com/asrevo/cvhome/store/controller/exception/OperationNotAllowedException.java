package com.asrevo.cvhome.store.controller.exception;

import java.io.Serial;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.store.errors.LegacyErrors;

/**
 * Reports {@link LegacyErrors#OPERATION_NOT_ALLOWED} — an un-migrated business-rule refusal.
 *
 * @deprecated use the typed subclasses of {@link BaseException}; removed once every module is migrated.
 */
@Deprecated(since = "error-handling-refactor")
public class OperationNotAllowedException extends ServiceRuntimeException {

    private static final String ERROR_CODE = "304";

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public OperationNotAllowedException(String message) {
        super(ERROR_CODE, message);
    }


    @Override
    protected ErrorCode legacyErrorCode() {
        return LegacyErrors.OPERATION_NOT_ALLOWED;
    }
}
