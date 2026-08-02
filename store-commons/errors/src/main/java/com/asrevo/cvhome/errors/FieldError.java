package com.asrevo.cvhome.errors;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One field-level validation failure, so a client can bind the message to the control that caused it instead of showing
 * a single form-wide error.
 *
 * @param field   dotted path to the offending property, e.g. {@code variants[0].sku}
 * @param code    the {@link ErrorCode#code()} describing why the field is invalid
 * @param message human-readable fallback for clients with no translation for {@code code}
 * @param params  values referenced by the message, e.g. {@code {"max": 64}}
 */
public record FieldError(String field, String code, String message, Map<String, Object> params) {

    public FieldError {
        params = params == null ? Map.of() : Collections.unmodifiableMap(new LinkedHashMap<>(params));
    }

    public static FieldError of(String field, ErrorCode code, String message) {
        return new FieldError(field, code.code(), message, Map.of());
    }

    public static FieldError of(String field, String code, String message) {
        return new FieldError(field, code, message, Map.of());
    }

}
