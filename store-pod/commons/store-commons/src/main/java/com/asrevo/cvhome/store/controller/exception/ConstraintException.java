package com.asrevo.cvhome.store.controller.exception;

import java.io.Serial;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.store.errors.LegacyErrors;

/**
 * Reports {@link LegacyErrors#CONSTRAINT} — an un-migrated constraint violation; previously fell through to a 500.
 *
 * @deprecated use the typed subclasses of {@link BaseException}; removed once every module is migrated.
 */
@Deprecated(since = "error-handling-refactor")
public class ConstraintException extends GenericRuntimeException {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private static final String CONSTRAINT_ERROR_CODE = "506";

    public ConstraintException(String message) {
        super(CONSTRAINT_ERROR_CODE, message);
    }


    @Override
    protected ErrorCode legacyErrorCode() {
        return LegacyErrors.CONSTRAINT;
    }
}
