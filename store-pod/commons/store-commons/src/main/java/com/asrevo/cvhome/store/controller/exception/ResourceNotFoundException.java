package com.asrevo.cvhome.store.controller.exception;

import java.io.Serial;

import org.apache.commons.lang3.StringUtils;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.store.errors.LegacyErrors;

/**
 * Reports {@link LegacyErrors#NOT_FOUND} — an un-migrated missing resource.
 *
 * @deprecated use the typed subclasses of {@link BaseException}; removed once every module is migrated.
 */
@Deprecated(since = "error-handling-refactor")
public class ResourceNotFoundException extends ServiceRuntimeException {

    private static final String ERROR_CODE = "404";

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String errorCode, String message) {
        super(StringUtils.isBlank(errorCode) ? ERROR_CODE : errorCode, message);
    }

    public ResourceNotFoundException(String message) {
        super(ERROR_CODE, message);
    }


    @Override
    protected ErrorCode legacyErrorCode() {
        return LegacyErrors.NOT_FOUND;
    }
}
