package com.asrevo.cvhome.errors;

import java.util.List;
import java.util.Map;

/**
 * Implemented by every exception in this module, checked and unchecked alike, so the web layer needs exactly one
 * {@code @ExceptionHandler} branch to render any of them.
 */
public interface ErrorCodeAware {

    ErrorPayload payload();

    default ErrorCode errorCode() {
        return payload().errorCode();
    }

    default ErrorCategory category() {
        return errorCode().category();
    }

    default Map<String, Object> params() {
        return payload().params();
    }

    default List<FieldError> fieldErrors() {
        return payload().fieldErrors();
    }

}
