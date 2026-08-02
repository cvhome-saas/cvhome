package com.asrevo.cvhome.store.core.exception;

import java.io.Serial;
import java.util.List;
import java.util.Map;

import com.asrevo.cvhome.errors.BaseException;
import com.asrevo.cvhome.errors.ErrorCode;
import com.asrevo.cvhome.errors.ErrorPayload;
import com.asrevo.cvhome.store.errors.LegacyErrors;

import lombok.Getter;

/**
 * Transitional bridge between the old single-exception style and the typed hierarchy.
 *
 * <p>
 * Now a subclass of {@link BaseException}, so its remaining throw sites render in the shared format — with a status
 * and a code — without being rewritten, and so existing {@code throws ServiceException} signatures keep compiling while
 * modules are migrated one at a time.
 * </p>
 *
 * <p>
 * New code must not use this type, and a migration must not "fix" a site by passing a better {@link ErrorCode} into it
 * — that keeps the useless signature, which is the actual defect. Define an exception that names the condition
 * ({@code PriceNotParseableException}, {@code ProductNotFoundException}, …) and declare it.
 * </p>
 *
 * @deprecated use the typed subclasses of {@link BaseException}; this type is removed once every module is migrated.
 */
@Deprecated(since = "error-handling-refactor")
@Getter
public class ServiceException extends BaseException {

    public static final int EXCEPTION_ERROR = 500;

    public static final int EXCEPTION_INVENTORY_MISMATCH = 120;

    @Serial
    private static final long serialVersionUID = -6854945379036729034L;

    private final int exceptionType;

    private final String messageCode;

    public ServiceException() {
        this(LegacyErrors.SERVICE_ERROR, null, null, 0, null);
    }

    public ServiceException(String messageCode) {
        this(LegacyErrors.SERVICE_ERROR, null, null, 0, messageCode);
    }

    public ServiceException(String message, Throwable cause) {
        this(LegacyErrors.SERVICE_ERROR, message, cause, 0, null);
    }

    public ServiceException(Throwable cause) {
        this(LegacyErrors.SERVICE_ERROR, cause.getMessage(), cause, 0, null);
    }

    public ServiceException(int exceptionType) {
        this(LegacyErrors.SERVICE_ERROR, null, null, exceptionType, null);
    }

    public ServiceException(int exceptionType, String message) {
        this(LegacyErrors.SERVICE_ERROR, message, null, exceptionType, null);
    }

    public ServiceException(int exceptionType, String message, String messageCode) {
        this(LegacyErrors.SERVICE_ERROR, message, null, exceptionType, messageCode);
    }

    private ServiceException(ErrorCode errorCode, String message, Throwable cause, int exceptionType,
                             String messageCode) {
        super(new ErrorPayload(errorCode, message, Map.of(), List.of()), cause);
        this.exceptionType = exceptionType;
        this.messageCode = messageCode;
    }

}
