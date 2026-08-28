package com.asrevo.cvhome.errors;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The data every error carries, independent of whether it travelled as a checked or unchecked exception.
 *
 * <p>
 * Keeping it in one immutable value means each exception subclass is a thin marker over a shared shape, and the web
 * layer can render any of them without knowing the concrete type.
 * </p>
 *
 * @param errorCode   what went wrong, and therefore the resulting HTTP status
 * @param detail      human-readable elaboration; may be {@code null} when the code says everything
 * @param params      structured context, e.g. {@code {"productId": 42, "storeId": 7}}
 * @param fieldErrors field-level failures; empty for everything that is not a validation error
 */
public record ErrorPayload(ErrorCode errorCode, String detail, Map<String, Object> params,
                           List<FieldError> fieldErrors) {

    public ErrorPayload {
        if (errorCode == null) {
            throw new IllegalArgumentException("errorCode is required");
        }
        params = params == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(params));
        fieldErrors = fieldErrors == null ? List.of() : List.copyOf(fieldErrors);
    }

    public static ErrorPayload of(ErrorCode errorCode) {
        return new ErrorPayload(errorCode, null, Map.of(), List.of());
    }

    public static ErrorPayload of(ErrorCode errorCode, String detail) {
        return new ErrorPayload(errorCode, detail, Map.of(), List.of());
    }

    /**
     * Message used for the exception itself and for server-side logs: the detail when present, otherwise the code, so a
     * stack trace is never anonymous.
     */
    public String toMessage() {
        return detail == null || detail.isBlank() ? errorCode.code()
                : String.format("[%s] %s", errorCode.code(), detail);
    }

}
